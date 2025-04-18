//
//  DashboardDashboardRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LGSideMenuController

final class DashboardRouter: BaseRouter {
    class func instantiate(with fleet: Fleet) -> DashboardViewController {
        let controller = UIStoryboard.main.instantiateViewController(withIdentifier: "DashboardViewController") as! DashboardViewController
        let interactor = inject(controller: controller)
        interactor.fleet = fleet
        return controller
    }
    
    class func navigation() -> UIViewController {
        let controller = UIStoryboard.main.instantiateViewController(withIdentifier: "menu")
        return controller
    }
    
    func openDispatch(with lock: Lock) {
        let dispatch = DispatchRouter.instantiate(with: lock, fromRoot: true)
        self.controller.navigationController?.pushViewController(dispatch, animated: true)
    }
    
    func openSettings(with lock: Lock) {
        let settings = SettingsRouter.instantiate(with: lock)
        self.controller.navigationController?.pushViewController(settings, animated: true)
    }
    
    func details(for ticket: Ticket) {
        let details = TicketDetailsRouter.instantiate(with: ticket)
        controller.navigationController?.pushViewController(details, animated: true)
    }
    
    func searchBikes() {
        let search = BikeSearchRouter.instantiate()
        controller.navigationController?.pushViewController(search, animated: true)
    }
    
    func showMenu() {
        controller?.sideMenuController?.showLeftViewAnimated(sender: self)
    }
    
    func tab(_ type: Dashboard.Tab, delegate: TicketsInteractorDelegate & LocksInteractorDelegate) -> UIViewController {
        switch type {
        case .tickets:
            return TicketsRouter.instantiate(with: delegate)
        case .locks:
            return LocksRouter.instantiate(with: delegate, dashboard: controller)
        }
    }
}

private func inject(controller: DashboardViewController) -> DashboardInteractor {
    let interactor = DashboardInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = DashboardRouter(controller)
    return interactor
}

final class DashboardConfigurator: NSObject {
    @IBOutlet weak var dashboard: DashboardViewController!
    override func awakeFromNib() {
        super.awakeFromNib()
        _ = inject(controller: dashboard)
    }
}
