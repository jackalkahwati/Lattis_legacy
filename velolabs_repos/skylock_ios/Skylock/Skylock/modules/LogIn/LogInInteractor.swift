//
//  LogInInteractor.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 22/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import KeychainSwift
import SwiftyJSON
import Crashlytics
import PhoneNumberKit
import RestService

final class LogInInteractor {
    weak var view: LogInInteractorOutput!
    var router: LogInRouter!
    fileprivate var locksService = LocksService()
    fileprivate var validationStore: Set<ValidationValueType> = []
    fileprivate let phoneNumberKit = PhoneNumberKit()
    fileprivate var phoneNumber: String?
    fileprivate var confirmationCode: String?
}

extension LogInInteractor: LogInInteractorInput {
    func logIn(with user: Oval.Users.Request) {
        guard checkValidation(for: user) else {
            let header = "Incorrect credentials.".localized()
            let info = "Plese check your credentials.".localized()
            view.presentWarning(header: header, info: info)
            return
        }
        view.presentLoader(with: "Checking credentials".localized())
        Oval.users.registration(user: user, success: { [weak self] (userId, isVerified) in
            self?.view.dismissLoader {}
            if isVerified {
                Oval.users.getTokens(userId: userId, password: user.password!, success: { 
                    self?.presentTermsIfNeeded()
                }, fail: { self?.loginFailed(with: $0) })
                Answers.logLogin(withMethod: "Phone Succeded", success: NSNumber(value: true), customAttributes: nil)
            } else {
                self?.getConfirmationCode(for: user.phoneNumber, needToRoute: true)
                
                Answers.logSignUp(withMethod: "Phone Succeded", success: NSNumber(value: true), customAttributes: nil)
            }
        }, fail: { [weak self] error in
            self?.loginFailed(with: error)
            if user.isSigningUp! {
                Answers.logSignUp(withMethod: "Phone Failed", success: NSNumber(value: false), customAttributes: ["error": "\(error)"])
            } else {
                Answers.logLogin(withMethod: "Phone Failed", success: NSNumber(value: false), customAttributes: ["error": "\(error)"])
            }
        })
    }
    
    private func presentTermsIfNeeded() {
        Oval.users.checkTerms(success: { [weak self] success in
            guard let `self` = self else { return }
            guard success == false else { return self.loginSucceded() }
            self.view.dismissLoader {}
            self.router.presentTerms(from: self.view as! UIViewController).configure = { interactor in
                interactor.router.delegate = self.router.delegate
                if let controller = interactor.view as? TermsAndConditionsViewController {
                    controller.isLogin = true
                }
            }
        }, fail: { [weak self] error in
            self?.loginFailed(with: error)
        })
    }
    
    private func checkValidation(for user: Oval.Users.Request) -> Bool {
        guard let isSignUp = user.isSigningUp else { return false }
        
        let signUp: [ValidationValueType] = [.phone, .email, .password]
        let login: [ValidationValueType] = [.phone, .password]
        
        return isSignUp && checkValidation(of: signUp) || isSignUp == false && checkValidation(of: login)
    }
    
    func checkValidation(of valueTypes: [ValidationValueType]) -> Bool {
        return validationStore.containsArray(array: valueTypes)
    }
    
    private func loginFailed(with error: Error) {
        var info = "\(error)"
        if let err = error as? Oval.Error,
            err == .resourceNotFound {
            info = "You have entered an incorrect password or the user doesn't exist. Please try again.".localized()
        }
        view.dismissLoader {
            self.view.presentWarning(header: "LOGIN FAILED".localized(), info: info)
        }
    }
    
    func getConfirmationCode(for phoneNumber: String?, needToRoute: Bool) {
        Oval.users.signInCode(success: { [weak self] in
            if needToRoute {
                self?.presentConfirmation(for: phoneNumber)
            } else {
                self?.view.dismissLoader {}
            }
            }, fail: { [weak self] error in
                let header = "SMS Code Error".localized()
                let info = "Sorry, sending SMS faild.".localized()
                self?.view.presentWarning(header: header, info: info)
        })
    }
    
    private func presentConfirmation(for phoneNumber: String?) {
        router.presentConfirmation(from: view as! UIViewController).configure = { interactor in
            interactor.router.delegate = self.router.delegate
            if let controller = interactor.view as? SmsConfirmationViewController {
                controller.phoneNumber = phoneNumber
            }
        }
    }
    
    func confirm(code: String?, for phoneNumber: String?) {
        guard let smsCode = code else {
            let header = "CODE VERIFICATION ERROR".localized()
            let info = "The verification code you have entered is incorrect.".localized()
            view.presentWarning(header: header, info: info)
            return
        }
        view.presentLoader(with: "Checking code...".localized())
        if let phoneNumber = phoneNumber {
            presentForgotPassword(phoneNumber: phoneNumber, code: smsCode)
        } else {
            Oval.users.confirm(signIn: smsCode, success: { [weak self] in
                self?.presentTermsIfNeeded()
                }, fail: { [weak self] error in
                    self?.view.dismissLoader {
                        let header = "CODE VERIFICATION ERROR".localized()
                        let info = "The verification code you have entered is incorrect.".localized()
                        self?.view.presentWarning(header: header, info: info)
                    }
            })
        }
    }
    
    func update(password: String) {
        guard let code = confirmationCode, let phone = phoneNumber else { return }
        view.presentLoader(with: "Saving new password.".localized())
        Oval.users.confirm(forgot: code, phone: phone, password: password, success: { [weak self] in
            self?.view.dismissLoader {
                self?.view.passwordChangingSuccess()
            }
        }, fail: { [weak self] error in
            self?.view.dismissLoader {
                let header = "Error Changing Password.".localized()
                let info = "Sorry, we weren't able to change your password. Please try again later.".localized()
                self?.view.presentWarning(header: header, info: info)
            }
        })
    }
    
    func presentForgotPassword(phoneNumber: String, code: String) {
        router.presentChangePassword(from: view as! UIViewController).configure = { interactor in
            interactor.router.delegate = self.router.delegate
            interactor.phoneNumber = phoneNumber
            interactor.confirmationCode = code
        }
    }
    
    func loginWithFacebook(in viewController: UIViewController) {
        FacebookService.shared.login(viewController: viewController, registration: { [weak self] in
            self?.view.presentLoader(with: "Logging in with Facebook".localized())
        }, completion: { [weak self] success in
            if success {
                self?.presentTermsIfNeeded()
                Answers.logLogin(withMethod: "Facebook Succeded", success: NSNumber(value: true), customAttributes: nil)
            } else {
                Answers.logLogin(withMethod: "Facebook Failed", success: NSNumber(value: false), customAttributes: nil)
                self?.view.dismissLoader {
                    let header = "LOGIN FAILED".localized()
                    let info = "Sorry, we couldn't log you in through Facebook right now. Please try again later, or you can sign in using your phone number and email.".localized()
                    self?.view.presentWarning(header: header, info: info)
                }
            }
        })
    }
    
    func getTermsAndConditions(){
        view.presentLoader(with: "Loading terms and conditions.".localized())
        Oval.users.getTermsAndConditions(success: { [weak self] (version, body) in
            self?.view.showTermsAndConditions(header: version, body: body)
            self?.view.dismissLoader {}
        }, fail: { [weak self] (error) in
            self?.view.dismissLoader {
                self?.view.presentWarning(header: "Server Error".localized(), info: "Sorry unable to fetch terms and conditions. please try again.".localized())
            }
        })
    }
    
    func acceptTermsAndConditions(_ accept: Bool) {
        guard accept else {
            Oval.users.delete(success: {}, fail: {_ in})
            router.backToRoot()
            return
        }
        view.presentLoader(with: "Accepting...".localized())
        Oval.users.acceptTermsAndConditions(success: { [weak self] (result) in
            self?.loginSucceded()
            }, fail: { [weak self] (error) in
                self?.view.dismissLoader {
                    self?.view.presentWarning(header: "Server Error".localized(), info: "Sorry unable to accept the terms and conditions. Please try again.".localized())
                }
        })
    }
    
    func validate(text: String, with type: ValidationValueType) -> Bool {
        var valid = text.characters.count >= type.minValue && text.characters.count <= type.maxValue
        switch type {
        case .phone:
            guard let region = Locale.current.regionCode else { valid = false; break }
            let phone = try? phoneNumberKit.parse(text, withRegion: region, ignoreType: true)
            valid = phone != nil
        case .email:
            valid = text.isValidEmail
        default:
            break
        }
        if valid {
            validationStore.insert(type)
            view.changeValidation(state: true, for: type)
        } else if text.characters.count < type.minValue {
            validationStore.remove(type)
            view.changeValidation(state: false, for: type)
        }
        return text.characters.count <= type.maxValue
    }
    
    private func save(ovalUser: Oval.Users.Responce) {
        SLDatabaseManager.shared().save(ovalUser: ovalUser, setAsCurrent: true)
        SLDatabaseManager.shared().setCurrentUser()
    }
    
    private func loginSucceded() {
        UserDefaults.standard.set(true, forKey: SLUserDefaultsSignedIn)
        UserDefaults.standard.synchronize()
        locksService.locks(updateCache: true) { [weak self] (locks, isServer, error) in
            guard isServer else { return }
            self?.view.dismissLoader {}
            self?.router.delegate?.logInSucceded(hasLocks: locks.isEmpty == false)
        }
        Oval.users.user(success: { [weak self] (result) in
            self?.save(ovalUser: result)
        }, fail: { error in
            
        })
    }
}

fileprivate extension SLUser {
    func fill(with user: Oval.Users.Responce) {
        userId = user.userId
        usersId = user.usersId
        email = user.email
        phoneNumber = user.phoneNumber
        firstName = user.firstName
        lastName = user.lastName
        userType = user.userType.rawValue
        countryCode = user.countryCode
        username = user.username
        title = user.title
        isVerified = user.isVerified
        maxLocks = user.maxLocks
    }
}

extension SLDatabaseManager {
    func save(ovalUser: Oval.Users.Responce, setAsCurrent: Bool, update: ((SLUser) -> ())? = nil) {
        let user = self.user(withId: ovalUser.userId, usersId: ovalUser.usersId)
        user.fill(with: ovalUser)
        update?(user)
        if setAsCurrent {
            user.isCurrentUser = true
        }
        self.save(user, withCompletion: nil)
        if setAsCurrent {
            self.setCurrentUser()
        }
    }
}

extension ValidationValueType {
    var minValue: Int {
        switch self {
        case .phone:
            return 4
        case .password:
            return 8
        case .email:
            return 5
        }
    }
    var maxValue: Int {
        switch self {
        case .phone:
            return Int.max
        case .password:
            return 20
        case .email:
            return Int.max
        }
    }
}

extension Set where Element: Equatable {
    func containsArray<T : Sequence> (array:T) -> Bool where T.Iterator.Element == Element {
        for item in array {
            if !self.contains(item) {
                return false
            }
        }
        return true
    }
}

extension String {
    var isValidEmail: Bool {
        let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailTest.evaluate(with: self)
    }
}
