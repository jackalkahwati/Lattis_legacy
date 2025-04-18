//
//  NavigationNavigationRouter.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 15/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class NavigationRouter: Router {
    class func instantiate(ellipse: Ellipse? = nil) -> NavigationViewController {
        let nav = UIStoryboard.navigation.instantiateInitialViewController() as! NavigationViewController
        let interactor = inject(controller: nav)
        interactor.selected = ellipse
        return nav
    }
}

private func inject(controller: NavigationViewController) -> NavigationInteractor {
    let interactor = NavigationInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = NavigationRouter(controller)
    return interactor
}
