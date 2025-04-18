//
//  CreditCardCreditCardRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/06/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit


final class CreditCardRouter: BaseRouter {
    class func instantiate(with card: CreditCard? = nil, postAction: @escaping (CreditCard) -> () = {_ in}, statusBar: UIStatusBarStyle = .lightContent) -> CreditCardViewController {
        let controller = CreditCardViewController()
        controller.barStyle = statusBar
        let interactor = inject(controller: controller)
        interactor.card = card
        interactor.postAction = postAction
        return controller
    }
    
//    func scan(with delegate: CardIOPaymentViewControllerDelegate) {
//        let scan = CardIOPaymentViewController(paymentDelegate: delegate)!
//        controller.present(scan, animated: true, completion: nil)
//    }
    
    func pop() {
        controller.navigationController?.popViewController(animated: true)
    }
}

private func inject(controller: CreditCardViewController) -> CreditCardInteractor {
    let interactor = CreditCardInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = CreditCardRouter(controller)
    return interactor
}
