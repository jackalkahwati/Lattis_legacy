//
//  Thing.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.03.2021.
//

import CoreLocation

struct Thing: Codable, Identifiable {
    init(metadata: Thing.Metadata, name: String? = nil) {
        self.metadata = metadata
        self.name = name
    }
    
    let metadata: Metadata
    let name: String?
    
    var id: Int { metadata.id }
    var deviceType: DeviceType { DeviceType(rawValue: metadata.deviceType) ?? .unknown }
    var batteryLevel: String? {
        guard let level = metadata.batteryLevel else { return nil }
        return "\(level)%"
    }
}

extension Thing {
    struct Metadata: Codable {
        let id: Int
        let key: String
        let qrCode: String?
        let vendor: String
        let make: String?
        let model: String?
        let deviceType: String
        let latitude: CLLocationDegrees?
        let longitude: CLLocationDegrees?
        let fleetId: Int?
        let fwVersion: String?
        let batteryLevel: Int?
    }
    
    enum DeviceType: String, Codable {
        case lock
        case IoT
        case adapter
        case unknown
        
        var imageName: String {
            switch self {
            case .IoT: return "simcard"
            case .lock: return "lock"
            case .adapter: return "personalhotspot"
            case .unknown: return "questionmark"
            }
        }
    }
    
    enum Vendor: String, Codable {
        case segway = "Segway"
        case segwayEU = "Segway IoT EU"
        case grow = "Grow"
        case linka = "Linka IoT"
        case geotab = "Geotab IoT"
        case manualLock = "Manual Lock"
        case acton = "ACTON"
        case ellipse = "Ellipse"
        case axa = "AXA"
        case invers = "Invers"
    }
    
    struct EllipseLock: Codable {
        let id: Int
        let macId: String
        let name: String?
        let fleetId: Int?
        let fwVersion: String?
        let batteryLevel: Double?
    }
    
    init(ellipse: EllipseLock) {
        self.init(metadata: .init(ellipse: ellipse), name: ellipse.name)
    }
    
    struct Status: Codable {
        let online: Bool?
        let locked: Bool
        let coordinate: CLLocationCoordinate2D?
        let batteryLevel: Int?
        let charging: Bool?
    }
    
    enum Failure: Error {
        case noIntegration(String)
    }
    
    var batteryCovered: Bool {
        guard let vendor = try? Vendor(metadata) else { return false }
        switch vendor {
        case .segway, .segwayEU:
            return true
        default:
            return false
        }
    }
    
    var lightControl: Bool {
        guard let vendor = try? Vendor(metadata) else { return false }
        switch vendor {
        case .segway, .segwayEU:
            return true
        default:
            return false
        }
    }
    
    var soundControl: Bool {
        guard let vendor = try? Vendor(metadata) else { return false }
        switch vendor {
        case .segway, .segwayEU, .grow:
            return true
        default:
            return false
        }
    }
}

extension Thing.Metadata {
    init(ellipse: Thing.EllipseLock) {
        id = ellipse.id
        key = ellipse.macId
        qrCode = nil
        vendor = "Ellipse"
        make = nil
        model = ellipse.name
        deviceType = "lock"
        latitude = nil
        longitude = nil
        fleetId = ellipse.fleetId
        batteryLevel = ellipse.batteryLevel == nil ? nil : Int(ellipse.batteryLevel!)
        fwVersion = ellipse.fwVersion
    }
}

extension Thing.Vendor {
    init(_ thing: Thing.Metadata) throws {
        guard let vendor = Thing.Vendor(rawValue: thing.vendor) else { throw Thing.Failure.noIntegration(thing.vendor) }
        self = vendor
    }
}

extension Thing: Hashable {
    static func == (lhs: Thing, rhs: Thing) -> Bool {
        lhs.id == rhs.id
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}

extension CLLocationCoordinate2D: Codable {
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let lat = try container.decode(CLLocationDegrees.self, forKey: .latitude)
        let lon = try container.decode(CLLocationDegrees.self, forKey: .longitude)
        self.init(latitude: lat, longitude: lon)
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(latitude, forKey: .latitude)
        try container.encode(longitude, forKey: .longitude)
    }
    
    enum CodingKeys: String, CodingKey {
        case latitude, longitude
    }
}

extension CLLocationCoordinate2D: Equatable {
    public static func == (lhs: CLLocationCoordinate2D, rhs: CLLocationCoordinate2D) -> Bool {
        lhs.latitude == rhs.latitude && lhs.longitude == rhs.longitude
    }
}
