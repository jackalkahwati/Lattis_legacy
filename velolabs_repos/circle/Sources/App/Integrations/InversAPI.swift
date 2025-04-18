//
//  InversAPI.swift
//  
//
//  Created by Ravil Khusainov on 01.11.2021.
//

import Vapor
import Foundation

enum InversAPI: RestAPI {
    static func builder(_ path: String) throws -> RESTBuilder {
        guard let apiKey = Environment.get("INVERS_API_KEY") else { throw Abort(.unauthorized, reason: "INVERS_API_KEY no found") }
        guard let baseURL = Environment.get("INVERS_API_URL") else { throw Abort(.unauthorized, reason: "INVERS_API_URL no found") }
        let headers = HTTPHeaders([
            ("X-CloudBoxx-ApiKey", apiKey),
            ("Accept", "application/json"),
            ("Content-Type", "application/json"),
        ])
        let uri = URI(string: baseURL + "/" + path)
        return .init(headers: headers, uri: uri)
    }
    
    static func status(_ req: Request) throws -> EventLoopFuture<Status> {
        guard let qnr = req.parameters.get("qnr") else { throw Abort(.badRequest) }
        return try builder("devices/\(qnr)/status").build { uri, headers in
            req.client.get(uri, headers: headers)
        }
        .flatMapThrowing { response in
            try response.content.decode(Status.self)
        }
    }
    
    static func changeStatus(_ req: Request) throws -> EventLoopFuture<HTTPStatus> {
        guard let qnr = req.parameters.get("qnr") else { throw Abort(.badRequest) }
        let status = try req.content.decode(Status.self)
        struct St: Content {
            let state: Security
        }
        if let central = status.central_lock {
            return try builder("devices/\(qnr)/central-lock").build { uri, headers in
                req.client.put(uri, headers: headers) { request in
                    try request.content.encode(St(state: central))
                }
                .flatMapThrowing { response in
                    let result = try response.content.decode(St.self)
                    guard result.state == central else { return HTTPStatus.conflict }
                    return HTTPStatus.ok
                }
            }
        }
        if let immo = status.immobilizer {
            return try builder("devices/\(qnr)/immobilizer").build { uri, headers in
                req.client.put(uri, headers: headers) { request in
                    try request.content.encode(St(state: immo))
                }
                .flatMapThrowing { response in
                    let result = try response.content.decode(St.self)
                    guard result.state == immo else { return HTTPStatus.conflict }
                    return HTTPStatus.ok
                }
            }
        }
        throw Abort(.badRequest)
    }
}

extension InversAPI {
    struct Status: Content {
        let central_lock: Security?
        let immobilizer: Security?
        let ignition: Ignition?
    }
    
    enum Security: String, Codable {
        case locked, unlocked
    }
    
    enum Ignition: String, Codable {
        case on, off
    }
}
