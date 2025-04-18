//
//  LogInLogInRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class LogInRouter: BaseRouter {
    class func instantiate(with delegate: WelcomeInteractorDelegate) -> LogInViewController {
        let controllet = WelcomeRouter.storyboard.instantiateViewController(withIdentifier: "logIn") as! LogInViewController
        let interactor = inject(controller: controllet)
        interactor.delegate = delegate
        return controllet
    }
    
    func openSignUp(with delegate: WelcomeInteractorDelegate) {
        let signUp = SignUpRouter.instantiate(with: delegate)
        controller.navigationController?.setViewControllers([signUp], animated: true)
    }
    
    func openForgot(with delegate: ForgotInteractorDelegate) {
        let forgot = ForgotRouter.instantiate(with: delegate)
        controller.navigationController?.pushViewController(forgot, animated: true)
    }
    
    func openVerification(with delegate: WelcomeInteractorDelegate) {
        let verification = VerificationRouter.instantiate(with: delegate, loadingText: "login_verification_loading".localized())
        controller.navigationController?.pushViewController(verification, animated: true)
    }
}

private func inject(controller: LogInViewController) -> LogInInteractor {
    let interactor = LogInInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = LogInRouter(controller)
    return interactor
}
