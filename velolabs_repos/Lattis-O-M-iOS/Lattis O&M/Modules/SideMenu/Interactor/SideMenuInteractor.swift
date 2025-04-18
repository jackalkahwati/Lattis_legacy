//
//  SideMenuSideMenuInteractor.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 05/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import Oval
import LattisSDK

class SideMenuInteractor: NSObject {
    weak var view: SideMenuInteractorOutput!
    var router: SideMenuRouter!
    
    fileprivate var handler: StorageHandler?
    fileprivate let network: FleetNetwork
    fileprivate let storage: FleetsStorage
    
    init(network: FleetNetwork = Session.shared, storage: FleetsStorage = CoreDataStack.shared) {
        self.network = network
        self.storage = storage
    }
}

extension SideMenuInteractor: SideMenuInteractorInput {
    func viewLoaded() {
        network.getFleets { [weak self] (result) in
            switch result {
            case .success(let fleets):
                self?.storage.save(fleets, update: true) {}
            case .failure(let error):
                self?.view.show(error: error)
            }
        }
        
        handler = storage.subscribe() { [weak self] (fleets) in
            self?.view.show(fleets: fleets)
        }
    }
    
    func select(fleet: Fleet) {
        var new = fleet
        new.isCurrent = true
        var fleets = [new]
        if var current = storage.currentFleet {
            current.isCurrent = false
            fleets.append(current)
        }
        storage.save(fleets, update: false) {
            self.router.open(fleet: fleet)
            EllipseManager.shared.clean()
        }
    }
}
