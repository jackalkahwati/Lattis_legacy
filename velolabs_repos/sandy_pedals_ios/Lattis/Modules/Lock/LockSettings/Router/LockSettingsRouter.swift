//
//  LockSettingsLockSettingsRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 23/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

class LockSettingsRouter: BaseRouter {
    static var navigation: UINavigationController {
        return UINavigationController(rootViewController: LockSettingsViewController(), style: .grey)
    }
}

private func inject(controller: LockSettingsViewController) -> LockSettingsInteractor {
    let interactor = LockSettingsInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = LockSettingsRouter(controller)
    return interactor
}
