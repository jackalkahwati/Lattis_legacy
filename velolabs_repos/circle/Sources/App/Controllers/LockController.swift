//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 05.09.2020.
//

import Vapor
import Fluent

struct LockController: RouteCollection {
    func boot(routes: RoutesBuilder) throws {
        let locks = routes.grouped("locks")
        routes.get("lock", ":id", use: read)
        routes.get("locks", ":id", use: read)
        locks.get(use: index)
    }
    
    func index(req: Request) throws -> EventLoopFuture<[Lock]> {
        var future = Lock.query(on: req.db(.main))
        if let macId = try? req.query.get(String.self, at: "mac_id") {
            future = future.filter(\.$macId == macId)
        }
        if let fleetId = try? req.query.get(Int.self, at: "fleet_id") {
            future = future.filter(\.$fleet.$id == fleetId)
        }
        return future
            .with(\.$fleet)
            .all()
    }
    
    fileprivate func read(req: Request) throws -> EventLoopFuture<Lock> {
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.notFound) }
        return Lock.query(on: req.db(.main))
            .filter(\.$id == id)
            .with(\.$fleet)
            .first()
            .unwrap(or: Abort(.notFound))
    }
}
