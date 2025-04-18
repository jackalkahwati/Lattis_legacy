//
//  TripController.swift
//  
//
//  Created by Ravil Khusainov on 15.03.2021.
//

import Vapor
import Fluent

struct TripController: RouteCollection {
    func boot(routes: RoutesBuilder) throws {
        let trips = routes.grouped("trips")
        trips.get(use: index)
        trips.group(":id") { trip in
//            trip.get(use: find)
            trip.put("end", use: end)
        }
        trips.get("total", use: total)
    }
    
    func index(req: Request) throws -> EventLoopFuture<[Trip]> {
        var query = TripModel.query(on: req.db(.main))
        if let bikeId = try? req.query.get(Int.self, at: "vehicle_id") {
            query = query.filter(\.$vehicleId == bikeId)
        }
        if let active = try? req.query.get(Bool.self, at: "active") {
            if active {
                query = query.filter(\.$endedAt == nil)
            } else {
                query = query.filter(\.$endedAt != nil)
            }
        }
        return query
            .sort(\.$createdAt, .descending)
            .with(\.$receipt)
            .all()
            .flatMap { Trip.whithUsers($0, from: req.db(.user)) }
    }
    
//    func find(req: Request) throws -> EventLoopFuture<Trip> {
//        TripModel.find(req.parameters.get("id", as: Int.self), on: req.db(.main))
//            .unwrap(or: Abort(.notFound))
//    }
    
    func end(req: Request) throws -> EventLoopFuture<HTTPStatus> {
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.badRequest) }
        let now = UInt(Date().timeIntervalSince1970)
        return TripModel.query(on: req.db(.main))
            .filter(\.$id == id)
            .with(\.$booking)
            .first()
            .unwrap(or: Abort(.notFound))
            .flatMap { trip in
                trip.endedAt = now
                return trip.$booking.query(on: req.db(.main))
                    .filter(\.$trip.$id == trip.id)
                    .set(\.$finishedAt, to: now)
                    .set(\.$status, to: .finished)
                    .update()
                    .flatMap {
                        trip.save(on: req.db(.main))
                            .flatMap {
                                Bike.query(on: req.db(.main))
                                    .filter(\.$id == trip.vehicleId!)
                                    .set(\.$status, to: .active)
                                    .set(\.$usage, to: .parked)
                                    .update()
                                    .transform(to: HTTPStatus.ok)
                            }
                    }
            }
    }
    
    fileprivate func total(req: Request) throws -> EventLoopFuture<Int> {
        TripModel.query(on: req.db(.main))
            .count()
    }
}
