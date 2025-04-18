//
//  DashboardDashboardInteractor.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import LattisSDK

class DashboardInteractor {
    weak var view: DashboardInteractorOutput!
    var vendor: Lock.Vendor = .ellipse
    var router: DashboardRouter! {
        didSet {
            controllers = tabs.map{ self.router.tab($0, delegate: self) }
        }
    }
    var fleet: Fleet!
    fileprivate let tabs: [Dashboard.Tab] = [.tickets, .locks]
    fileprivate var controllers: [UIViewController]!
}

extension DashboardInteractor: DashboardInteractorInput {
    var controller: UIViewController {
        return view as! UIViewController
    }
    
    func viewLoaded() {
        EllipseManager.shared.clean()
    }
    
    func viewDidApear() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            if CoreDataStack.shared.currentFleet == nil {
                self.router.showMenu()
            }
        }
    }
    
    func createTicket() {
        router.searchBikes()
    }
    
    var numberOfTabs: Int {
        return tabs.count
    }
    
    func tab(for index: Int) -> UIViewController? {
        return controllers[index]
    }
    
    var tabTitles: [String] {
        return controllers.compactMap { $0.title }
    }
    
    func scanQRCode() {
        guard let locks = controllers.last as? LocksViewController else {
            return
        }
        locks.interactor.scanQRCode()
    }
    
    func change(vendor: Lock.Vendor, filter: Lock.Filter) {
        guard vendor != self.vendor else { return }
        self.vendor = vendor
        controllers.removeLast()
        switch vendor {
        case .axa:
            controllers.append(AxaLocksViewController(filter, delegate: self))
        default:
            controllers.append(router.tab(.locks, delegate: self))
        }
        view.change(vendor: vendor)
    }
}

extension DashboardInteractor {
    func dispatch(lock: Lock) {
        router.openDispatch(with: lock)
    }
    
    func settings(lock: Lock) {
        router.openSettings(with: lock)
    }
}

extension DashboardInteractor: TicketsInteractorDelegate {
    func select(ticket: Ticket) {
        router.details(for: ticket)
    }
}
