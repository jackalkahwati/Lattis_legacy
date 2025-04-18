//
//  RideHistoryListRideHistoryListRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 18/08/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class RideHistoryListRouter: BaseRouter {
    static var navigation: UINavigationController {
        let controller = instantiate()
        return UINavigationController(rootViewController: controller, style: .blue)
    }
    
    class func instantiate() -> RideHistoryListViewController {
        let controller = UIStoryboard(name: "RideHistory", bundle: nil).instantiateViewController(withIdentifier: "list") as! RideHistoryListViewController
        inject(controller: controller)
        return controller
    }
    
    func details(for trip: Trip) {
        let details = RideHistoryDetailsRouter.instantiate(with: trip)
        controller.navigationController?.pushViewController(details, animated: true)
    }
}

private func inject(controller: RideHistoryListViewController) {
    let interactor = RideHistoryListInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = RideHistoryListRouter(controller)
}
