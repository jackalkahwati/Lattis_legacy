//
//  MenuMenuRouter.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LGSideMenuController

final class MenuRouter: Router {
    class func instantiate() -> MenuViewController {
        let controller = MenuViewController()
        inject(controller: controller)
        return controller
    }
    
    func open(_ item: MenuItem) {
        switch item {
        case .ellipses:
            open(controller: LocksRouter.instantiate())
        case .home:
            open(controller: DashboardRouter.instantiate())
        case .profile:
            openProfile()
        case .sharing:
            open(controller: ShareRouter.instantiate())
        case .emergency:
            open(controller: EmergencyRouter.instantiate())
        case .find:
            open(controller: NavigationRouter.instantiate())
        case .terms:
            openTerms()
        case .order:
            openOrder()
        case .help:
            openHelp()
        case .logout:
            openHelp()
        }
    }
    
    fileprivate func open(controller: UIViewController) {
        if let navigation = self.controller.sideMenuController?.rootViewController as? UINavigationController {
            navigation.setViewControllers([controller], animated: false)
            largeTitleWhiteStyle(navigation.navigationBar)
        }
        self.controller.hideLeftViewAnimated(nil)
    }
    
    fileprivate func openTerms() {
        let terms = TermsAndConditionsViewController()
        let navigation = NavigationController(rootViewController: terms)
        largeTitleWhiteStyle(navigation.navigationBar)
        let interactor = (controller as? MenuViewController)?.interactor
        terms.delegate = interactor
        interactor?.terms = terms
        controller.present(navigation, animated: true, completion: nil)
        controller.hideLeftViewAnimated(nil)
    }
    
    fileprivate func openProfile() {
        let profile = ProfileRouter.instantiate()
        let nav = NavigationController(rootViewController: profile)
        largeTitleWhiteStyle(nav.navigationBar)
        controller.present(nav, animated: true, completion: nil)
        controller.hideLeftViewAnimated(nil)
    }
    
    fileprivate func openOrder() {
        let nav = WebViewController.navigation(with: "action_order_ellipse".localized(), url: URL(string: "https://www.lattis.io")!)
        controller.present(nav, animated: true, completion: nil)
        controller.hideLeftViewAnimated(nil)
    }
    
    fileprivate func openHelp() {
        let nav = WebViewController.navigation(with: "action_help".localized(), url: URL(string: "https://lattis.helpscoutdocs.com")!)
        controller.present(nav, animated: true, completion: nil)
        controller.hideLeftViewAnimated(nil)
    }
}

private func inject(controller: MenuViewController) {
    let interactor = MenuInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = MenuRouter(controller)
    
    AppDelegate.shared.menuRouter = interactor.router
}
