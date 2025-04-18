//
//  EndRideEndRideRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 17/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class EndRideRouter {
    static let storyboard = UIStoryboard(name: "EndRide", bundle: nil)
    class func navigation(configure: (EndRideInteractor) -> ()) -> UINavigationController {
        let controller = storyboard.instantiateViewController(withIdentifier: "EndRideViewController") as! EndRideViewController
        let interactor = inject(controller: controller)
        configure(interactor)
        return UINavigationController(rootViewController: controller, style: .white)
    }
    
    class func instantiate(type: ControllerType, configure: (EndRideInteractor) -> ()) -> UIViewController {
        let controller = EndRideRouter.storyboard.instantiateViewController(withIdentifier: type.rawValue)
        if let controller = controller as? EndRideInteractorOutput {
            let interactor = inject(controller: controller)
            configure(interactor)
        }
        return controller
    }
    
    fileprivate weak var controller: UIViewController!
    init(_ controller: UIViewController) {
        self.controller = controller
    }
    
    func pushController(with type: ControllerType, configure: (EndRideInteractor) -> ()) {
        let next = EndRideRouter.instantiate(type: type, configure: configure)
        controller.navigationController?.pushViewController(next, animated: true)
    }
    
    func dismiss() {
        controller.dismiss(animated: true, completion: nil)
    }
    
    func openPayments() {
        let billing = PaymentMethodRouter.navigation(accessory: .select)
        controller.present(billing, animated: true, completion: nil)
    }
}

extension EndRideRouter {
    enum ControllerType: String {
        case confirm = "EndRideViewController"
        case action = "EndRideActionViewController"
    }
}

private func inject(controller: EndRideInteractorOutput) -> EndRideInteractor {
    let interactor = EndRideInteractor()
    controller.interactor = interactor
    interactor.view = controller
    if let controller = controller as? UIViewController {
        interactor.router = EndRideRouter(controller)
    }
    return interactor
}
