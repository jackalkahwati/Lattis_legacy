//
//  Ticket.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 12/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreLocation

public struct Ticket: Codable, LocationPresentable {
    var latitude: Double?
    var longitude: Double?
    public let ticketId: Int
    public let fleetId: Int
    public let bikeId: Int
    public let operatorId: Int?
    public let lockId: Int?
    public var assigneeId: Int?
    public var name: String?
    public var created: Date
    public var resolved: Date?
    public var riderNotes: String?
    public var maintenanceNotes: String?
    public var operatorNotes: String?
    public var userPhoto: URL?
    public var operatorPhoto: URL?
    public var categoryString: String?
    public var status: Status?
    public var type: TicketType?
    public var bikeName: String?
    public var lockName: String?
    public var ticketStatus: String?
    public var currentStatus: Bike.CurrentStatus?
    public var bikeStatus: Bike.Status?
    
    public var isNew: Bool? = nil
    
    enum CodingKeys: String, CodingKey {
        case latitude
        case longitude
        case ticketId
        case fleetId
        case bikeId
        case operatorId
        case lockId
        case assigneeId = "assignee"
        case name
        case created = "ticketDateCreated"
        case resolved = "dateResolved"
        case riderNotes
        case maintenanceNotes
        case operatorNotes
        case userPhoto
        case operatorPhoto
        case categoryString = "category"
        case status
        case type
        case bikeName
        case lockName
        case ticketStatus
        case currentStatus
        case bikeStatus
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(ticketId, forKey: .ticketId)
    }
    
    var category: Category? {
        set {
            categoryString = newValue?.rawValue
        }
        get {
            switch categoryString {
            case "frame_scratched"?:
                return .damage_reported
            default:
                return Category(string: categoryString)
            }
        }
    }
}

public extension Ticket {
    struct Create: Encodable {
        let bikeId: Int
        let lockId: Int
        let fleetId: Int
        let category: Category
        let operatorPhoto: URL?
        let maintenanceNotes: String
    }
    public enum TicketType: String, Codable {
        case maintenance, crash, theft, bike, trip
    }
    
    public enum Status: String, Codable {
        case created, assigned, resolved, in_workshop
    }
    
    enum Category: String, Codable {
        case reported_theft
        case damage_reported
        case service_due
        case parking_outside_geofence
        
        var displayTitle: String {
            return rawValue.localized()
        }
    }
    
    var displayTitle: String {
        var result = lockName ?? ""
        if result.isEmpty {
            result = bikeName ?? ""
        } else if let bikeName = bikeName {
            result += ": \(bikeName)"
        }
        return result
    }
    
    public var displayStatus: String {
        guard let curr = currentStatus, let stat = bikeStatus else { return "" }
        switch curr {
        case .lockAssigned:
            return "Lock assigned"
        case .lockNotAssigned:
            return "Lock not assigned"
        case .onTrip:
            return "On trip"
        case .damageReported:
            return "Damage reported"
        case .underMaintenance:
            return "Under maintenance"
        case .totalLoss:
            return "Total loss"
        case .stolen where stat == .deleted:
            return "Stollen"
        case .reportedStolen where stat == .suspended:
            return "Reported Slolen"
        case .stolen where stat == .suspended:
            return "Reported Slolen"
        default:
            return curr.rawValue.capitalized
        }
    }
}

extension RawRepresentable where RawValue == String {
    init?(string: String?) {
        guard let string = string else { return nil }
        self.init(rawValue: string)
    }
}
