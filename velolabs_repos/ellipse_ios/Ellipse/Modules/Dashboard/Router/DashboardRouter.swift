//
//  DashboardDashboardRouter.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LGSideMenuController

fileprivate class SideMenuController: LGSideMenuController {
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .default
    }
}

final class DashboardRouter: Router {
    class func instantiate(_ isInitial: Bool = false) -> DashboardViewController {
        let controller = DashboardViewController()
        let interactor = inject(controller: controller)
        interactor.isInitial = isInitial
        return controller
    }
    
    class func menu(_ isInitial: Bool = false) -> UIViewController {
        let menuController = MenuRouter.instantiate()
        let dashboard = instantiate(isInitial)
        let navigation = NavigationController(rootViewController: dashboard)
        navigation.view.backgroundColor = .white
        largeTitleWhiteStyle(navigation.navigationBar)
        let menu = SideMenuController(rootViewController: navigation, leftViewController: menuController, rightViewController: nil)
        menu.isLeftViewSwipeGestureEnabled = false
        menu.leftViewWidth = 285
        menu.leftViewStatusBarStyle = .lightContent
        return menu
    }
    
    func showOnboarding(delegate: LockOnboardingDelegate) {
        let onboarding = LockOnboardingRouter.instantiate(delegate: delegate)
        let nav = NavigationController(rootViewController: onboarding)
        largeTitleWhiteStyle(nav.navigationBar)
        controller.present(nav, animated: true, completion: nil)
    }
    
    func contacts(delegate: EmergencyDelegate) {
        let contacts = EmergencyRouter.instantiate()
        contacts.delegate = delegate
        let navigaton = NavigationController(rootViewController: contacts)
        largeTitleWhiteStyle(navigaton.navigationBar)
        controller.present(navigaton, animated: true, completion: nil)
    }
    
    func navigate(to ellipse: Ellipse) {
        let map = NavigationRouter.instantiate(ellipse: ellipse)
        let navigation = NavigationController(rootViewController: map)
        largeTitleWhiteStyle(navigation.navigationBar)
        controller.present(navigation, animated: true, completion: nil)
    }
    
    func grant(permission: Permission) {
        let perm = PermissionsViewController()
        perm.modalTransitionStyle = .crossDissolve
        perm.delegate = (controller as? DashboardViewController)?.interactor
        controller.present(perm, animated: true, completion: nil)
    }
    
    func updateFW() {

    }
    
    func openSettings(_ lock: Ellipse.Lock) {
        let settings = LockDetailsRouter.instantiate(lock)
        settings.backToDashboard = true
        if let nav = controller.navigationController {
            nav.pushViewController(settings, animated: true)
            largeTitleWhiteStyle(nav.navigationBar)
        }
    }
    
    func openLocks() {
        AppDelegate.shared.menuRouter?.open(.ellipses)
    }
}

private func inject(controller: DashboardViewController) -> DashboardInteractor {
    let interactor = DashboardInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = DashboardRouter(controller)
    return interactor
}
