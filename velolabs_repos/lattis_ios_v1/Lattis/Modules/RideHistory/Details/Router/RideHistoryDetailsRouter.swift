//
//  RideHistoryDetailsRideHistoryDetailsRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 18/08/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class RideHistoryDetailsRouter: BaseRouter {
    class func instantiate(with trip: Trip) -> RideHistoryDetailsViewController {
        let controller = UIStoryboard(name: "RideHistory", bundle: nil).instantiateViewController(withIdentifier: "details") as! RideHistoryDetailsViewController
        inject(controller: controller).trip = trip
        return controller
    }
}

private func inject(controller: RideHistoryDetailsViewController) -> RideHistoryDetailsInteractor {
    let interactor = RideHistoryDetailsInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = RideHistoryDetailsRouter(controller)
    return interactor
}
