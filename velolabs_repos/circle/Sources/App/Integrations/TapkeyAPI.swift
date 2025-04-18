//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 17.01.2022.
//

import Vapor
import Fluent

enum TapkeyAPI: IntegrationAPI {
    static func integration(_ path: String, req: Request) throws -> EventLoopFuture<RESTBuilder> {
        let fleetId = try req.query.get(Int.self, at: "fleetId")
        return Integration<Metadata>.query(on: req.db(.main))
            .filter(\.$fleetId == fleetId)
            .filter(\.$type == .tapkey)
            .filter(\.$metadeta != nil)
            .first()
            .unwrap(or: Abort(.notFound))
            .flatMap { integration in // Get Valid token
                req.eventLoop.makeFailedFuture(Abort(.notFound))
            }
    }
    
    static func credentials(_ req: Request) throws -> EventLoopFuture<Credentials> {
        req.eventLoop.makeFailedFuture(Abort(.notFound))
    }
}

extension TapkeyAPI {
    struct Metadata: Codable {
        let accessToken: String
        let refreshToken: String
    }
    
    struct Credentials: Content {
        let accessToken: String
        let physicalLockId: String
    }
}
