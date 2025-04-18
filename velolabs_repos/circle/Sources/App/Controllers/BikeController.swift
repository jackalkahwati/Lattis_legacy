
import Vapor
import Fluent

struct BikeController: RouteCollection {
    func boot(routes: RoutesBuilder) throws {
        let bikes = routes.grouped("vehicles")
        bikes.get(use: index)
        bikes.get("find", use: find)
        bikes.group(":id") { bike in
            bike.delete(use: delete)
            bike.get(use: read)
            bike.get("trips", use: trips)
            bike.patch(use: patch)
            bike.get("location", use: location)
        }
        bikes.patch(use: batch)
        bikes.get("total", use: total)
    }
    
    fileprivate func index(req: Request) throws -> EventLoopFuture<[Bike]> {
        var query = Bike.query(on: req.db(.main))
        if let fleetId = try? req.query.get(Int.self, at: "fleet_id") {
            query = query.filter(\.$fleet.$id == fleetId)
        }
        if let lockId = try? req.query.get(Int.self, at: "lock_id") {
            query = query.filter(\.$ellipse.$id == lockId)
        }
        if let name = try? req.query.get(String.self, at: "name") {
            query = query.filter(\.$name, .contains(inverse: false, .anywhere), name)
        } else {
            query = query.group(.and) { $0.filter(\.$name != nil).filter(\.$name != "") }
        }
        if let status = try? req.query.get(String.self, at: "status"), let st = Bike.Status(rawValue: status) {
            query = query.filter(\.$status == st)
        } else {
            query = query.filter(\.$status != .deleted)
        }
        if let string = try? req.query.get(String.self, at: "usage") {
            let usage = string.split(separator: ",").map(String.init).compactMap(Bike.Usage.init)
            query = query.filter(\.$usage ~~ usage)
        }
        if let level = try? req.query.get(Double.self, at: "battery-level") {
            query = query.filter(\.$batteryLevel != nil)
                .filter(\.$batteryLevel <= level)
        }
        return query
            .with(\.$group)
            .with(\.$things)
            .with(\.$ellipse)
            .sort(\.$status)
            .sort(\.$name)
            .sort(\.$id)
            .paginate(for: req)
            .map{ $0.items }
    }
    
    fileprivate func read(req: Request) throws -> EventLoopFuture<Bike> {
        guard let id = req.parameters.get("id", as: Int.self) else {
            throw Abort(.badRequest)
        }
        return Bike.query(on: req.db(.main))
            .filter(\.$id == id)
            .with(\.$group)
            .with(\.$things)
            .first()
            .unwrap(or: Abort(.notFound))
    }
    
    fileprivate func find(req: Request) throws -> EventLoopFuture<Bike> {
        let fleetId = try req.query.get(Int.self, at: "fleet_id")
        if let qrCode = try? req.query.get(Int.self, at: "qr_code") {
            return Bike.query(on: req.db(.main))
                .filter(\.$qrCode == qrCode)
                .filter(\.$fleet.$id == fleetId)
                .with(\.$group)
                .with(\.$things)
                .with(\.$ellipse)
                .sort(\.$status)
                .first()
                .unwrap(or: Abort(.notFound))
        }
        if let qrCode = try? req.query.get(String.self, at: "thing_qr_code") {
            return Thing.query(on: req.db(.main))
                .filter(\.$qrCode == qrCode)
                .filter(\.$fleet.$id == fleetId)
                .first()
                .unwrap(or: Abort(.notFound))
                .flatMap { thing in
                    thing.$bike.query(on: req.db(.main))
                        .with(\.$group)
                        .with(\.$things)
                        .with(\.$ellipse)
                        .first()
                }
                .unwrap(or: Abort(.notFound))
        }
        throw Abort(.badRequest)
    }
    
    fileprivate func delete(req: Request) throws -> EventLoopFuture<HTTPStatus> {
        guard let id = req.parameters.get("id", as: Int.self) else {
            throw Abort(.badRequest)
        }
        return Bike.find(id, on: req.db(.main))
            .unwrap(or: Abort(.notFound))
            .flatMap({ $0.delete(on: req.db(.main)) })
            .transform(to: .ok)
    }
    
    fileprivate func trips(req: Request) throws -> EventLoopFuture<Trip.Vehicle> {
        guard let id = req.parameters.get("id", as: Int.self) else {
            throw Abort(.badRequest)
        }
        return TripModel.query(on: req.db(.main))
            .filter(\.$vehicleId == id)
            .with(\.$receipt)
            .sort(\.$createdAt, .descending)
            .all()
            .flatMap { Trip.whithUsers($0, from: req.db(.user)) }
            .flatMap { trips in
                Booking.query(on: req.db(.main))
                    .filter(\.$vehicleId == id)
                    .filter(\.$finishedAt == nil)
                    .all()
                    .flatMap { Booking.whithUsers($0, from: req.db(.user)) }
                    .map { bookings in
                        Trip.Vehicle(trips: trips.filter({$0.endedAt == nil}), history: trips.filter({$0.endedAt != nil}).count, bookings: bookings)
                    }
            }
    }
    
    fileprivate func batch(req: Request) throws -> EventLoopFuture<HTTPStatus> {
        let string = try req.query.get(String.self, at: "batch")
        let patch = try req.content.decode(Bike.Patch.self)
        let vehicles = string.split(separator: ",").map(String.init).compactMap(Int.init)
        var query = Bike.query(on: req.db(.main))
            .filter(\.$id ~~ vehicles)
        if let status = patch.status {
            query = query.set(\.$status, to: status)
            willChange(status: status, usage: patch.usage, for: vehicles, req: req)
        }
        if let usage = patch.usage {
            query = query.set(\.$usage, to: usage)
        }
        if let maintenance = patch.maintenance {
            query = query.set(\.$maintenance, to: maintenance)
        } else {
            query = query.set(\.$maintenance, to: nil)
        }
        return query
            .update()
            .transform(to: .ok)
    }
    
    fileprivate func patch(req: Request) throws -> EventLoopFuture<HTTPStatus> {
        guard let id = req.parameters.get("id", as: Int.self) else {
            throw Abort(.badRequest)
        }
        let patch = try req.content.decode(Bike.Patch.self)
        return Bike.find(id, on: req.db(.main))
            .unwrap(or: Abort(.notFound))
            .flatMap { bike in
                if let status = patch.status {
                    bike.status = status
                    willChange(status: status, usage: patch.usage, for: [id], req: req)
                }
                if let usage = patch.usage {
                    bike.usage = usage
                }
                if let maintenance = patch.maintenance {
                    bike.maintenance = maintenance
                } else {
                    bike.maintenance = nil
                }
                if let coordinate = patch.coordinate {
                    bike.latitude = coordinate.latitude
                    bike.longitude = coordinate.longitude
                }
                return bike.update(on: req.db(.main))
                    .transform(to: .ok)
            }
    }
    
    /*
     Changing sound settings for IoT devices if possible on status change
     Only works for Grow now
     Also sets lastValidCoordinate to the GPSTracking service
     */
    
    fileprivate func willChange(status: Bike.Status, usage: Bike.Usage?, for vehicles: [Int], req: Request) {
        vehicles.forEach { id in
            _ = Thing.query(on: req.db(.main))
                .filter(\.$bike.$id == id)
                .first()
                .map { th in
                    guard let thing = th, let vendor = Thing.Vendor(rawValue: thing.vendor), vendor == .grow else { return }
                    switch status {
                    case .active:
                        _ = try? GrowAPI.control(sound: .init(controlType: nil, workMode: .on), on: thing.key, req: req)
                    default:
                        _ = try? GrowAPI.control(sound: .init(controlType: nil, workMode: .off), on: thing.key, req: req)
                    }
                }
        }
        if let use = usage {
            _ = try? GPSTracking.update(status: .init(status: status.rawValue, current_status: use.rawValue), vehicles: vehicles, client: req.client)
        }
    }
    
    private func location(req: Request) throws -> EventLoopFuture<Coordinate> {
        guard let id = req.parameters.get("id", as: Int.self) else {
            throw Abort(.badRequest)
        }
        return GPSTracking.Thing.query(on: req.db(.gps))
            .filter(\.$bikeId == id)
            .first()
            .flatMap { thing in
                if let coordinate = thing?.coordinate {
                    return req.eventLoop.makeSucceededFuture(coordinate)
                }
                do {
                    return try read(req: req)
                         .flatMap { bike in
                             if let lat = bike.latitude, let lon = bike.longitude {
                                 return req.eventLoop.makeSucceededFuture(.init(latitude: lat, longitude: lon))
                             }
                             return req.eventLoop.makeFailedFuture(Abort(.notFound))
                         }
                } catch {
                    return req.eventLoop.makeFailedFuture(error)
                }
            }
    }
    
    fileprivate func total(req: Request) throws -> EventLoopFuture<Int> {
        Bike.query(on: req.db(.main))
            .count()
    }
}

extension Trip {
    struct Vehicle: Content {
        let trips: [Trip]
        let history: Int
        let bookings: [Booking.Content]
    }
    
    static func whithUsers(_ trips: [TripModel], from db: Database) -> EventLoopFuture<[Trip]> {
        UserModel.query(on: db)
            .filter(\.$id ~~ trips.map(\.userId))
            .all()
            .map { users in trips.compactMap({Trip($0, users: users.map(User.init))}) }
    }
}

extension Booking {
    static func whithUsers(_ bookings: [Booking], from db: Database) -> EventLoopFuture<[Booking.Content]> {
        UserModel.query(on: db)
            .filter(\.$id ~~ bookings.map(\.userId))
            .all()
            .map { users in bookings.compactMap({Booking.Content($0, users: users.map(User.init))}) }
    }
}




