//
//  MenuMenuRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 27/02/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import LGSideMenuController

class MenuRouter {
    var homeController: UIViewController!
    class func instantiate(configure: (MenuInteractor) -> () = { _ in }) -> MenuViewController {
        let controller = UIStoryboard(name: "Menu", bundle: nil).instantiateViewController(withIdentifier: "menu") as! MenuViewController
        let interactor = inject(controller: controller)
        configure(interactor)
        return controller
    }
    
    fileprivate weak var controller: UIViewController!
    init(_ controller: UIViewController) {
        self.controller = controller
    }
    
    func openDamage(with bike: Bike) {
        controller.sideMenuController?.hideLeftViewAnimated()
        let damage = DamageRouter.navigation() { $0.bike = bike }
        controller.sideMenuController?.rootViewController?.present(damage, animated: true, completion: nil)
    }
    
    func openTheft(with bike: Bike) {
        controller.sideMenuController?.hideLeftViewAnimated()
        let theft = TheftRouter.navigation() { $0.bike = bike }
        controller.sideMenuController?.rootViewController?.present(theft, animated: true, completion: nil)
    }
    
    @discardableResult func openProfile() -> ProfileInteractor {
        let profile = ProfileRouter.instantiate()
        open(controller: profile.0)
        return profile.1
    }
    
    func openHome() {
        open()
    }
    
    func openHistory() {
        let history = RideHistoryListRouter.instantiate()
        open(controller: history)
    }
    
    func openHelp() {
        let help = HelpViewController.navigation
        controller.present(help, animated: true, completion: nil)
    }
    
    func openBilling() {
        let billing = PaymentMethodRouter.navigation()
        controller.sideMenuController?.rootViewController?.present(billing, animated: true, completion: nil)
        controller.sideMenuController?.hideLeftViewAnimated()
    }
    
    private func open(controller: UIViewController? = nil) {
        let ctrl = controller ?? homeController
        let navigation = self.controller.sideMenuController?.rootViewController as? UINavigationController
        navigation?.setViewControllers([ctrl!], animated: false)
        self.controller.sideMenuController?.hideLeftView(animated: true) {
//            if ctrl == self.homeController {
//                AppRouter.shared.endTrip()
//            }
        }
    }
}

private func inject(controller: MenuViewController) -> MenuInteractor {
    let interactor = MenuInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = MenuRouter(controller)
    AppRouter.shared.menuRouter = interactor.router
    return interactor
}
