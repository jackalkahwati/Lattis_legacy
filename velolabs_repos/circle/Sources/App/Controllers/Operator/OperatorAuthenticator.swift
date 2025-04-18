//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 22.05.2021.
//

import Vapor
import Fluent

struct OperatorAuthenticator: BearerAuthenticator {
    
    func login(req: Request) throws -> EventLoopFuture<FleetOperator.Auth> {
        let login = try req.content.decode(FleetOperator.Login.self)
        return FleetOperator.query(on: req.db(.user))
            .filter(\.$email == login.email)
            .first()
            .unwrap(or: Abort(.notFound, reason: "No operator with email \(login.email) found"))
            .flatMapThrowing { oper in
                guard LattisPasswordHasher.verify(password: login.password, hashed: oper.password) else { throw Abort(.unauthorized, reason: "Invalid password: \(oper.id!)") }
                return FleetOperator.Auth(token: oper.token, operator: .init(oper: oper))
            }
    }
    
    func authenticate(bearer: BearerAuthorization, for request: Request) -> EventLoopFuture<Void> {
        FleetOperator.query(on: request.db(.user))
            .filter(\.$token == bearer.token)
            .first()
            .unwrap(or: Abort(.unauthorized, reason: "Invalid token"))
            .map { oper in
                request.auth.login(oper)
                return ()
            }
    }
}

struct LattisPasswordHasher {
    static func verify(password: String, hashed: String) -> Bool {
        let salt = String(hashed.prefix(16))
        let digest = SHA256.hash(data: Data((salt + password).utf8))
        return hashed == salt + digest.hex
    }
}

struct HeaderAuthenticator: RequestAuthenticator {
    func authenticate(request: Request) -> EventLoopFuture<Void> {
        if request.headers[.authorization].contains(where: {$0.hasPrefix("Bearer")}) {
            return request.eventLoop.makeSucceededVoidFuture()
        }
        return request.eventLoop.makeFailedFuture(Abort(.unauthorized))
    }
}
