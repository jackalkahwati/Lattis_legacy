
import Vapor
import Fluent

final class Lock: Model, Content {
    static let schema: String = "locks"
    
    @ID(custom: "lock_id")
    var id: Int?
    
    @Field(key: "name")
    var name: String?
    
    @OptionalField(key: "mac_id")
    var macId: String?
    
    @OptionalField(key: "key")
    var key: String?
    
    @OptionalField(key: "fleet_id")
    var fleetId: Int?
    
    @OptionalField(key: "firmware_version")
    var fwVersion: String?
    
    @OptionalField(key: "battery_level")
    var batteryLevel: Double?
    
    @OptionalParent(key: "fleet_id")
    var fleet: Fleet?
}

final class PinCode: Model {
    static var schema: String = "pin_codes"
    
    @ID(custom: "pin_code_id")
    var id: Int?
    
    @Field(key: "code")
    var code: String?
    
    @Field(key: "lock_id")
    var lockId: Int?
    
    @Timestamp(key: "date", on: .none, format: .unixUInt)
    var resolvedAt: Date?
}

extension Lock {
    enum Pin: String, Content {
        case up, down, left, right
    }
}
