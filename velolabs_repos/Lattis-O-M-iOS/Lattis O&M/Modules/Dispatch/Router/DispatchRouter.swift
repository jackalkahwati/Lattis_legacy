//
//  DispatchDispatchRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class DispatchRouter: BaseRouter {
    class func instantiate(with lock: Lock, fromRoot: Bool = false) -> DispatchViewController {
        let controller = UIStoryboard.dashboard.instantiateViewController(withIdentifier: "dispatch") as! DispatchViewController
        controller.title = "dispatch_title".localized()
        let interactor = inject(controller: controller)
        interactor.lock = lock
        interactor.fromRoot = fromRoot
        return controller
    }
    
    func pop(root: Bool) {
        if root {
            controller.navigationController?.popToRootViewController(animated: true)
        } else {
            _ = controller.navigationController?.popViewController(animated: true)
        }
    }
}

private func inject(controller: DispatchViewController) -> DispatchInteractor {
    let interactor = DispatchInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = DispatchRouter(controller)
    return interactor
}
