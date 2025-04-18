//
//  BikeInfoBikeInfoRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 20/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import SafariServices

class BikeInfoRouter {
    class func navigation(configure: (BikeInfoInteractor) -> ()) -> UINavigationController {
        let controller = UIStoryboard(name: "BikeInfo", bundle: nil).instantiateViewController(withIdentifier: "bikeInfo") as! BikeInfoViewController
        let interactor = inject(controller: controller)
        configure(interactor)
        return UINavigationController(rootViewController: controller, style: .white)
    }
    
    fileprivate weak var controller: UIViewController!
    init(_ controller: UIViewController) {
        self.controller = controller
    }
    
    func openTems(with link: URL) {
        let terms = SFSafariViewController(url: link)
        controller.present(terms, animated: true, completion: nil)
    }
    
    func openZones(of fleet: Int) {
        let zones = ParkingZonesRouter.navigation(with: fleet)
        controller.present(zones, animated: true, completion: nil)
    }
    
    func addCreditCard(with action: @escaping (CreditCard) -> ()) {
        let cardController = CreditCardRouter.instantiate(postAction: action, statusBar: .default)
        controller.navigationController?.pushViewController(cardController, animated: true)
    }
}

private func inject(controller: BikeInfoViewController) -> BikeInfoInteractor {
    let interactor = BikeInfoInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = BikeInfoRouter(controller)
    return interactor
}
