//
//  TicketsTicketsRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Localize_Swift

final class TicketsRouter: BaseRouter {
    class func instantiate(with delegate: TicketsInteractorDelegate) -> TicketsViewController {
        let controller = UIStoryboard.dashboard.instantiateViewController(withIdentifier: "tickets") as! TicketsViewController
        let interactor = inject(controller: controller)
        interactor.delegate = delegate
        controller.title = "tickets_title".localized()
        return controller
    }
    
    class func fileter(with interactor: TicketsInteractorInput) -> FilterViewController {
        let controller = UIStoryboard.dashboard.instantiateViewController(withIdentifier: "filter") as! FilterViewController
        controller.interactor = interactor
        controller.title = "filter_title".localized()
        return controller
    }
}

private func inject(controller: TicketsViewController) -> TicketsInteractor {
    let interactor = TicketsInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = TicketsRouter(controller)
    return interactor
}
