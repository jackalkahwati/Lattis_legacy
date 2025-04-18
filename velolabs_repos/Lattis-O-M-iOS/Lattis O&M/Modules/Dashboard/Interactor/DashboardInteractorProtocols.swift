//
//  DashboardDashboardInteractorProtocols.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

struct Dashboard {
    enum Tab {
        case tickets, locks
    }
}

protocol DashboardInteractorInput: LocksInteractorDelegate {
    var numberOfTabs: Int {get}
    var tabTitles: [String] {get}
    
    func viewLoaded()
    func viewDidApear()
    func scanQRCode()
    func tab(for index: Int) -> UIViewController?
}

protocol DashboardInteractorOutput: class {
    func change(vendor: Lock.Vendor)
}
