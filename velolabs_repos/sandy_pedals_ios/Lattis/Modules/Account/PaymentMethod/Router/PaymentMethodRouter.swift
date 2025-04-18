//
//  PaymentMethodPaymentMethodRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/06/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class PaymentMethodRouter: BaseRouter {
    class func instantiate(accessory: CreditCardCell.Accessory, onClose: @escaping () -> () = {}) -> PaymentMethodViewController {
        let controller = PaymentMethodViewController()
        let interactor = inject(controller: controller, accessory: accessory)
        interactor.onClose = onClose
        return controller
    }
    
    class func navigation(accessory: CreditCardCell.Accessory = .none, onClose: @escaping () -> () = {}) -> UINavigationController {
        return UINavigationController(rootViewController: instantiate(accessory: accessory, onClose: onClose), style: .blue)
    }
    
    func open(card: CreditCard?) {
        let cardController = CreditCardRouter.instantiate(with: card)
        controller.navigationController?.pushViewController(cardController, animated: true)
    }
}

private func inject(controller: PaymentMethodViewController, accessory: CreditCardCell.Accessory) -> PaymentMethodInteractor {
    let interactor = PaymentMethodInteractor(accessory: accessory)
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = PaymentMethodRouter(controller)
    return interactor
}
