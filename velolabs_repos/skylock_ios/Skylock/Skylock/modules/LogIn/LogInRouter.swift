//
//  LogInRouter.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 22/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit

enum LogInType: String {
    case logIn, signUp
}

protocol LogInRouterDelegate: class {
    func logInSucceded(hasLocks: Bool)
}

final class LogInRouter {
    weak var delegate: LogInRouterDelegate?
    private let storyboard = UIStoryboard(name: "LogIn", bundle: nil)
    private weak var parent: UIViewController?
    init(_ parent: UIViewController) {
        self.parent = parent
    }
    
    func backToRoot() {
        _ = parent?.navigationController?.popToRootViewController(animated: true)
    }
    
    func present(logIn type: LogInType) -> LogInConfigurator {
        let controller = storyboard.instantiateViewController(withIdentifier: type.rawValue) as! LogInViewController
        parent?.navigationController?.pushViewController(controller, animated: true)
        let configurator = LogInConfigurator(controller)
        return configurator
    }
    
    class func instantiateLogin(with phoneNumber: String? = nil, configure: ((LogInInteractor) -> ())? = nil) -> LogInViewController {
        let controller = UIStoryboard(name: "LogIn", bundle: nil).instantiateViewController(withIdentifier: LogInType.logIn.rawValue) as! LogInViewController
        controller.phoneNumber = phoneNumber
        let configurator = LogInConfigurator(controller)
        configurator.configure = configure
        return controller
    }
    
    func presentConfirmation(from parent: UIViewController) -> LogInConfigurator {
        let vc = storyboard.instantiateViewController(withIdentifier: "confirmation") as! SmsConfirmationViewController
        if let navigation = parent.navigationController {
            navigation.pushViewController(vc, animated: true)
        } else {
            parent.present(vc, animated: true, completion: nil)
        }
        let configurator = LogInConfigurator(vc)
        return configurator
    }
    
    func presentTerms(from parent: UIViewController) -> LogInConfigurator {
        let vc = storyboard.instantiateViewController(withIdentifier: "termsAndConditions") as! TermsAndConditionsViewController
        if let navigation = parent.navigationController {
            navigation.pushViewController(vc, animated: true)
        } else {
            parent.present(vc, animated: true, completion: nil)
        }
        let configurator = LogInConfigurator(vc)
        return configurator
    }
    
    func presentChangePassword(from parent: UIViewController) -> LogInConfigurator {
        let vc = storyboard.instantiateViewController(withIdentifier: "password") as! ChangePasswordViewController
        if let navigation = parent.navigationController {
            var controllers = navigation.viewControllers
            _ = controllers.popLast()
            controllers.append(vc)
            navigation.setViewControllers(controllers, animated: true)
        } else {
            parent.present(vc, animated: true, completion: nil)
        }
        let configurator = LogInConfigurator(vc)
        return configurator
    }
    
    func pushToLockController() {
        let lvc = SLLockViewController()
        parent?.navigationController?.setViewControllers([lvc], animated: true)
    }
}

extension TermsAndConditionsViewController {
    static var stroryboard: TermsAndConditionsViewController {
        let controller = UIStoryboard(name: "LogIn", bundle: nil).instantiateViewController(withIdentifier: "termsAndConditions") as! TermsAndConditionsViewController
        _ = LogInConfigurator(controller)
        return controller
    }
}
