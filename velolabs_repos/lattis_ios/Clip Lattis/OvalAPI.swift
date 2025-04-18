//
//  OvalAPI.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 26.01.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import UIKit

enum OvalAPI {
    
    static fileprivate(set) var agent = NetworkAgent(Env.ovalURL)
    
    static func logOut() {
        agent.authorization = nil
    }
    
    static func logIn(_ token: String) {
        agent.authorization = .lattis(token)
    }
}

extension OvalAPI {
    static func startTrip(info: Trip.Start) async throws -> TripRequest {
        try await agent.run(.post(info, path: "api/bikes/create-booking"))
        let envelope: Envelope<TripRequest> = try await agent.run(.post(info, path: "api/trips/start-trip"))
        return envelope.payload
    }
    
    static func endTrip(info: Trip.End) async throws {
        try await agent.run(.post(info, path: "api/trips/end-trip"))
    }
    
    static func iotStatus(id: Int, controller: String) async throws -> Vehicle.Status {
        let payload: Envelope<Vehicle.Status> = try await agent.run(.get("api/bikes/\(id)/iot/status", queryItems: [.init(name: "controller_key", value: controller)]))
        return payload.payload
    }
    
    static func lock(id: Int, controller: String) async throws {
        struct Controllers: Codable {
            let controller_key: [String]
        }
        try await agent.run(.post(Controllers(controller_key: [controller]), path: "api/bikes/\(id)/user-lock"))
    }
    
    static func unlock(id: Int, controller: String) async throws {
        struct Controllers: Codable {
            let controller_key: [String]
        }
        try await agent.run(.post(Controllers(controller_key: [controller]), path: "api/bikes/\(id)/user-unlock"))
    }
    
    static func appStatus() async throws -> AppStatus {
        let payload: Envelope<AppStatus> = try await agent.run(.post(UIDevice.current, path: "api/users/get-current-status"))
        return payload.payload
    }
    
    static func trip(id: Int) async throws -> Legacy.Trip {
        struct Wrap: Codable {
            let trip: Legacy.Trip
        }
        let payload: Envelope<Wrap> = try await agent.run(.post(TripRequest(trip_id: id), path: "api/trips/get-trip-details"))
        return payload.payload.trip
    }
    
    fileprivate struct Bike: Codable {
        let bike_id: Int
    }
    
    struct TripRequest: Codable {
        let trip_id: Int
    }
    
    fileprivate struct Envelope<Value: Codable>: Codable {
        let payload: Value
    }
    
    fileprivate struct Empty: Codable {}
}

struct AppStatus: Codable {
    let trip: Trip.Status?
}

extension UIDevice: Encodable {
    var deviceAndSystem: String {
        return "\(modelName)(\(systemVersion))"
    }
    
    var systemNameAndVersion: String {
        return "\(systemName) \(systemVersion)"
    }
    
    var deviceLanguage: String {
        return "\(Locale.current.languageCode ?? "en")"
    }
    
    var modelName: String {
        var systemInfo = utsname()
        uname(&systemInfo)
        let machineMirror = Mirror(reflecting: systemInfo.machine)
        let identifier = machineMirror.children.reduce("") { identifier, element in
            guard let value = element.value as? Int8, value != 0 else { return identifier }
            return identifier + String(UnicodeScalar(UInt8(value)))
        }
        
        switch identifier {
        case "iPod5,1":                                 return "iPod Touch 5"
        case "iPod7,1":                                 return "iPod Touch 6"
        case "iPhone3,1", "iPhone3,2", "iPhone3,3":     return "iPhone 4"
        case "iPhone4,1":                               return "iPhone 4s"
        case "iPhone5,1", "iPhone5,2":                  return "iPhone 5"
        case "iPhone5,3", "iPhone5,4":                  return "iPhone 5c"
        case "iPhone6,1", "iPhone6,2":                  return "iPhone 5s"
        case "iPhone7,2":                               return "iPhone 6"
        case "iPhone7,1":                               return "iPhone 6 Plus"
        case "iPhone8,1":                               return "iPhone 6s"
        case "iPhone9,1", "iPhone9,3":                  return "iPhone 7"
        case "iPhone9,2", "iPhone9,4":                  return "iPhone 7 Plus"
        case "i386", "x86_64":                          return "Simulator"
        case "iPad2,1", "iPad2,2", "iPad2,3", "iPad2,4":return "iPad 2"
        case "iPad3,1", "iPad3,2", "iPad3,3":           return "iPad 3"
        case "iPad3,4", "iPad3,5", "iPad3,6":           return "iPad 4"
        case "iPad4,1", "iPad4,2", "iPad4,3":           return "iPad Air"
        case "iPad5,3", "iPad5,4":                      return "iPad Air 2"
        case "iPad2,5", "iPad2,6", "iPad2,7":           return "iPad Mini"
        case "iPad4,4", "iPad4,5", "iPad4,6":           return "iPad Mini 2"
        case "iPad4,7", "iPad4,8", "iPad4,9":           return "iPad Mini 3"
        case "iPad5,1", "iPad5,2":                      return "iPad Mini 4"
        case "iPad6,7", "iPad6,8":                      return "iPad Pro"
        case "AppleTV5,3":                              return "Apple TV"
        default:                                        return identifier
        }
    }
    
    enum CodingKeys: String, CodingKey {
        case device_model
        case device_os
        case device_language
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(modelName, forKey: .device_model)
        try container.encode(systemNameAndVersion, forKey: .device_os)
        try container.encode(deviceLanguage, forKey: .device_language)
    }
}
