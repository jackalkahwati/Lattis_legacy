//
//  EllipseAPI.swift
//  
//
//  Created by Ravil Khusainov on 21.10.2021.
//

import Foundation
import Vapor
import Fluent

enum EllipseAPI {
    static func credentials(_ req: Request) async throws -> Credentials {
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.badRequest) }
        let lock = try await Lock.query(on: req.db(.main))
            .filter(\.$id == id)
            .with(\.$fleet)
            .first()
            .unwrap(or: Abort(.notFound))
            .get()
        guard let mac = lock.macId else { throw Abort(.notFound, reason: "macId id required") }
            guard let fleetKey = lock.fleet?.key else { throw Abort(.notFound, reason: "fleet.key is required") }
            guard let lockKey = lock.key else { throw Abort(.notFound, reason: "lock.key is required") }
        let macId = try await req.decrypt(mac)
        let secret = try await req.decrypt(fleetKey)
        let privateKey = try await req.decrypt(lockKey)
        let publicKey = try await req.publicKey(privateKey)
        let signedMessage = try await req.signedMessage(.init(mac_id: macId, user_id: secret, private_key: privateKey, public_key: publicKey))
        return .init(macId: macId, signedMessage: signedMessage, publicKey: publicKey, secret: secret)
    }
    
    static func pin(req: Request) throws -> EventLoopFuture<[Lock.Pin]> {
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.notFound) }
        return PinCode.query(on: req.db(.main))
            .filter(\.$lockId == id)
            .first()
            .unwrap(or: Abort(.notFound))
            .flatMap { pin in
                guard let code = pin.code else { return req.eventLoop.makeFailedFuture(Abort(.notFound))}
                do {
                    return try Lambda.ECDH.decrypt(code, client: req.client)
                        .map { pincode in
                            let array = pincode.split(separator: ",").map(String.init).compactMap(Lock.Pin.init)
                            return array
                        }
                } catch {
                    return req.eventLoop.makeFailedFuture(error)
                }
            }
    }
    
    static func find(req: Request) async throws -> Lock {
        let query = Lock.query(on: req.db(.main))
        if let mac = try? req.query.get(String.self, at: "macId") {
            let encoded = try await req.encrypt(mac)
            return try await query
                .filter(\.$macId == encoded)
                .first()
                .unwrap(or: Abort(.notFound))
                .get()
        }
        throw Abort(.badRequest)
    }
    
    static func assign(req: Request) async throws -> HTTPStatus {
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.notFound) }
        let bikeId = try req.content.get(Int.self, at: "bikeId")
        let db = req.db(.main)
        let lock = try await Lock.query(on: db)
            .filter(\.$id == id)
            .first()
            .unwrap(or: Abort(.notFound))
            .get()
        let bike = try await Bike.query(on: db)
            .filter(\.$id == bikeId)
            .first()
            .unwrap(or: Abort(.notFound))
            .get()
        bike.ellipse = lock
        lock.fleetId = bike.fleetId
        try await bike.update(on: db)
        try await lock.update(on: db)
        return .ok
    }
    
    static func onboard(req: Request) async throws -> Lock {
        let lock = try req.content.decode(Lock.self)
        guard let fleetId = lock.fleetId else { throw Abort(.conflict, reason: "fleetId is required") }
        let db = req.db(.main)
        try await lock.create(on: db)
        let fleet = try await Fleet.find(fleetId, on: db).unwrap(or: Abort(.notFound, reason: "Fleet \(fleetId) not found")).get()
        
        if let bikeId = try? req.query.get(Int.self, at: "bikeId") {
            try await Bike.query(on: db)
                .set(\.$lockId, to: lock.id)
                .filter(\.$id == bikeId)
                .update()
        }
        return lock
    }
    
    struct Credentials: Content {
        let macId: String
        let signedMessage: String
        let publicKey: String
        let secret: String
    }
}

private extension Request {
    func decrypt(_ value: String) async throws -> String {
        try await Lambda.ECDH.decrypt(value, client: client).get()
    }
    
    func encrypt(_ value: String) async throws -> String {
        try await Lambda.ECDH.encrypt(value, client: client).get()
    }
    
    func publicKey(_ privateKey: String) async throws -> String {
        try await Lambda.ECDH.publicKey(privateKey, client: client).get()
    }
    
    func signedMessage(_ req: Lambda.KeyMaster.KeyRequest) async throws -> String {
        try await Lambda.KeyMaster.signedMessage(client: client, req: req).get()
    }
}

