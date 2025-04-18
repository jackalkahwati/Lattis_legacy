//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 13.01.2021.
//

import Foundation
import CoreLocation

public struct Hub: Codable {
    public init(hubId: Int, model: String?, make: String?, hubName: String, description: String?, latitude: CLLocationDegrees, longitude: CLLocationDegrees, type: String, fleet: Fleet, integration: Hub.Integration, bikes: [Hub.Bike], ports: [Hub.Port], promotions: [Promotion], pricingOptions: [Pricing], equipment: Thing?, qrCode: String? = nil, image: URL? = nil) {
        self.hubId = hubId
        self.model = model
        self.make = make
        self.hubName = hubName
        self.description = description
        self.latitude = latitude
        self.longitude = longitude
        self.type = type
        self.integration = integration
        self.bikes = bikes
        self.ports = ports
        self.fleet = fleet
        self.promotions = promotions
        self.pricingOptions = pricingOptions
        self.equipment = equipment
        self.qrCode = qrCode
        self.image = image
    }
    
    public let hubId: Int
    public let model: String?
    public let make: String?
    public let hubName: String
    public let description: String?
    public let latitude: CLLocationDegrees
    public let longitude: CLLocationDegrees
    public let type: String
    public let fleet: Fleet
    public let integration: Integration
    public let bikes: [Bike]?
    public let ports: [Port]?
    public let promotions: [Promotion]?
    public let pricingOptions: [Pricing]?
    public let equipment: Thing?
    public let qrCode: String?
    public let image: URL?
}

public extension Hub {
    
    struct Bike: Codable {
        public let bikeId: Int
        public let bikeName: String
        public let bikeUuid: String
    }
    
    struct Port: Codable {
        public init(portId: Int, portUuid: String, portVehicleUuid: String?, portNumber: Int, portQrCode: String?, portEquipment: Int?, portLocked: Bool?, equipment: Thing?, qrCode: String? = nil) {
            self.portId = portId
            self.portUuid = portUuid
            self.portVehicleUuid = portVehicleUuid
            self.portNumber = portNumber
            self.portQrCode = portQrCode
            self.portEquipment = portEquipment
            self.portLocked = portLocked
            self.equipment = equipment
            self.qrCode = qrCode
        }
        
        public let portId: Int?
        public let portUuid: String?
        public let portVehicleUuid: String?
        public let portNumber: Int?
        public let portQrCode: String?
        public let portEquipment: Int?
        public let portLocked: Bool?
        public let equipment: Thing?
        public let qrCode: String?
    }
    
    enum Integration: String, Codable {
        case custom
        case kuhmute
        case duckt
    }
    
    var imageURL: URL? {
        image ?? defaultImage
    }
    
    var defaultImage: URL? {
        URL(string: equipment == nil ? "https://hubs-and-fleets-images.s3.ca-central-1.amazonaws.com/default_open.png" : "https://hubs-and-fleets-images.s3.ca-central-1.amazonaws.com/default_closed.png")
    }
}

