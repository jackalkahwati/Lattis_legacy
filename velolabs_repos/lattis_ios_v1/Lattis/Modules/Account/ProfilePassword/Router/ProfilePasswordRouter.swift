//
//  ProfilePasswordProfilePasswordRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class ProfilePasswordRouter: BaseRouter {
    class func instantiate() -> ProfilePasswordViewController {
        let controller = UIStoryboard.account.instantiateViewController(withIdentifier: "password") as! ProfilePasswordViewController
        inject(controller: controller)
        return controller
    }
}

private func inject(controller: ProfilePasswordViewController) {
    let interactor = ProfilePasswordInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = ProfilePasswordRouter(controller)
}
