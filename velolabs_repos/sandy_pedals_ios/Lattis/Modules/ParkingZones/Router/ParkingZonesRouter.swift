//
//  ParkingZonesParkingZonesRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/05/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class ParkingZonesRouter: BaseRouter {
    static func navigation(with fleetId: Int) -> UINavigationController {
        let controller = ParkingZonesViewController()
        let interactor = inject(controller: controller)
        interactor.fleetId = fleetId
        return UINavigationController(rootViewController: controller, style: .white)
    }
}

private func inject(controller: ParkingZonesViewController) -> ParkingZonesInteractor {
    let interactor = ParkingZonesInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = ParkingZonesRouter(controller)
    return interactor
}
