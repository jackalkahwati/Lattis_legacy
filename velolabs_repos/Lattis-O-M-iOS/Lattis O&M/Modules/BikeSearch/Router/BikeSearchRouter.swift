//
//  BikeSearchBikeSearchRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 28/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class BikeSearchRouter: BaseRouter {
    class func instantiate() -> BikeSearchViewController {
        let controller = UIStoryboard.bikeSearch.instantiateInitialViewController() as! BikeSearchViewController
        inject(controller: controller)
        return controller
    }
    
    func createTicket(for bike: Bike) {
        let create = CreateTicketRouter.instantiate(with: bike)
        controller.navigationController?.pushViewController(create, animated: true)
    }
}

private func inject(controller: BikeSearchViewController) {
    let interactor = BikeSearchInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = BikeSearchRouter(controller)
}
