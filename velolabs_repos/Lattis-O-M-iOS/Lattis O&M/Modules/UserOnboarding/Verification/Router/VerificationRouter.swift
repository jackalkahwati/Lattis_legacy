//
//  VerificationVerificationRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 10/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class VerificationRouter {
    class func instantiate(configure: (VerificationInteractor) -> () = {_ in}) -> VerificationViewController {
        let controller = UIStoryboard.userOnboarding.instantiateViewController(withIdentifier: "VerificationViewController") as! VerificationViewController
        let interactor = inject(controller: controller)
        configure(interactor)
        return controller
    }
    
    fileprivate weak var controller: UIViewController!
    init(_ controller: UIViewController) {
        self.controller = controller
    }
}

private func inject(controller: VerificationViewController) -> VerificationInteractor {
    let interactor = VerificationInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = VerificationRouter(controller)
    return interactor
}
