
import Vapor
import Fluent

final class Thing: Model, Content {
    static var schema: String = "controllers"
    
    @ID(custom: "controller_id")
    var id: Int?
    
    @Field(key: "device_type")
    var deviceType: String
    
    @Field(key: "vendor")
    var vendor: String
    
    @Field(key: "key")
    var key: String
    
    @Field(key: "qr_code")
    var qrCode: String?
    
    @Parent(key: "fleet_id")
    var fleet: Fleet
    
    @Field(key: "fleet_id")
    var fleetId: Int
    
    @OptionalParent(key: "bike_id")
    var bike: Bike?
    
    @OptionalField(key: "bike_id")
    var bikeId: Int?
}

extension Thing {
    struct Status: Content {
        let locked: Bool
        let online: Bool?
        let coordinate: Coordinate?
        let batteryLevel: Int?
        let charging: Bool?
        let lockStatus: LockStatus?
    }
    
    enum LockStatus: String, Codable {
        case locked
        case unlocked
        case locking
        case unlocting
    }
    
    enum Vendor: String, Codable {
        case segway = "Segway"
        case segwayEU = "Segway IoT EU"
        case grow = "Grow"
        case linka = "Linka IoT"
        case geotab = "Geotab IoT"
        case manualLock = "Manual Lock"
        case acton = "ACTON"
        case omniLock = "Omni"
        case comodule = "COMODULE Ninebot ES4"
        case duckt = "Duckt"
        case kisi = "Kisi"
    }
    
    struct Message: Content {
        let linka: LinkaAPI.Command?
    }
    
    final class New: Model, Content {
        static var schema: String = "controllers"
        
        @ID(custom: "controller_id")
        var id: Int?
        
        @Field(key: "device_type")
        var deviceType: String
        
        @Field(key: "vendor")
        var vendor: String
        
        @Field(key: "key")
        var key: String
        
        @Field(key: "qr_code")
        var qrCode: String?
        
        @Field(key: "fleet_id")
        var fleetId: Int
        
        @OptionalField(key: "bike_id")
        var bikeId: Int?
    }
}

struct Coordinate: Content {
    let latitude: Double
    let longitude: Double
}

extension Thing.Vendor {
    init(_ thing: Thing) throws {
        guard let vendor = Thing.Vendor(rawValue: thing.vendor) else { throw Abort(.conflict, reason: "No integration for \(thing.vendor)") }
        self = vendor
    }
}
