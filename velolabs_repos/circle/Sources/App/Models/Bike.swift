
import Vapor
import Fluent

final class Bike: Model, Content {
        
    static var schema: String = "bikes"
    
    @ID(custom: "bike_id")
    var id: Int?
    
    @OptionalField(key: "bike_name")
    var name: String?
    
    @OptionalField(key: "lock_id")
    var lockId: Int?
    
    @OptionalParent(key: "lock_id")
    var ellipse: Lock?
    
    @OptionalField(key: "qr_code_id")
    var qrCode: Int?
    
    @OptionalField(key: "fleet_id")
    var fleetId: Int?
    
    @OptionalParent(key: "fleet_id")
    var fleet: Fleet?
    
    @OptionalField(key: "latitude")
    var latitude: Double?

    @OptionalField(key: "longitude")
    var longitude: Double?
    
    @OptionalParent(key: "bike_group_id")
    var group: Group?
    
    @OptionalEnum(key: "status")
    var status: Status?
    
    @Enum(key: "current_status")
    var usage: Usage
    
    @OptionalEnum(key: "maintenance_status")
    var maintenance: Maintenance?
    
    @OptionalField(key: "bike_battery_level")
    var batteryLevel: Double?
    
    @Children(for: \.$bike)
    var things: [Thing]
}

extension Bike {
    enum Status: String, Codable {
        case active
        case inactive
        case suspended
        case deleted
    }
    enum Usage: String, Codable {
        case lock_assigned
        case controller_assigned
        case lock_not_assigned
        case parked
        case on_trip
        case damaged
        case reported_stolen
        case stolen
        case under_maintenance
        case total_loss
        case defleeted
        case defleet
        case collect
        case balancing
        case reserved
        case transport
    }
    enum Maintenance: String, Codable {
        case shop_maintenance
        case field_maintenance
        case parked_outside_geofence
        case issue_detected
        case damage_reported
        case reported_theft
    }
    
    struct Patch: Codable {
        let status: Status?
        let usage: Usage?
        let maintenance: Maintenance?
        let coordinate: Coordinate?
    }
    
    enum Filter {        
        enum Maintenance: String {
            case lowBattery = "low-battery"
        }
    }
}
