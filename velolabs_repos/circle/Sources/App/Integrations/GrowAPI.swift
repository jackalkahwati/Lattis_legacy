//
//  GreenAPI.swift
//  
//
//  Created by Ravil Khusainov on 14.03.2021.
//

import Vapor

struct GrowAPI {
    static func getStatus(vehicle: String, req: Request) throws -> EventLoopFuture<Status> {
        try req.mqtt.run(command: "101", on: "s/c/" + vehicle, subscribing: "s/a/17/" + vehicle)
            .flatMapThrowing { bytes in
                let payload = try JSONDecoder().decode(Payload<LockStatus>.self, from: bytes)
                let locked = payload.response == .locked
                return Status(locked: locked, lat: nil, lon: nil, batteryLevel: nil)
            }
    }
    
    static func lock(vehicle: String, req: Request) throws -> EventLoopFuture<HTTPStatus> {
        try req.mqtt.run(command: "1,1", on: "s/c/\(vehicle)", subscribing: "s/a/17/\(vehicle)")
            .flatMapThrowing { bytes in
//                guard let str = String(buffer: bytes) else { throw Abort(.conflict) }
//                print(str)
                return HTTPStatus.ok
            }
    }
    
    static func unlock(vehicle: String, req: Request) throws -> EventLoopFuture<HTTPStatus> {
        try req.mqtt.run(command: "1,0", on: "s/c/" + vehicle, subscribing: "s/a/17/" + vehicle)
            .flatMapThrowing { bytes in
                return HTTPStatus.ok
            }
    }
    
    static func control(sound: Thing.Sound, on vehicle: String, req: Request) throws -> EventLoopFuture<HTTPStatus> {
        guard let mode = sound.workMode else { return req.eventLoop.makeSucceededFuture(.ok) }
        var command = "57,"
        switch mode {
        case .off:
            command += "0"
        case .on:
            command += "1"
        default:
            break
        }
        return try req.mqtt.run(command: command, on: "s/c/" + vehicle, subscribing: "s/a/17/" + vehicle)
            .flatMapThrowing { bytes in
                return HTTPStatus.ok
            }
    }
}


extension GrowAPI {
    struct Status: Content {
        let locked: Bool
        let lat: Double?
        let lon: Double?
        let batteryLevel: Int?
        
        enum CodingKeys: String, CodingKey {
            case locked = "coderr"
            case lat = "position.latitude"
            case lon = "position.longitude"
            case batteryLevel = "batiot"
        }
    }
    
    struct Payload<T:Codable>: Codable {
        let command: Int
        let response: T
    }
    
    enum LockStatus: Int, Codable {
        case unlocked, locked
    }
}

extension Thing.Status {
    init(_ grow: GrowAPI.Status) {
        locked = grow.locked
        batteryLevel = grow.batteryLevel
        if let lat = grow.lat, let lon = grow.lon {
            coordinate = .init(latitude: lat, longitude: lon)
        } else {
            coordinate = nil
        }
        charging = nil
        online = true
        lockStatus = grow.locked ? .locked : .unlocked
    }
}
