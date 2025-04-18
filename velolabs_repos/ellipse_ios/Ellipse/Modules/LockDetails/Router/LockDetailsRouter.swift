//
//  LockDetailsLockDetailsRouter.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 27/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class LockDetailsRouter: Router {
    class func instantiate(_ lock: Ellipse.Lock) -> LockDetailsViewController {
        let controller = LockDetailsViewController()
        inject(controller: controller).lock = lock
        return controller
    }
    
    func pop() {
        controller.navigationController?.popViewController(animated: true)
    }
    
    func dismiss() {
        controller.dismiss(animated: true, completion: nil)
    }
    
    func editName(info: LockDetails.Info) -> EditController {
        let edit = EditController(content: info)
        controller.present(edit, animated: true, completion: nil)
        return edit
    }
    
    func open(info: LockDetails.Info) {
        switch info {
        case .pin(let code):
            edit(pin: code)
        default:
            break
        }
    }
    
    fileprivate func edit(pin: [Ellipse.Pin]) {
        let pinController = UIStoryboard.lockOnboarding.instantiateViewController(withIdentifier: PageType.pin.rawValue) as! OnboardingPinPage
        pinController.style = .edit(pin)
        pinController.delegate = (controller as? LockDetailsViewController)?.interactor
        controller.navigationController?.pushViewController(pinController, animated: true)
    }
}

private func inject(controller: LockDetailsViewController) -> LockDetailsInteractor {
    let interactor = LockDetailsInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = LockDetailsRouter(controller)
    return interactor
}
