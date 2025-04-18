//
//  Bike.swift
//  
//
//  Created by Ravil Khusainov on 28.08.2020.
//

import Foundation

public struct Bike: Codable {
    public let bikeGroup: Group
    public let bikeBatteryLevel: Int?
    public let fleet: Fleet
    public let bikeName: String
    public let bikeId: Int
    public let latitude: Double?
    public let longitude: Double?
    public let qrCodeId: Int?
    public let qrCode: String?
    public let macId: String?
    public let controllers: [Thing]?
}

public extension Bike {
    struct Group: Codable {
        public let description: String
        public let bikeGroupId: Int
        public let operatorId: Int
        public let fleetId: Int
        public let type: BikeType
        public let pic: URL
        public let make: String
        public let model: String
    }
    
    enum BikeType: String, Codable {
        case regular
        case electric
        case kickScooter = "Kick Scooter"
        case locker
        case cart
        case kayak
        case moped
    }
}


