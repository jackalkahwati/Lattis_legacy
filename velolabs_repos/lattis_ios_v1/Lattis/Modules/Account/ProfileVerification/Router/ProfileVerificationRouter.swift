//
//  ProfileVerificationProfileVerificationRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class ProfileVerificationRouter: BaseRouter {
    class func instantiate(verificationType: ProfileVerificationType) -> ProfileVerificationViewController {
        let verification = UIStoryboard.account.instantiateViewController(withIdentifier: "verification") as! ProfileVerificationViewController
        let interactor = inject(controller: verification)
        interactor.verificationType = verificationType
        return verification
    }
}

private func inject(controller: ProfileVerificationViewController) -> ProfileVerificationInteractor {
    let interactor = ProfileVerificationInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = ProfileVerificationRouter(controller)
    return interactor
}
