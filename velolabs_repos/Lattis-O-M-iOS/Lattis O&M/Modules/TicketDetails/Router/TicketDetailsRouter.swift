//
//  TicketDetailsTicketDetailsRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 27/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class TicketDetailsRouter: BaseRouter {
    class func instantiate(with ticket: Ticket) -> TicketDetailsViewController {
        let details = UIStoryboard.ticketDetails.instantiateViewController(withIdentifier: "details") as! TicketDetailsViewController
        let interactor = inject(controller: details)
        interactor.ticket = ticket
        return details
    }
    
    func present(viewControler: UIViewController) {
        controller.present(viewControler, animated: true, completion: nil)
    }
    
    func pop() {
        _ = controller.navigationController?.popToRootViewController(animated: true)
    }
    
    func show(action: UIAlertController) {
        controller.present(action, animated: true, completion: nil)
    }
}

private func inject(controller: TicketDetailsViewController) -> TicketDetailsInteractor {
    let interactor = TicketDetailsInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = TicketDetailsRouter(controller)
    return interactor
}
