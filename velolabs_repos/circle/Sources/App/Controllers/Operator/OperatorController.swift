//
//  OperatorController.swift
//  
//
//  Created by Ravil Khusainov on 01.03.2021.
//

import Vapor
import Fluent
import Plot

struct OperatorController: RouteCollection {
    
    func boot(routes: RoutesBuilder) throws {
        routes.group("colleagues") { col in
            col.get(use: colleagues)
            col.get(":id", use: find)
        }
        routes.get("me", use: me)
    }
    
    fileprivate func find(req: Request) throws -> EventLoopFuture<FleetOperator.User> {
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.badRequest, reason: "Bad id \(req.parameters.get("id") ?? "none")") }
        return FleetOperator.find(id, on: req.db(.user))
            .unwrap(or: Abort(.notFound, reason: "No user found with \(id)"))
            .flatMapThrowing(FleetOperator.User.init)
    }
    
    fileprivate func me(req: Request) throws -> EventLoopFuture<FleetOperator.User> {
        guard let oper = req.auth.get(FleetOperator.self) else { throw Abort(.unauthorized) }
        req.parameters.set("id", to: String(oper.id!))
        return try find(req: req)
    }
    
    fileprivate func colleagues(req: Request) throws -> EventLoopFuture<[FleetOperator.User]> {
        let fleetId = try req.query.get(Int.self, at: "fleet_id")
        return Fleet.Association.query(on: req.db(.main))
            .filter(\.$fleet.$id == fleetId)
            .all()
            .flatMap { assc in
                return FleetOperator.query(on: req.db(.user))
                    .filter(\.$id ~~ assc.compactMap{$0.operatorId})
                    .all()
                    .map{$0.map(FleetOperator.User.init)}
            }
    }
}


