//
//  LogInInteractorProtocols.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 22/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import Foundation
import RestService

enum ValidationValueType {
    case password, email, phone
}

protocol LogInInteractorInput {
    var view: LogInInteractorOutput! { get set }
    var router: LogInRouter! { get set }
    func logIn(with user: Oval.Users.Request)
    func update(password: String)
    func confirm(code: String?, for phoneNumber: String?)
    func loginWithFacebook(in viewController: UIViewController)
    func getConfirmationCode(for phoneNumber: String?, needToRoute: Bool)
    func validate(text: String, with type: ValidationValueType) -> Bool
    func checkValidation(of valueTypes: [ValidationValueType]) -> Bool
    func getTermsAndConditions()
    func acceptTermsAndConditions(_ accept: Bool)
}

protocol LogInInteractorOutput: class {
    var interactor: LogInInteractorInput! { get set }
    func presentLoader(with message: String)
    func dismissLoader(completion: @escaping () -> ())
    func presentWarning(header: String, info: String)
    func changeValidation(state: Bool, for type: ValidationValueType)
    func showTermsAndConditions(header: String, body: String)
    func passwordChangingSuccess()
}


extension LogInInteractorOutput where Self: SLBaseViewController {
    func presentLoader(with message: String) {
        view.endEditing(true)
        presentLoadingViewWithMessage(message: message)
    }
    
    func dismissLoader(completion:@escaping () -> ()) {
        dismissLoadingViewWithCompletion(completion: completion)
    }
    
    func presentWarning(header: String, info: String) {
        view.endEditing(true)
        let texts:[SLWarningViewControllerTextProperty:String?] = [
            .Header: header,
            .Info: info,
            .CancelButton: NSLocalizedString("OK", comment: ""),
            .ActionButton: nil
        ]
        
        self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil)
    }
    
    func showTermsAndConditions(header: String, body: String) {}
    
    func changeValidation(state: Bool, for type: ValidationValueType) {}
    
    func passwordChangingSuccess() {}
}
