//
//  DirectionsRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 14/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

final class DirectionsRouter: BaseRouter {
    class func navigation(with delegate: DirectionsInteractorDelegate, title: String? = nil, currentAllowed: Bool = true) -> UINavigationController {
        let controller = instantiate(with: delegate)
        controller.title = title?.uppercased()
        controller.currentAllowed = currentAllowed
        return UINavigationController(rootViewController: controller, style: .grey)
    }
    class func instantiate(with delegate: DirectionsInteractorDelegate) -> GetDirectionsViewController {
        let controller = GetDirectionsViewController(nibName: "GetDirectionsView", bundle: nil)
        let interactor = inject(controller: controller)
        interactor.delegate = delegate
        return controller
    }
    
    func dismiss() {
        controller.dismiss(animated: true, completion: nil)
    }
}

private func inject(controller: GetDirectionsViewController) -> DirectionsInteractor {
    let interactor = DirectionsInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = DirectionsRouter(controller)
    return interactor
}
