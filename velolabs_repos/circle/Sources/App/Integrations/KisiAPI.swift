//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 23.11.2021.
//

import Vapor
import Fluent

enum KisiAPI: IntegrationAPI {
    static func builder(_ path: String, apiKey: String) throws -> RESTBuilder {
        guard let baseUrl = Environment.get("KISI_API_URL") else { throw Abort(.conflict, reason: "No KISI_API_URL found") }
        let uri = URI(string: baseUrl + "/" + path)
        return .init(headers: .init([
            ("Authorization", "KISI-LOGIN \(apiKey)"),
            ("Accept", "application/json"),
            ("Content-Type", "application/json"),
            ("Connection", "keep-alive")
        ]), uri: uri)
    }
    
    static func integration(_ path: String, req: Request) throws -> EventLoopFuture<RESTBuilder> {
        let fleetId = try req.query.get(Int.self, at: "fleetId")
        return Integration<String?>.query(on: req.db(.main))
            .filter(\.$fleetId == fleetId)
            .filter(\.$type == .kisi)
            .first()
            .unwrap(or: Abort(.notFound))
            .flatMapThrowing { integration in
                guard let key = integration.apiKey else { throw Abort(.conflict, reason: "No API key found for integration")}
                return try builder(path, apiKey: key)
            }
    }
    
    static func device(_ req: Request) throws -> EventLoopFuture<Lock> {
        guard let id = req.parameters.get("id") else { throw Abort(.badRequest, reason: "no id found")}
        return try integration("locks/\(id)", req: req)
            .flatMap { builder in
                builder.build { uri, headers in
                    req.client.get(uri, headers: headers)
                        .flatMapThrowing { response in
                            let lock = try response.content.decode(Lock.self)
                            return lock
                        }
                }
            }
    }
    
    static func unlock(_ req: Request) throws -> EventLoopFuture<HTTPStatus> {
        guard let id = req.parameters.get("id") else { throw Abort(.badRequest, reason: "no id found")}
        return try integration("locks/\(id)/unlock", req: req)
            .flatMap { builder in
                builder.build { uri, headers in
                    req.client.post(uri, headers: headers) { request in
                        try request.content.encode(UnlockOptions.standard)
                    }
                    .flatMapThrowing { response in
                        response.status
                    }
                }
            }
    }
}

extension KisiAPI {
    struct Lock: Content {
        let id: Int
        let online: Bool
        let unlocked: Bool
        let name: String?
        let description: String?
    }
    
    struct UnlockOptions: Content {
        struct Lock: Codable {
            let proximity_proof: String
        }
        let lock: Lock
        
        static let standard: UnlockOptions = .init(lock: .init(proximity_proof: "string"))
    }
}

protocol IntegrationAPI {
    static func integration(_ path: String, req: Request) throws -> EventLoopFuture<RESTBuilder>
}
