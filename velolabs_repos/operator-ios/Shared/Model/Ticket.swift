//
//  Ticket.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import Foundation

struct Ticket: Codable, Identifiable {
    var metadata: Metadata
    var assignee: FleetOperator
    var vehicle: Vehicle?
    
    var id: Int { metadata.id }
    func createdAt(_ formatter: DateFormatter = .default) -> String {
        formatter.string(from: metadata.createdAt)
    }
    
    init(metadata: Metadata, assignee: FleetOperator = .unassigned) {
        self.metadata = metadata
        self.assignee = assignee
        if let meta = metadata.vehicle {
            self.vehicle = .init(meta)
        } else {
            self.vehicle = nil
        }
    }
}

extension Ticket {
    struct Metadata: Codable {
        let id: Int
        let category: Category
        var status: Status
        let createdAt: Date
        var assignee: Int?
        let fleetId: Int
        let operatorId: Int?
        let tripId: Int?
        let vehicle: Vehicle.Metadata?
        var operatorNotes: String?
        let riderNotes: String?
    }
    
    enum Category: String, Codable, Identifiable, CaseIterable {
        case parking_outside_geofence
        case issue_detected
        case damage_reported
        case service_due
        case reported_theft
        case low_battery
        case potential_theft
        
        var id: String { rawValue }
        
        var imageName: String {
            switch self {
            case .damage_reported: return "bandage"
            case .service_due: return "wrench.and.screwdriver.fill"
            case .reported_theft: return "lock.shield"
            case .parking_outside_geofence: return "map"
            case .low_battery: return "battery.25"
            default: return "exclamationmark.triangle"
            }
        }
    }
    
    enum Status: String, Codable {
        case resolved
        case created
        case assigned
    }
    
    struct Patch: Codable {
        internal init(assignee: Int? = nil, notes: String? = nil, status: Ticket.Status? = nil) {
            self.assignee = assignee
            self.notes = notes
            self.status = status
        }
        
        let assignee: Int?
        let notes: String?
        let status: Status?
    }
    
    struct Create: Codable {
        let category: Category
        var assignee: Int?
        var fleetId: Int
        var vehicle: Int
        var notes: String?
        var createdBy: Int
    }
}

extension Ticket: Hashable {
    static func == (lhs: Ticket, rhs: Ticket) -> Bool {
        lhs.hashValue == rhs.hashValue
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(metadata.hashValue)
        hasher.combine(vehicle?.hashValue)
    }
}

extension Ticket.Metadata: Hashable {
    
}
