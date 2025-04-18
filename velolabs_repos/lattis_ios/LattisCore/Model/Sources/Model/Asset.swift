
import Foundation

public enum Asset: Codable {
    case bike(Bike)
    case hub(Hub)
    case port(Hub.Port, Hub)
}

public extension Asset {
    var name: String {
        switch self {
        case .bike(let bike):
            return bike.bikeName
        case .hub(let hub):
            return hub.hubName
        case .port(let port, let hub):
            return "\(hub.hubName) #\(port.portNumber ?? 0)"
        }
    }
    
    var kind: String {
        switch self {
        case .bike(let bike):
            return bike.bikeGroup.type.rawValue
        case .hub:
            return "parking"
        case .port:
            return "parking"
        }
    }
    
    var fleetName: String {
        switch self {
        case .bike(let bike):
            return bike.fleet.name ?? "invalid"
        case .hub(let hub):
            return hub.fleet.name ?? "invalid"
        case .port(_ , let hub):
            return hub.fleet.name ?? "invalid"
        }
    }
    
    var isFree: Bool {
        let fleet: Fleet
        switch self {
        case .bike(let bike):
            fleet = bike.fleet
        case .hub(let hub):
            fleet = hub.fleet
        case .port(_, let hub):
            fleet = hub.fleet
        }
        return fleet.type == .privateFree || fleet.type == .publicFree
    }
    
    var equipment: Thing? {
        switch self {
        case .bike(let bike):
            return bike.controllers?.first
        case .hub(let hub):
            return hub.equipment
        case .port(let port, let hub):
            return port.equipment ?? hub.equipment
        }
    }
    
    var qrCode: String? {
        switch self {
        case .bike(let bike):
            return bike.qrCode
        case .hub(let hub):
            return hub.qrCode
        case .port(let port, let hub):
            return port.qrCode ?? hub.qrCode
        }
    }
    
    var queryId: (name: String, value: Int) {
        switch self {
        case .bike(let bike):
            return ("bike_id", bike.bikeId)
        case .hub(let hub):
            return ("hub_id", hub.hubId)
        case .port(let port, _):
            return ("port_id", port.portId ?? 0)
        }
    }
}

//public extension Rental {
//    enum CodingKeys: String, CodingKey {
//        case bike
//        case hub
//        case port
//    }
//    
//    public init(from decoder: Decoder) throws {
//        let container = try decoder.container(keyedBy: CodingKeys.self)
//    }
//}
