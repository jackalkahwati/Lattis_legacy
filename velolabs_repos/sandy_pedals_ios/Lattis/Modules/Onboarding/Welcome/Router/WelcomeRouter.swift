//
//  WelcomeWelcomeRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

class WelcomeRouter: BaseRouter {
    static let storyboard = UIStoryboard(name: "Onboarding", bundle: nil)
    fileprivate var mapInitializer: (MapViewController) -> () = {_ in}
    fileprivate var loginSucceded: () -> () = {}
    
    class func instantiate(mapInitializer: @escaping (MapViewController) -> () = {_ in}, loginSucceded: @escaping () -> () = {}) -> WelcomeViewController {
        let controller = storyboard.instantiateViewController(withIdentifier: "welcome") as! WelcomeViewController
        let interactor = inject(controller: controller)
        interactor.router.mapInitializer = mapInitializer
        interactor.router.loginSucceded = loginSucceded
        return controller
    }
    
    func openSignUp(with delegate: WelcomeInteractorDelegate) {
        let navigation = self.navigation(with: SignUpRouter.instantiate(with: delegate))
        controller.present(navigation, animated: true, completion: nil)
    }
    
    func openLogIn(with delegate: WelcomeInteractorDelegate) {
        let navigation = self.navigation(with: LogInRouter.instantiate(with: delegate))
        controller.present(navigation, animated: true, completion: nil)
    }
    
    func openDashboard() {
        loginSucceded()
        let menu = FindRideRouter.menu(configure: mapInitializer)
        controller.dismiss(animated: true, completion: nil)
        controller.navigationController?.setViewControllers([menu], animated: false)
    }
    
    private func navigation(with controller: UIViewController) -> UINavigationController {
        let navigation = UINavigationController(rootViewController: controller)
        navigation.modalTransitionStyle = .flipHorizontal
        navigation.isNavigationBarHidden = true
        return navigation
    }
}

private func inject(controller: WelcomeViewController) -> WelcomeInteractor {
    let interactor = WelcomeInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = WelcomeRouter(controller)
    return interactor
}
