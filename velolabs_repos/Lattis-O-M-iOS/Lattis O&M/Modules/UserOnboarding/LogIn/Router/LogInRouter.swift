//
//  LogInLogInRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class LogInRouter {
    class func instantiate(configure: (LogInInteractor) -> () = {_ in}) -> LogInViewController {
        let controller = UIStoryboard.userOnboarding.instantiateViewController(withIdentifier: "LogInViewController") as! LogInViewController
        let interactor = inject(controller: controller)
        configure(interactor)
        return controller
    }
    
    fileprivate weak var controller: UIViewController!
    init(_ controller: UIViewController) {
        self.controller = controller
    }
    
    func open(verification: UIViewController) {
        controller.navigationController?.pushViewController(verification, animated: true)
    }
}

private func inject(controller: LogInViewController) -> LogInInteractor {
    let interactor = LogInInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = LogInRouter(controller)
    return interactor
}

