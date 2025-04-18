//
//  DucktAPI.swift
//  
//
//  Created by Ravil Khusainov on 09.09.2021.
//

import Vapor
import Fluent

enum DucktAPI {
    static func headers(authorization: String? = nil) -> HTTPHeaders {
        var head = HTTPHeaders([
            ("Accept", "application/json"),
            ("Content-Type", "application/json")
        ])
        if let auth = authorization {
            head.add(name: .authorization, value: auth)
        }
        return head
    }
    static var authorization: String?
    
    static func baseURI(with path: String, query: String? = nil) throws -> URI {
        guard let env = Environment.get("DUCKT_API_URL") else { throw Abort(.internalServerError, reason: "DUCKT_API_URL not found") }
        var url = URI(string: env)
        url.path = path
        url.query = query
        return url
    }
    
    static func authenticate(req: Request, fleet: Int) throws -> EventLoopFuture<HTTPHeaders> {
        if let auth = authorization {
            return req.client.eventLoop.makeSucceededFuture(headers(authorization: auth))
        }
        let url = try baseURI(with: "api/authenticate")
        return Integration<String?>.query(on: req.db(.main))
            .filter(\.$fleetId == fleet)
            .filter(\.$type == .duckt)
            .first()
            .unwrap(or: Abort(.notFound, reason: "Integration not found"))
            .flatMap { integration in
                guard let key = integration.apiKey else { return req.client.eventLoop.makeFailedFuture(Abort(.notFound, reason: "api key not found")) }
                guard let username = integration.email else { return req.client.eventLoop.makeFailedFuture(Abort(.notFound, reason: "email not found")) }
                do {
                    return try Lambda.ECDH.decrypt(key, client: req.client)
                        .flatMap { password in
                            return req.client.post(url, headers: headers()) { request in
                                try request.content.encode(Credentials.init(username: username, password: password))
                            }
                            .flatMapThrowing { response in
                                let token = try response.content.decode(Token.self)
                                authorization = token.jwt
                                return headers(authorization: token.jwt)
                            }
                        }
                } catch {
                    return req.eventLoop.makeFailedFuture(error)
                }
            }
    }
    
    static func status(of thing: Thing, req: Request) async throws -> Status {
        guard let bikeId = thing.bikeId else { throw Abort(.conflict)}
        let url = try baseURI(with: "api/status", query: "vehicle=\(bikeId)")
        let headers = try await authenticate(req: req, fleet: thing.fleetId)
            .get()
        let response = try await req.client.get(url, headers: headers)
            .get()
        return try response.content.decode(Status.self)
    }
    
    static func unlock(thing: Thing, req: Request) throws -> EventLoopFuture<HTTPStatus> {
        guard let bikeId = thing.bikeId else { throw Abort(.conflict)}
        let url = try baseURI(with: "api/unlock", query: "vehicle=\(bikeId)")
        return try authenticate(req: req, fleet: thing.fleetId)
            .flatMap { headers in
                req.client.post(url, headers: headers)
                    .transform(to: .ok)
            }
    }
}

extension DucktAPI {
    struct Credentials: Content {
        let username: String
        let password: String
    }
    
    struct Token: Content {
        let jwt: String
    }
    
    struct Status: Content {
        let lat: Double
        let lng: Double
        let status: LockStatus
    }
    
    enum LockStatus: String, Codable {
        case locked = "LOCKED"
        case unlocked = "UNLOCKED"
    }
}

extension Thing.Status {
    init(_ duckt: DucktAPI.Status) {
        coordinate = .init(latitude: duckt.lat, longitude: duckt.lng)
        locked = duckt.status == .locked
        online = true
        batteryLevel = nil
        charging = nil
        lockStatus = duckt.status == .locked ? .locked : .unlocked
    }
}
