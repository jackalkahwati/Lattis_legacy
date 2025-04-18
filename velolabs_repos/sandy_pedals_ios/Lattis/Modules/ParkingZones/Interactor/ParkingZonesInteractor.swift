//
//  ParkingZonesParkingZonesInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/05/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation
import Oval

class ParkingZonesInteractor {
    weak var view: ParkingZonesInteractorOutput!
    var router: ParkingZonesRouter!
    
    var fleetId: Int!
    fileprivate var handleError: (Error) -> () = {_ in}
    fileprivate let network: ParkingNetwork
    init(network: ParkingNetwork = Session.shared) {
        self.network = network
        handleError = { [weak self] error in self?.view.show(error: error, file: #file, line: #line)}
    }
}

extension ParkingZonesInteractor: ParkingZonesInteractorInput {
    func viewLoaded() {
        network.getZones(fleet: fleetId) { [weak self] (result) in
            switch result {
            case .success(let zones):
                self?.view.show(zones: zones)
            case .failure(let error):
                self?.handleError(error)
            }
        }
        
        network.getSpots(fleet: fleetId) { [weak self] (result) in
            switch result {
            case .success(let parkings):
                guard parkings.isEmpty == false else { return }
                self?.view.show(spots: parkings)//.filter({ $0.parkingId != 21 && $0.parkingId != 33 && $0.parkingId != 61 }))
            case .failure(let error):
                self?.handleError(error)
            }
        }
    }
}
