//
//  ParkingHub.swift
//  
//
//  Created by Ravil Khusainov on 24.11.2020.
//

import Foundation

public struct ParkingHub: Codable {
    public let hubId: Int
    public let latitude: Double
    public let longitude: Double
    public let hubName: String
    public let ports: [Port]
    public let localHubStatus: String
    public let remoteHubStatus: String
    public let make: String
    public let model: String
    public let integration: String
    public let description: String?
    public let image: URL?
}

public extension ParkingHub {
    struct Port: Codable {
        public let portId: Int
        public let portStatus: String
        public let portNumber: Int
        public let portChargingStatus: Int
    }
}
