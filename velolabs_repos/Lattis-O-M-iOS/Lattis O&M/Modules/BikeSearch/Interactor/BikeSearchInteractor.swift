//
//  BikeSearchBikeSearchInteractor.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 28/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Oval

class BikeSearchInteractor {
    weak var view: BikeSearchInteractorOutput!
    var router: BikeSearchRouter!
    
    fileprivate var searchType: SearchType = .bike
    fileprivate let network: BikeNetwork
    fileprivate let storage: FleetsStorage
    fileprivate var bikes: [Bike] = []
    fileprivate let sort: (Bike, Bike) -> Bool = { lBike, rBike in
        guard let lName = lBike.name, let rName = rBike.name else { return true }
        return lName < rName
    }
    init(network: BikeNetwork = Session.shared, storage: FleetsStorage = CoreDataStack.shared) {
        self.network = network
        self.storage = storage
        
        guard let fleet = storage.currentFleet else { return }
        network.getBikes(for: fleet) { [weak self] result in
            switch result {
            case .success(let bikes):
                self?.bikes = bikes.filter({$0.lockId != nil && $0.qrCodeId != nil && $0.status == .active}).sorted(by: self!.sort)
                self?.view.show(bikes: self!.bikes)
            case .failure:
                break
            }
        }
    }
}

extension BikeSearchInteractor: BikeSearchInteractorInput {
    func select(searchType: SearchType) {
        self.searchType = searchType
    }
    
    func search(by term: String) {
        var filter: (Bike) -> Bool = { _ in return true }
        if term.isEmpty == false {
            switch searchType {
            case .bike:
                filter = { $0.name != nil && $0.name!.lowercased().contains(term.lowercased()) }
            default:
                break
            }
        }
        view.show(bikes: bikes.filter(filter))
    }
    
    func select(bike: Bike) {
        router.createTicket(for: bike)
    }
}
