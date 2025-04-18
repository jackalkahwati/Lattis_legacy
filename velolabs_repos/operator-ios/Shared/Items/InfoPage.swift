//
//  InfoPage.swift
//  Operator
//
//  Created by Ravil Khusainov on 09.03.2021.
//

import Foundation
import CoreLocation


enum InfoPage: Identifiable {
    case ticket(Ticket)
    case vehicle(Vehicle)
    case equipment([Thing])
    case map(CLLocationCoordinate2D)
    case device(Thing)
    case tickets(Vehicle)
    
    var id: String {
        switch self {
        case .ticket: return "Ticket"
        case .vehicle: return "Vehicle"
        case .equipment: return "Equipment"
        case .map: return "Map"
        case .device: return "Device"
        case .tickets: return "Tickets"
        }
    }
}

extension String: Identifiable {
    public var id: String { self }
}

extension InfoPage: Hashable {
    static func ==(lhs: InfoPage, rhs: InfoPage) -> Bool {
        lhs.hashValue == rhs.hashValue
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}
