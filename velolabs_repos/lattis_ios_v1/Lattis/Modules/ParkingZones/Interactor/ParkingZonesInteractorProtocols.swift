//
//  ParkingZonesParkingZonesInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/05/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

protocol ParkingZonesInteractorInput {
    func viewLoaded()
}

protocol ParkingZonesInteractorOutput: BaseInteractorOutput {
    func show(zones: [ParkingZone])
    func show(spots: [Parking])
}
