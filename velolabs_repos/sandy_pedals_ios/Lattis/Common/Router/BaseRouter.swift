//
//  BaseRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 20/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import LGSideMenuController

class BaseRouter {
    internal weak var controller: UIViewController!
    init(_ controller: UIViewController) {
        self.controller = controller
    }
    
    func openMenu(configure: (MenuInteractor) -> () = { _ in }) {
        let controller = self.controller.sideMenuController?.leftViewController as? MenuViewController
        if let interactor = controller?.interactor as? MenuInteractor {
            configure(interactor)
        }
        controller?.interactor.viewLoaded()
        self.controller.sideMenuController?.showLeftViewAnimated()
    }
}
