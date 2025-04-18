//
//  HubDetailsViewModel.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 31.01.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Combine
import Model

final class HubDetailsViewModel: ObservableObject {
    
    @Published private(set) var ports: [Hub.Port] = []
    @Published var port: Hub.Port?
    let hub: Hub
    let booked: (Hub.Port, Booking) -> Void
    let discount: Double?
    
    init(_ hub: Hub, discount: Double?, booked: @escaping (Hub.Port, Booking) -> Void) {
        let ports = hub.ports ?? []
        let port = ports.filter({ $0.portId != nil })
        self.booked = booked
        self.hub = hub
        self.ports = port
        self.discount = discount
    }
    
    var hubName: String {
        hub.hubName
    }
    
    var fleetName: String {
        hub.fleet.name ?? "No fleet"
    }
}

extension Hub {
    static var mock: Hub {
        .init(hubId: 0, model: "Local", make: "Desk hub", hubName: "Piltover", description: "Original Netflix series", latitude: 42.3344, longitude: 54.532, type: "parking_station", fleet: .init(fleetId: 0, name: "Lattis Fleet", email: nil, customer: nil, logo: nil, legal: nil, requirePhoneNumber: nil, type: .publicFree, reservationSettings: nil), integration: .custom, bikes: [], ports: [
            .init(portId: 0, portUuid: UUID().uuidString, portVehicleUuid: nil, portNumber: 1, portQrCode: nil, portEquipment: nil, portLocked: nil, equipment: nil),
            .init(portId: 1, portUuid: UUID().uuidString, portVehicleUuid: nil, portNumber: 2, portQrCode: nil, portEquipment: nil, portLocked: nil, equipment: nil),
            .init(portId: 3, portUuid: UUID().uuidString, portVehicleUuid: nil, portNumber: 3, portQrCode: nil, portEquipment: nil, portLocked: nil, equipment: nil)
        ], promotions: [], pricingOptions: [], equipment: nil)
    }
}

extension Hub.Port: Identifiable {
    public var id: Int { portId ?? 0 }
}
