//
//  LocksLocksRouter.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 26/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class LocksRouter: Router {
    class func instantiate() -> LocksViewController {
        let controller = LocksViewController()
        inject(controller: controller)
        return controller
    }
    
    func openOnboarding() {
        let onboard = LockOnboardingRouter.instantiate(delegate: nil)
        let navigation = UINavigationController(rootViewController: onboard)
        largeTitleWhiteStyle(navigation.navigationBar)
        controller.present(navigation, animated: true, completion: nil)
    }
    
    func open(lock: Ellipse.Lock) {
        let details = LockDetailsRouter.instantiate(lock)
        controller.navigationController?.pushViewController(details, animated: true)
    }
}

private func inject(controller: LocksViewController) {
    let interactor = LocksInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = LocksRouter(controller)
}
