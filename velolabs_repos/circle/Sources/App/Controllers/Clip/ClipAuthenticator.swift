//
//  ClipAuthenticator.swift.swift
//  
//
//  Created by Ravil Khusainov on 01.02.2022.
//

import Vapor
import Fluent
import Foundation

struct ClipAuthenticator: RequestAuthenticator {
    func authenticate(request: Request) -> EventLoopFuture<Void> {
        let promise = request.eventLoop.makePromise(of: Void.self)
        do {
            let user = try request.content.decode(OAuth.User.self)
            promise.completeWithTask {
                let _ = try await request.jwt.apple.verify(user.identityToken, applicationIdentifier: "io.lattis.www.Lattis.Clip").get()
                promise.succeed(())
            }
        } catch {
            promise.fail(error)
        }
        return promise.futureResult
    }
    
    func signIn(_ req: Request) async throws -> User {
        let user = try req.content.decode(OAuth.User.self)
        let oauth = try await OAuth.query(on: req.db(.main))
            .filter(\.$providerId == user.user)
            .first()
            .get()
        if let o = oauth {
            let model = try await UserModel.query(on: req.db(.user))
                .filter(\.$id == o.userId)
                .first()
                .unwrap(or: Abort(.conflict, reason: "There is no user for OAuth: \(o.userId)"))
                .get()
            return .init(model)
        } else {
            let model = try await getOrCreate(user, req: req)
            try await createOAuth(user, userId: model.id!, req: req)
            return .init(model)
        }
    }
    
    func getOrCreate(_ user: OAuth.User, req: Request) async throws -> UserModel {
        guard let email = user.email else { throw Abort(.conflict, reason: "Email required for authentication") }
        let existing = try await UserModel.query(on: req.db(.user))
            .filter(\.$email == email)
            .filter(\.$scope == .lattis)
            .first()
            .get()
        if let model = existing {
            return model
        }
        let model = UserModel(user)
        let plain = "\(user.email!)*deliminator*\(Int(Date().timeIntervalSince1970) + 28 * 24 * 3600 * 1000)"
        model.restToken = try await Lambda.ECDH.encrypt(plain, client: req.client).get()
        try await model.save(on: req.db(.user))
        return model
    }
    
    func createOAuth(_ user: OAuth.User, userId: Int, req: Request) async throws {
        let oauth = OAuth(provider: .apple, service: .lattis, userId: userId, providerId: user.user)
        try await oauth.create(on: req.db(.main))
    }
}
