//
//  UserController.swift
//  
//
//  Created by Ravil Khusainov on 28.11.2020.
//

import Vapor
import Fluent


struct UserController: RouteCollection {
    func boot(routes: RoutesBuilder) throws {
        let users = routes.grouped("users")
        users.get(use: index)
        users.group(":id") { user in
            user.put("block", use: blockUser)
            user.get("blocked", use: getBlockedUser)
            user.put("unblock", use: unblockUser)
            user.put("anonymize", use: anonymize)
        }
    }
    
    func index(req: Request) -> EventLoopFuture<[User]> {
        UserModel.query(on: req.db(.user))
            .all()
            .map({$0.map(User.init)})
    }
    
    func blockUser(req: Request) throws -> EventLoopFuture<HTTPStatus> {
        UserModel.find(req.parameters.get("id", as: Int.self), on: req.db(.user))
            .unwrap(or: Abort(.notFound))
            .flatMap { user in
                guard let phone = user.phoneNumber else { return req.eventLoop.makeFailedFuture(Abort(.conflict))}
                let blocked = UserModel()
                user.phoneNumber = nil
                blocked.phoneNumber = phone
                blocked.scope = .lattis
                blocked.firstName = "\(user.id!)"
                return user.save(on: req.db(.user))
                    .flatMap {
                        blocked.save(on: req.db(.user))
                            .transform(to: .ok)
                    }
            }
    }
    
    func getBlockedUser(req: Request) throws -> EventLoopFuture<User> {
        guard let id = req.parameters.get("id", as: String.self) else { throw Abort(.badRequest) }
        return UserModel.query(on: req.db(.user))
            .filter(\.$firstName == id)
            .first()
            .unwrap(or: Abort(.notFound))
            .map{User($0)}
    }
    
    func unblockUser(req: Request) throws -> EventLoopFuture<HTTPStatus> {
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.badRequest) }
        return UserModel.query(on: req.db(.user))
            .filter(\.$firstName == "\(id)")
            .first()
            .unwrap(or: Abort(.notFound))
            .flatMap { blocked in
                blocked.delete(on: req.db(.user))
                    .flatMap {
                        UserModel.query(on: req.db(.user))
                            .filter(\.$id == id)
                            .set(\.$phoneNumber, to: blocked.phoneNumber)
                            .update()
                            .transform(to: .ok)
                    }
            }
    }
    
    func anonymize(req: Request) throws -> EventLoopFuture<HTTPStatus> {
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.badRequest) }
        return UserModel.query(on: req.db(.user))
            .filter(\.$id == id)
            .set(\.$email, to: UUID().uuidString)
            .set(\.$identifier, to: UUID().uuidString)
            .set(\.$password, to: UUID().uuidString)
            .set(\.$firstName, to: nil)
            .set(\.$lastName, to: nil)
            .set(\.$phoneNumber, to: nil)
            .set(\.$refreshToken, to: nil)
            .set(\.$restToken, to: nil)
            .update()
            .flatMap {
                removeAccess(req: req)
            }
            .flatMap {
                removePaymentProfiles(req: req)
            }
            .transform(to: .ok)
    }
    
    func removeAccess(req: Request) -> EventLoopFuture<Void> {
        guard let id = req.parameters.get("id", as: Int.self) else { return req.eventLoop.makeFailedFuture(Abort(.badRequest)) }
        return User.FleetAccess.query(on: req.db(.user))
            .filter(\.$userId == id)
            .delete()
    }
    
    func removePaymentProfiles(req: Request) -> EventLoopFuture<Void> {
        guard let id = req.parameters.get("id", as: Int.self) else { return req.eventLoop.makeFailedFuture(Abort(.badRequest)) }
        return User.PaymentProfile.query(on: req.db(.main))
            .filter(\.$userId == id)
            .delete()
    }
}
