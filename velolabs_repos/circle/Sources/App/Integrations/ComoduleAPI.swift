//
//  ComoduleAPI.swift
//  
//
//  Created by Ravil Khusainov on 01.09.2021.
//

import Vapor
import Fluent


enum ComoduleAPI {
    static let headers: HTTPHeaders = .init(
        [
            ("Accept", "application/json"),
            ("Content-Type", "application/ninebot_es4+json")
        ]
    )
    static let baseUrl = "https://api.comodule.com/vehicleapi/v3"
    static let apiKey = Environment.get("COMODULE_API_KEY")!
    
    static func getStatus(_ vehicleId: String, client: Client) -> EventLoopFuture<Status> {
        var uri = URI(string: baseUrl + "/vehicle/\(vehicleId)")
        uri.query = "apiKey=\(apiKey)"
        return client.get(uri, headers: headers)
            .flatMapThrowing { response in
                let status = try response.content.decode(Status.self)
                return status
            }
    }
    
    static func lock(_ vehicleId: String, client: Client) -> EventLoopFuture<HTTPStatus> {
        updateState(vehicleId: vehicleId, client: client, vehiclePoweOn: false)
    }
    
    static func unlock(_ vehicleId: String, client: Client) -> EventLoopFuture<HTTPStatus> {
        updateState(vehicleId: vehicleId, client: client, vehiclePoweOn: true)
    }
    
    fileprivate static func updateState(vehicleId: String, client: Client, vehiclePoweOn: Bool) -> EventLoopFuture<HTTPStatus> {
        struct VehicleStatus: Content {
            let vehiclePowerOn: Bool
        }
        var uri = URI(string: baseUrl + "/vehicle/\(vehicleId)")
        uri.query = "apiKey=\(apiKey)&sync=true"
        return client.post(uri, headers: headers) { request in
            let content = VehicleStatus(vehiclePowerOn: vehiclePoweOn)
            try request.content.encode(content)
        }
        .transform(to: .ok)
    }
}

extension ComoduleAPI {
    struct Status: Content {
        let gpsLatitude: Double
        let gpsLongitude: Double
        let modulePowerOn: Bool
        let vehiclePowerOn: Bool
        let moduleBatteryPercentage: Double
        let vehicleBatteryPercentage: Double?
    }
}

extension Thing.Status {
    init(_ comodule: ComoduleAPI.Status) {
        online = comodule.modulePowerOn
        locked = !comodule.vehiclePowerOn
        coordinate = .init(latitude: comodule.gpsLatitude, longitude: comodule.gpsLongitude)
        batteryLevel = Int(comodule.vehicleBatteryPercentage ?? 0)
        charging = false
        lockStatus = comodule.vehiclePowerOn ? .unlocked : .locked
    }
}
