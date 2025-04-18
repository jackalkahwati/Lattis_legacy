//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 01.03.2021.
//

import Vapor
import Fluent

final class Ticket: Content, Model {
    static var schema: String = "tickets"
    
    @ID(custom: "ticket_id")
    var id: Int?
    
    @Enum(key: "category")
    var category: Category
    
    @Enum(key: "status")
    var status: Status
    
    @OptionalField(key: "assignee")
    var assignee: Int?
    
    @Field(key: "fleet_id")
    var fleetId: Int
    
    @Field(key: "date_created")
    var createdAt: UInt
    
    @Timestamp(key: "date_created", on: .create, format: .unixUInt)
    var createdAtDate: Date?
    
    @Timestamp(key: "date_resolved", on: .none, format: .unixUInt)
    var resolvedAt: Date?
    
    @Timestamp(key: "date_assigned", on: .none, format: .unixUInt)
    var assignedAt: Date?
    
    @OptionalField(key: "operator_id")
    var operatorId: Int?
    
    @OptionalParent(key: "bike_id")
    var vehicle: Bike?
    
    @OptionalField(key: "trip_id")
    var tripId: Int?
    
    @OptionalField(key: "operator_notes")
    var operatorNotes: String?
    
    @OptionalField(key: "rider_notes")
    var riderNotes: String?
}

extension Ticket {    
    enum Category: String, Codable {
        case parking_outside_geofence
        case issue_detected
        case damage_reported
        case service_due
        case reported_theft
        case maintenance_due
        case low_battery
        case potential_theft
        
        var id: String { rawValue }
    }
    
    enum Status: String, Codable {
        case resolved
        case created
        case assigned
    }
    
    struct Patch: Codable {
        let assignee: Int?
        let notes: String?
        let status: Status?
        
        func patch(_ ticket: Ticket) {
            if let id = assignee {
                ticket.assignee = id
                ticket.status = .assigned
                ticket.assignedAt = Date()
            }
            if let n = notes {
                ticket.operatorNotes = n
            }
            if let s = status {
                ticket.status = s
                if s == .resolved {
                    ticket.resolvedAt = Date()
                }
            }
        }
    }
    
    struct Create: Codable {
        let category: Category
        var assignee: Int?
        var fleetId: Int
        var vehicle: Int
        var notes: String?
        var createdBy: Int
    }
    
    convenience init(_ cr: Create) {
        self.init()
        self.category = cr.category
        self.assignee = cr.assignee
        self.fleetId = cr.fleetId
        self.$vehicle.id = cr.vehicle
        self.operatorNotes = cr.notes
        self.operatorId = cr.createdBy
        if cr.assignee == nil {
            self.status = .created
        } else {
            self.status = .assigned
        }
    }
}


public struct UIntTimestampFormat: TimestampFormat {
    public typealias Value = UInt

    public func parse(_ value: UInt) -> Date? {
        .init(timeIntervalSince1970: TimeInterval(value))
    }

    public func serialize(_ date: Date) -> UInt? {
        UInt(date.timeIntervalSince1970)
    }
}

extension TimestampFormatFactory {
    public static var unixUInt: TimestampFormatFactory<UIntTimestampFormat> {
        .init {
            UIntTimestampFormat()
        }
    }
}

