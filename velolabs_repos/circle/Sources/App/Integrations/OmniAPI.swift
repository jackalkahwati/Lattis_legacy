//
//  OmniAPI.swift
//  
//
//  Created by Ravil Khusainov on 01.09.2021.
//

import Vapor
import Fluent


enum OmniAPI {
    static let wait = 30
    static let cached = 1
    static func metadata() throws -> RequestBuilder {
        guard let apiKey = Environment.get("MAPTEX_API_KEY") else { throw Abort(.conflict, reason: "No MAPTEX_API_KEY found") }
        guard let baseUrl = Environment.get("OMNI_API_URL") else { throw Abort(.conflict, reason: "No OMNI_API_URL found")}
        return .init(headers: .init([
            ("MAPTEX-API-KEY", apiKey),
            ("Accept", "application/json"),
            ("Content-Type", "application/json"),
            ("Connection", "keep-alive")
        ]), baseUrl: baseUrl)
    }
    
    static func status(imei: String, req: Request) throws -> EventLoopFuture<Status> {
        try metadata()
            .build { url, headers in
                let uri = URI(string: url + "/devices/\(imei)/actions/current-status?cached=\(cached)&wait=\(wait)")
                return req.client.get(uri, headers: headers)
                    .flatMapThrowing { response in
                        let status = try response.content.decode(Status.self)
                        return status
                    }
            }
    }
    
    static func lock(imei: String, req: Request) throws -> EventLoopFuture<HTTPStatus> {
        try metadata()
            .build { url, headers in
                let uri = URI(string: url + "/devices/\(imei)/actions/stop-vehicle?cached=\(cached)&wait=\(wait)")
                return req.client.post(uri, headers: headers)
                    .transform(to: .ok)
            }
    }
    
    static func unlock(imei: String, req: Request) throws -> EventLoopFuture<HTTPStatus> {
        try metadata()
            .build { url, headers in
                let uri = URI(string: url + "/devices/\(imei)/actions/start-vehicle?cached=\(cached)&wait=\(wait)")
                return req.client.post(uri, headers: headers)
                    .transform(to: .ok)
            }
    }
}

extension OmniAPI {
    
    struct Status: Content {
        let lat: Double
        let lon: Double
        let altitude: Double
        let speedGps: Double
        let ignitionOn: Bool
        let iotBattery: Int
        let extendedData: Extended?
//        "extendedDataType": "AcmeABC1",
    }
    
    struct RequestBuilder {
        let headers: HTTPHeaders
        let baseUrl: String
        
        func build<T>(comletion: (String, HTTPHeaders) -> EventLoopFuture<T>) -> EventLoopFuture<T> {
            comletion(baseUrl, headers)
        }
    }
    
    struct Extended: Codable {
        let scooterBattery: Int?
        let scooterCharging: Bool?
        let scooterLocked: Bool?
        let vehicleBattery: Int?
        let vehicleEnabled: Bool?
        let vehicleOdometer: Double
        let topboxOpen: Bool?
        
        var battryLevel: Int? {
            scooterBattery ?? vehicleBattery
        }
        
        var loced: Bool {
            scooterLocked ?? false
        }
    }
}

extension Thing.Status {
    init(acton: OmniAPI.Status) {
        self.coordinate = .init(latitude: acton.lat, longitude: acton.lon)
        self.locked = acton.extendedData?.loced ?? false
        self.lockStatus = acton.ignitionOn ? .unlocked : .locked
        self.batteryLevel = acton.extendedData?.vehicleBattery ?? acton.iotBattery
        self.online = true
        self.charging = acton.extendedData?.scooterCharging
    }
}
