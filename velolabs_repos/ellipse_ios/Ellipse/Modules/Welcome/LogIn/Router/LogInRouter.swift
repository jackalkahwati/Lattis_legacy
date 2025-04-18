//
//  LogInLogInRouter.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 06/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class LogInRouter: Router {
    static var onboarding: OnboardingViewController {
        return OnboardingViewController()
    }
    
    static var welcome: WelcomeViewController {
        let controller = WelcomeViewController()
        inject(controller: controller)
        return controller
    }
    
    class func screen(_ type: Screen) -> UIViewController {
        return UIStoryboard.welcome.instantiateViewController(withIdentifier: type.rawValue)
    }
    
    class func navigation(for screen: Screen) -> UINavigationController {
        let nav = NavigationController(rootViewController: self.instantiate(screen: screen))
        nav.isNavigationBarHidden = false
        nav.modalTransitionStyle = .flipHorizontal
        largeTitleWhiteStyle(nav.navigationBar)
        return nav
    }
    
    fileprivate class func instantiate(screen: Screen) -> UIViewController {
        let controller = screen.controller
        if let con = controller as? LogInViewController {
            inject(controller: con)
        }
        if let con = controller as? SignUpViewController {
            inject(controller: con)
        }
        if let con = controller as? WelcomeViewController {
            inject(controller: con)
        }
        return controller
    }
    
    func switchTo(screen: Screen) {
        let con = LogInRouter.instantiate(screen: screen)
        controller.navigationController?.setViewControllers([con], animated: true)
    }
    
    func openTerms(interactor: LogInInteractor) {
        let terms = TermsAndConditionsViewController()
        terms.delegate = interactor
        interactor.terms = terms
        terms.isLogin = true
        controller.navigationController?.pushViewController(terms, animated: true)
    }
    
    func openRestore(with phone: String, interactor: LogInInteractorInput) {
        let restore = RestorePasswordViewController()
        restore.phoneNumber = phone
        restore.interactor = interactor
        controller.navigationController?.pushViewController(restore, animated: true)
    }
    
    func openConfirmation(for phone: String, interactor: LogInInteractorInput) {
        let confirmation = WelcomeConfirmationViewController()
        confirmation.phoneNumber = phone
        confirmation.interactor = interactor
        controller.navigationController?.pushViewController(confirmation, animated: true)
    }
    
    func pop(root: Bool = false) {
        if root {
            controller.navigationController?.popToRootViewController(animated: true)
        } else {
            controller.navigationController?.popViewController(animated: true)
        }
    }
    
    func dismiss(completion: (() -> ())? = nil) {
        controller.navigationController?.dismiss(animated: true, completion: completion)
    }
}

extension LogInRouter {
    enum Screen: String {
        case logIn, signUp, welcome, restore, confirm, terms
        
        var controller: UIViewController {
            switch self {
            case .logIn:
                return LogInViewController()
            case .signUp:
                return SignUpViewController()
            default:
                return UIViewController()
            }
        }
    }
}

private func inject(controller: LogInViewController) {
    let interactor = LogInInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = LogInRouter(controller)
}

private func inject(controller: SignUpViewController) {
    let interactor = LogInInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = LogInRouter(controller)
}

private func inject(controller: WelcomeViewController) {
    let interactor = LogInInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = LogInRouter(controller)
}
