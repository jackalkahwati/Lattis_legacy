//
//  LogInLogInInteractorProtocols.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 06/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol LogInInteractorInput: TermsAndConditionsDelegate {
    func login(with credentials: User.Credentials?)
    func restorePassword(for phoneNumber: String?)
    func switchTo(screen: LogInRouter.Screen, with credentials: User.Credentials?)
    
    func resendCode(to phone: String)
    func save(pass: String, code: String, phone: String)
    
    func confirm(code: String)
    
    func facebookLogin()
}

protocol LogInInteractorOutput: InteractorOutput {

}

protocol TermsInteractorOutput: InteractorOutput  {
    func showTermsAndConditions(header: String, body: String)
}
