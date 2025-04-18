//
//  LoginErrorHandler.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/27/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Oval

final class LoginErrorHandler: ErrorHandler {
    var isFacebook: Bool = false
    var isSignUp: Bool = false
    
    func event(success: Bool = false) -> Event {
        let method: Event.Method = isFacebook ? .facebook : .phone
        if isSignUp {
            return .signUp(method, success)
        } else {
            return .logIn(method, success)
        }
    }
    
    override func handleOval(error: SessionError) {
        guard let path = error.api.conveniencePath else { return super.handleOval(error: error)}
        report(error: error)
        switch path {
        case .registration:
            handleLogin(error: error)
        case .acceptTermsAndConditions, .termsAndConditions:
            view.show(warning: "alert.tc.accept.error.message".localized(), title: "alert.error.server.title".localized())
        case .signInCode:
            view.show(warning: "alert.verificationcode.resent.fail.message".localized(), title: "alert.verificationcode.resent.fail.title".localized())
        case .confirmForgotPasswordCode:
            log(.custom(.passwordRestore), error: error, attributes: [.succeded(false)])
            fallthrough
        case .confirmUserCode:
            view.show(warning: "incorrect_verification_code_alert".localized(), title: "incorrect_verification_code_title".localized())
        default:
            super.handleOval(error: error)
        }
    }
    
    fileprivate func handleLogin(error: SessionError) {
        log(event(), error: error)
        switch error.code {
        case .resourceNotFound:
            view.show(warning:"action_loginfailed_description".localized(), title: "alert.facebook.login.failed.title".localized())
        default:
            super.handleOval(error: error)
        }
    }
}
