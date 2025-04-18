//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 2022-05-03.
//

import Foundation
import CoreLocation
import Model

public enum Rental: Codable {
    case bike(Bike)
    case hub(Hub)
    case port(Hub.Port, Hub)
    case invalid
}

extension Rental {
    struct Port: Decodable {
        let portId: Int
        let portUuid: String
        let portVehicleUuid: String?
        let portNumber: Int?
        let portQrCode: String?
        let portEquipment: Int?
        let portLocked: Bool?
        let equipment: Thing?
        let fleet: Model.Fleet
        let hub: ProxyHub
        let promotions: [Model.Promotion]?
        let pricingOptions: [Pricing]?
    }

    struct ProxyHub: Decodable {
        let hubId: Int
        let model: String?
        let make: String?
        let name: String
        let description: String?
        let latitude: CLLocationDegrees
        let longitude: CLLocationDegrees
        let type: String
        let integration: Hub.Integration
        let equipment: Thing?
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        if let bike = try container.decodeIfPresent(Bike.self, forKey: .bike) {
            self = .bike(bike)
            return
        }
        if let hub = try container.decodeIfPresent(Hub.self, forKey: .hub) {
            self = .hub(hub)
            return
        }
        if let port = try container.decodeIfPresent(Port.self, forKey: .port) {
            self = .port(.init(port), .init(port))
            return
        }
        self = .invalid
    }

    enum CodingKeys: String, CodingKey {
        case bike
        case hub
        case port
    }
}

extension Hub {
    init(_ port: Rental.Port) {
        let hub = port.hub
        self.init(hubId: hub.hubId, model: hub.model, make: hub.make, hubName: hub.name, description: hub.description, latitude: hub.latitude, longitude: hub.longitude, type: hub.type, fleet: port.fleet, integration: hub.integration, bikes: [], ports: [], promotions: port.promotions ?? [], pricingOptions: port.pricingOptions ?? [], equipment: port.equipment ?? hub.equipment)
    }
}

extension Hub.Port {
    init(_ port: Rental.Port) {
        self.init(portId: port.portId, portUuid: port.portUuid, portVehicleUuid: port.portVehicleUuid, portNumber: port.portNumber ?? 0, portQrCode: port.portQrCode, portEquipment: port.portEquipment, portLocked: port.portLocked, equipment: port.equipment, qrCode: port.portQrCode)
    }
}
