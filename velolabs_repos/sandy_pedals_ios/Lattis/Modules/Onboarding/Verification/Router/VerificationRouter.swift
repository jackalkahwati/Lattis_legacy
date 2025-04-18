//
//  VerificationVerificationRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class VerificationRouter: BaseRouter {
    class func instantiate(with delegate: WelcomeInteractorDelegate, loadingText: String) -> VerificationViewController {
        let controller = WelcomeRouter.storyboard.instantiateViewController(withIdentifier: "verification") as! VerificationViewController
        let interactor = inject(controller: controller)
        interactor.delegate = delegate
        interactor.loadingText = loadingText
        return controller
    }
}

private func inject(controller: VerificationViewController) -> VerificationInteractor {
    let interactor = VerificationInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = VerificationRouter(controller)
    return interactor
}
