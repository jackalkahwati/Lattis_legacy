//
//  CreateOAuth.swift
//  
//
//  Created by Ravil Khusainov on 19.01.2022.
//

import Fluent
import JWTKit

struct CreateOAuth: AsyncMigration {
    
    func prepare(on database: Database) async throws {
        try await database.schema("oauth")
            .field(.id, .int32, .identifier(auto: true))
            .field("provider", .enum(.init(name: "oauth_provider", cases: ["google", "apple", "facebook"])), .required)
            .field("service", .enum(.init(name: "oauth_service", cases: ["lattis", "operator", "ellipse"])), .required)
            .field("provider_id", .string)
            .field("user_id", .int32)
            .create()
            .get()
    }
    
    func revert(on database: Database) async throws {
        try await database.schema("oauth").delete().get()
    }
}

struct TokenCreator: JWTPayload {
    var sub: SubjectClaim
    var exp: ExpirationClaim
    
    func verify(using signer: JWTSigner) throws {
    }
}
