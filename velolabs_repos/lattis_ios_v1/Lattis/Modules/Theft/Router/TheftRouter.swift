//
//  TheftTheftRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 04/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class TheftRouter: BaseRouter {
    class func navigation(configure: (TheftInteractor) -> () = {_ in}) -> UINavigationController {
        let controller = TheftViewController(nibName: "TheftViewController", bundle: nil)
        let interactor = inject(controller: controller)
        configure(interactor)
        return UINavigationController(rootViewController: controller, style: .grey)
    }
}

private func inject(controller: TheftViewController) -> TheftInteractor {
    let interactor = TheftInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = TheftRouter(controller)
    return interactor
}
