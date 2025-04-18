//
//  CreateTicketCreateTicketRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 28/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class CreateTicketRouter: BaseRouter {
    class func instantiate(with bike: Bike) -> CreateTicketViewController {
        let controller = UIStoryboard.bikeSearch.instantiateViewController(withIdentifier: "create") as! CreateTicketViewController
        inject(controller: controller).bike = bike
        return controller
    }
}

private func inject(controller: CreateTicketViewController) -> CreateTicketInteractor {
    let interactor = CreateTicketInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = CreateTicketRouter(controller)
    return interactor
}
