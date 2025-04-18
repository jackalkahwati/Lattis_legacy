//
//  LogInLogInInteractor.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 06/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import Crashlytics
import Oval
import KeychainSwift

class LogInInteractor {
    weak var view: LogInInteractorOutput! {
        didSet {
            errorHandler = LoginErrorHandler(view)
            facebook.errorhandler = errorHandler.handle(error:)
        }
    }
    weak var terms: TermsInteractorOutput?
    
    var router: LogInRouter!
    fileprivate var errorHandler: LoginErrorHandler!
    fileprivate let network: UserNetwork = Session.shared
    fileprivate var credentials: User.Credentials?
    fileprivate var facebook = FacebookHelper()
}

extension LogInInteractor: LogInInteractorInput {
    func restorePassword(for phoneNumber: String?) {
        guard let phone = phoneNumber else { return }
        let action = AlertView.Action(title: "ok".localized()) { [unowned self] (_) in
            self.view.startLoading(text: "sending_verification_code".localized())
            self.network.forgotPassword(phone: phone.trimmedPhoneNumber) { [weak self] result in
                switch result {
                case .success:
                    self?.view.stopLoading() {
                        self?.router.openRestore(with: phone, interactor: self!)
                    }
                case .failure(let error):
                    self?.errorHandler.handle(error: error)
                }
            }
        }
        AlertView.alert(title: phone, text: "we_will_send_sms".localized(), actions: [action]).show()
    }
    
    func login(with credentials: User.Credentials?) {
        self.credentials = credentials
        guard let user = credentials else { return }
        errorHandler.isFacebook = false
        errorHandler.isSignUp = user.isSigningUp
        view.startLoading(text: "checking_credentials".localized())
        network.login(user: user) { [weak self] result in
            switch result {
            case .success(let userId, let isVerified):
                self?.process(userId: userId, isVerified: isVerified)
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
    
    func switchTo(screen: LogInRouter.Screen, with credentials: User.Credentials?) {
        router.switchTo(screen: screen)
    }
    
    func resendCode(to phone: String) {
        view.startLoading(text: "sending_verification_code".localized())
        self.network.forgotPassword(phone: phone.trimmedPhoneNumber) { [weak self] result in
            switch result {
            case .success:
                self?.view.stopLoading(completion: nil)
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
    
    func save(pass: String, code: String, phone: String) {
        view.startLoading(text: "saving_new_password".localized())
        network.confirm(forgot: code, phone: phone.trimmedPhoneNumber, password: pass) { [weak self] result in
            switch result {
            case .success:
                log(.custom(.passwordRestore), attributes: [.succeded(true)])
                self?.view.stopLoading() {
                    let action = AlertView.Action(title: "ok".localized()) { (_) in
                        self?.router.pop()
                    }
                    AlertView.alert(title: "alert.success.title".localized(), text: "password_changed".localized(), actions: [action]).show()
                }
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
    
    func confirm(code: String) {
        view.startLoading(text: "checking_code".localized())
        network.confirm(signIn: code) { [weak self] result in
            switch result {
            case .success:
                self?.checkTerms()
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
    
    func getTermsAndConditions() {
        terms?.startLoading(text: "loading_terms".localized())
        network.getTermsAndConditions { [weak self] result in
            switch result {
            case .success(let version,let body):
                self?.terms?.stopLoading(completion: nil)
                self?.terms?.showTermsAndConditions(header: version, body: body)
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
    
    func acceptTermsAndConditions(_ isAccepted: Bool) {
        guard isAccepted else {
            network.delete {_ in}
            router.pop(root: true)
            return
        }
        terms?.startLoading(text: "accepting".localized())
        network.acceptTermsAndConditions { [weak self] result in
            switch result {
            case .success:
                self?.openDashboard()
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
    
    func facebookLogin() {
        errorHandler.isFacebook = true
        facebook.login(nil) { [weak self] (user) in
            let credentials = User.Credentials(user)
            self?.credentials = credentials
            self?.view.startLoading(text: "checking_credentials".localized())
            self?.network.login(user: credentials) { [weak self] result in
                switch result {
                case .success(let userId, let isVerified):
                    self?.process(userId: userId, isVerified: isVerified)
                case .failure(let error):
                    self?.errorHandler.handle(error: error)
                }
            }
        }
    }
}

private extension LogInInteractor {
    func process(userId: Int, isVerified: Bool) {
        Session.shared.storage.userId = userId
        if let password = credentials?.password, isVerified {
            network.getTokens(userId: userId, password: password) { [weak self] result in
                switch result {
                case .success:
                    self?.checkTerms()
                case .failure(let error):
                    self?.errorHandler.handle(error: error)
                }
            }
        } else if let phone = credentials?.phone {
            network.signInCode { [weak self] result in
                switch result {
                case .success:
                    self?.view.stopLoading(completion: nil)
                    self?.router.openConfirmation(for: phone, interactor: self!)
                case .failure(let error):
                    self?.errorHandler.handle(error: error)
                }
            }
        } else {
            // TODO: show warning
        }
    }
    
    func checkTerms() {
        network.checkTerms { [weak self] result in
            switch result {
            case .success(let success):
                if success {
                    //                self?.network.delete(success: {}, fail: {_ in})
                    self?.openDashboard()
                } else {
                    self?.view.stopLoading(completion: nil)
                    self?.router.openTerms(interactor: self!)
                }
            case .failure:
                self?.view.stopLoading(completion: nil)
                self?.router.openTerms(interactor: self!)
            }
        }
    }
    
    func openDashboard() {
        KeychainSwift().login()
        view.stopLoading {
            self.router.dismiss {
                do {
                    try AppDelegate.shared.openDashboard()
                    log(self.errorHandler.event(success: true))
                } catch {
                    self.errorHandler.handle(error: error)
                }
            }
        }
    }
}

extension String {
    var trimmedPhoneNumber: String {
        return self.replacingOccurrences(of: " ", with: "").replacingOccurrences(of: "-", with: "").replacingOccurrences(of: "(", with: "").replacingOccurrences(of: ")", with: "")
    }
}
