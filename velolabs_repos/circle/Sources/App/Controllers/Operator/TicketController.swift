//
//  TicketController.swift
//  
//
//  Created by Ravil Khusainov on 01.03.2021.
//

import Vapor
import Fluent

struct TicketController: RouteCollection {
    func boot(routes: RoutesBuilder) throws {
        let tickets = routes.grouped("tickets")
        tickets.get(use: index)
        tickets.post(use: create)
        tickets.group(":id") { ticket in
            ticket.patch(use: patch)
        }
    }
    
    fileprivate func index(req: Request) throws -> EventLoopFuture<[Ticket]> {
        var query = Ticket.query(on: req.db(.main))
        if let fleetId = try? req.query.get(Int.self, at: "fleet_id") {
            query = query.filter(\.$fleetId == fleetId)
        }
        if let status = try? req.query.get(String.self, at: "status"),
           let st = Ticket.Status(rawValue: status) {
            query = query.filter(\.$status == st)
        } else {
            query = query.filter(\.$status != .resolved)
        }
        if let vehicle = try? req.query.get(Int.self, at: "vehicle_id") {
            query = query.filter(\.$vehicle.$id == vehicle)
        }
        if let assignee = try req.query.get(Int?.self, at: "assignee") {
            query = query.filter(\.$assignee == assignee)
        }
        return query
            .with(\.$vehicle) {
                $0.with(\.$group)
                $0.with(\.$things)
                $0.with(\.$ellipse)
            }
            .sort(\.$createdAt, .descending)
            .all()
    }
    
    fileprivate func patch(req: Request) throws -> EventLoopFuture<Ticket> {
        let patch = try req.content.decode(Ticket.Patch.self)
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.badRequest) }
        return Ticket.query(on: req.db(.main))
            .filter(\.$id == id)
            .with(\.$vehicle) {
                $0.with(\.$group)
                $0.with(\.$things)
                $0.with(\.$ellipse)
            }
            .first()
            .flatMapThrowing { t in
                guard let ticket = t else { throw Abort(.notFound) }
                return ticket
            }
            .flatMap { (ticket: Ticket) in
                patch.patch(ticket)
                return ticket.save(on: req.db(.main))
                    .transform(to: ticket)
            }
    }
    
    fileprivate func create(req: Request) throws -> EventLoopFuture<Ticket> {
        let new = try req.content.decode(Ticket.Create.self)
        let ticket = Ticket(new)
        return ticket.create(on: req.db(.main))
            .flatMap {
                ticket.$vehicle.query(on: req.db(.main))
                    .with(\.$group)
                    .with(\.$ellipse)
                    .with(\.$things)
                    .first()
                    .map { ticket.$vehicle.value = $0 }
            }
            .map{ticket}
    }
}
