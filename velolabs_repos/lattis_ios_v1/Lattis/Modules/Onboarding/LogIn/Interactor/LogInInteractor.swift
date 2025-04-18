//
//  LogInLogInInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Oval

class LogInInteractor {
    weak var view: LogInInteractorOutput!
    var router: LogInRouter!
    weak var delegate: WelcomeInteractorDelegate!
    var network: UserNetwork!
    
    init(network: UserNetwork = Session.shared) {
        self.network = network
    }
}

extension LogInInteractor: LogInInteractorInput {
    func openSignUp() {
        router.openSignUp(with: delegate)
    }
    
    func forgotPassword() {
        router.openForgot(with: self)
    }
    
    func logIn(user: User.Request) {
        view.startLoading(with: "login_loading".localized())
        network.registration(user: user) { [weak self] (result) in
            switch result {
            case .success(let userId, let isVerified):
                if isVerified {
                    self?.view.stopLoading(completion: nil)
                    self?.delegate.loginSucceded(for: userId, password: user.password!)
                } else {
                    self?.verify(user: user)
                }
            case .failure(let error):
                self?.view.stopLoading(completion: nil)
                self?.handle(error: error)
            }
        }
    }
}

extension LogInInteractor: ForgotInteractorDelegate {
    func passwordChanged(for email: String) {
        view.show(email: email)
    }
}

private extension LogInInteractor {
    func handle(error: Error?) {
        var title = "general_error_title".localized()
        var text = "general_error_text".localized()
        if let error = error as? SessionError {
            switch error.code {
            case .unauthorized:
                title = "login_error_wrong_password_title".localized()
                text = "login_error_wrong_password_text".localized()
            case .resourceNotFound:
                title = "login_error_wrong_email_title".localized()
                text = "login_error_wrong_email_text".localized()
            default:
                break
            }
        }
        view.warning(with: title, subtitle: text)
    }
    
    func verify(user: User.Request) {
        network.getEmailVerificationCode(email: user.email, accoutnType: nil) { [weak self] (result) in
            switch result {
            case .success:
                self?.router.openVerification(with: self!.delegate)
                self?.view.stopLoading(completion: nil)
            case .failure(let error):
                Analytics.report(error)
                self?.view.stopLoading(completion: nil)
            }
        }
    }
}
