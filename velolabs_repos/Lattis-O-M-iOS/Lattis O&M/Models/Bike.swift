//
//  Bike.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 26/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

public struct Bike: Decodable {
    let bikeId: Int
    let qrCodeId: Int?
    let fleetId: Int
    let lockId: Int?
    var name: String?
    var status: Status
    var lock: Ellipse? = nil
    
    enum CodingKeys: String, CodingKey {
        case bikeId
        case lockId
        case fleetId
        case qrCodeId
        case name = "bikeName"
        case status
    }
}

extension Bike {
    public enum Status: String, Codable {
        case active, inactive, suspended, deleted
    }
    
    public enum CurrentStatus: String, Codable {
        case controllerAssigned = "controller_assigned"
        case lockAssigned = "lock_assigned"
        case lockNotAssigned = "lock_not_assigned"
        case parked
        case onTrip = "on_trip"
        case damageReported = "damage_reported"
        case damaged
        case stolen
        case reportedStolen = "reported_stolen"
        case underMaintenance = "under_maintenance"
        case totalLoss = "total_loss"
        case defleeted
    }
}
