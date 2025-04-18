//
//  WelcomeWelcomeRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 10/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class WelcomeRouter {
    class func instantiate(configure: (WelcomeInteractor) -> () = {_ in}) -> WelcomeViewController {
        let controller = UIStoryboard.userOnboarding.instantiateViewController(withIdentifier: "WelcomeViewController") as! WelcomeViewController
        let interactor = inject(controller: controller)
        configure(interactor)
        return controller
    }
    
    fileprivate weak var controller: UIViewController!
    init(_ controller: UIViewController) {
        self.controller = controller
    }
    
    func signIn(configure: (LogInInteractor) -> () = {_ in}) {
        let login = LogInRouter.instantiate(configure: configure)
        let navigation = UINavigationController(rootViewController: login)
        navigation.modalTransitionStyle = .flipHorizontal
        controller.present(navigation, animated: true, completion: nil)
    }
    
    func verifiCation(configure: (VerificationInteractor) -> () = {_ in}) -> UIViewController {
        let verification = VerificationRouter.instantiate(configure: configure)
        return verification
    }
    
    func openDashboard() {
        let dashboard = DashboardRouter.navigation()
        controller.dismiss(animated: true, completion: nil)
        controller.navigationController?.setViewControllers([dashboard], animated: false)
    }
}


private func inject(controller: WelcomeViewController) -> WelcomeInteractor {
    let interactor = WelcomeInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = WelcomeRouter(controller)
    return interactor
}
