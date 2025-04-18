//
//  SignUpSignUpRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class SignUpRouter: BaseRouter {
    static func instantiate(with delegate: WelcomeInteractorDelegate) -> SignUpViewController {
        let controllet = WelcomeRouter.storyboard.instantiateViewController(withIdentifier: "signUp") as! SignUpViewController
        let interactor = inject(controller: controllet)
        interactor.delegate = delegate
        return controllet
    }
    
    func openLogIn(with delegate: WelcomeInteractorDelegate) {
        let login = LogInRouter.instantiate(with: delegate)
        controller.navigationController?.setViewControllers([login], animated: true)
    }
    
    func openVerification(with delegate: WelcomeInteractorDelegate) {
        let verification = VerificationRouter.instantiate(with: delegate, loadingText: "signup_verification_loading".localized())
        controller.navigationController?.pushViewController(verification, animated: true)
    }
}

private func inject(controller: SignUpViewController) -> SignUpInteractor {
    let interactor = SignUpInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = SignUpRouter(controller)
    return interactor
}
