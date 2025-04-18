//
//  ForgotForgotRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class ForgotRouter: BaseRouter {
    class func instantiate(with delegate: ForgotInteractorDelegate) -> ForgotViewController {
        let controller = WelcomeRouter.storyboard.instantiateViewController(withIdentifier: "forgot") as! ForgotViewController
        let interactor = inject(controller: controller)
        interactor.delegate = delegate
        return controller
    }
    
    func openPassword(interactor: ForgotInteractor) {
        let password = WelcomeRouter.storyboard.instantiateViewController(withIdentifier: "newPassword") as! NewPasswordViewController
        password.interactor = interactor
        interactor.passwordView = password
        controller.navigationController?.pushViewController(password, animated: true)
    }
}

private func inject(controller: ForgotViewController) -> ForgotInteractor {
    let interactor = ForgotInteractor()
    controller.interactor = interactor
    interactor.emailView = controller
    interactor.router = ForgotRouter(controller)
    return interactor
}
