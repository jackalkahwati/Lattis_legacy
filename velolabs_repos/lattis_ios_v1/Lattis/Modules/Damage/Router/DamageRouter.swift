//
//  DamageDamageRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

class DamageRouter: BaseRouter {
    class func navigation(configure: (DamageInteractor) -> () = { _ in }) -> UINavigationController {
        let controller = instantiate(configure: configure)
        return UINavigationController(rootViewController: controller, style: .grey)
    }
    
    class func instantiate(configure: (DamageInteractor) -> () = { _ in }) -> DamageViewController {
        let controller = UIStoryboard(name: "Damage", bundle: nil).instantiateViewController(withIdentifier: "damage") as! DamageViewController
        let interactor = inject(controller: controller)
        configure(interactor)
        return controller
    }
    
    func close() {
        controller.dismiss(animated: true, completion: nil)
    }
}

private func inject(controller: DamageViewController) -> DamageInteractor {
    let interactor = DamageInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = DamageRouter(controller)
    return interactor
}

