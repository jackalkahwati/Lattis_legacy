//
//  BookingController.swift
//  
//
//  Created by Ravil Khusainov on 14.06.2021.
//

import Vapor
import Fluent

struct BookingController: RouteCollection {
    func boot(routes: RoutesBuilder) throws {
        let bookings = routes.grouped("bookings")
        bookings.group(":id") { booikn in
            booikn.put("cancel", use: cancel)
        }
    }
    
    func cancel(req: Request) throws -> EventLoopFuture<HTTPStatus> {
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.badRequest) }
        let now = UInt(Date().timeIntervalSince1970)
        return Booking.query(on: req.db(.main))
            .filter(\.$id == id)
            .first()
            .unwrap(or: Abort(.notFound))
            .flatMap { booking in
                booking.finishedAt = now
                booking.status = .cancelled
                return booking.save(on: req.db(.main))
                    .transform(to: .ok)
            }
    }
}
