//
//  SideMenuSideMenuRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 05/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LGSideMenuController

final class SideMenuRouter: BaseRouter {
    func open(fleet: Fleet) {
        let menu = controller.sideMenuController
        let navigation = menu?.rootViewController as? UINavigationController
        let dashboard = DashboardRouter.instantiate(with: fleet)
        navigation?.setViewControllers([dashboard], animated: false)
        menu?.hideLeftViewAnimated(sender: self)
    }
}

class SideMenuConfigurator: NSObject {
    @IBOutlet weak var controller: SideMenuViewController!
    override func awakeFromNib() {
        super.awakeFromNib()
        inject(controller: controller)
    }
}

private func inject(controller: SideMenuViewController) {
    let interactor = SideMenuInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = SideMenuRouter(controller)
}
