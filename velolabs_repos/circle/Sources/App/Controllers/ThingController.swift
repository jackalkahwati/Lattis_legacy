
import Vapor
import Fluent

struct ThingController: RouteCollection {
    
    func boot(routes: RoutesBuilder) throws {
        let things = routes.grouped("things")
        things.get(use: index)
        things.group(":id") { thing in
            thing.get(use: find)
            thing.get("status", use: status)
            thing.put("lock", use: lock)
            thing.put("unlock", use: unlock)
            thing.put("uncover", use: uncover)
            thing.put("light", use: light)
            thing.put("sound", use: sound)
        }
        things.group("linka") { linka in
            linka.get("command", ":id", use: LinkaAPI.command)
        }
        things.group("ellipse") { ellipse in
            ellipse.get("find", use: EllipseAPI.find)
            ellipse.put(":id", "assign", use: EllipseAPI.assign)
            ellipse.get(":id", "credentials", use: EllipseAPI.credentials)
            ellipse.get(":id", "pin", use: EllipseAPI.pin)
        }
        things.group("invers", ":qnr") { invers in
            invers.get("status", use: InversAPI.status)
            invers.put("status", use: InversAPI.changeStatus)
        }
        things.group("kisi", ":id") { kisi in
            kisi.get(use: KisiAPI.device)
            kisi.put("unlock", use: KisiAPI.unlock)
        }
        things.group("tapkey", ":id") { tapkey in
            tapkey.get("credentials", use: TapkeyAPI.credentials)
        }
        let sas = things.grouped("sas", ":id")
        try sas.register(collection: SASController())
        
        things.get("find", use: search)
        things.post("onboard", use: onboard)
    }
    
    private func index(req: Request) throws -> EventLoopFuture<[Thing]> {
        var future = Thing.query(on: req.db(.main))
        if let bikeId = try req.query.get(Int?.self, at: "bike_id") {
            future = future.filter(\.$bike.$id == bikeId)
        }
        if let fleetId = try req.query.get(Int?.self, at: "fleet_id") {
            future = future.filter(\.$fleet.$id == fleetId)
        }
        return future
            .with(\.$bike)
            .all()
    }
    
    func status(req: Request) throws -> EventLoopFuture<Thing.Status> {
        try find(req: req)
            .flatMap { (thing: Thing) in
                do {
                    let vendor = try Thing.Vendor(thing)
                    switch vendor {
                    case .linka:
                        return try LinkaAPI.status(thing: thing, req: req)
                            .map(Thing.Status.init)
                    case .segway:
                        return try SegwayAPI.getStatus(thing.key, client: req.client)
                            .map(Thing.Status.init)
                    case .segwayEU:
                        return try SegwayAPI.getStatus(thing.key, client: req.client, region: .eu)
                            .map(Thing.Status.init)
                    case .grow:
                        return try GrowAPI.getStatus(vehicle: thing.key, req: req)
                            .map(Thing.Status.init)
                            .flatMapError { error in
                                guard let e = error as? MQTTRunner.Failure, e == .timeout else {
                                    return req.eventLoop.makeFailedFuture(error)
                                }
                                return req.eventLoop.makeSucceededFuture(Thing.Status(locked: false, online: false, coordinate: nil, batteryLevel: nil, charging: nil, lockStatus: nil))
                            }
                    case .acton:
                        return try ActonAPI.status(imei: thing.key, req: req)
                            .map(Thing.Status.init)
                    case .omniLock:
                        return try OmniAPI.status(imei: thing.key, req: req)
                            .map(Thing.Status.init)
                    case .geotab:
                        return try GeotabAPI.status(thing: thing, req: req)
                            .map(Thing.Status.init)
                    case .comodule:
                        return ComoduleAPI.getStatus(thing.key, client: req.client)
                            .map(Thing.Status.init)
                    case .manualLock:
                        throw Abort(.conflict, reason: "NO integration for \(vendor.rawValue)")
                    case .duckt:
                        let promise = req.eventLoop.makePromise(of: Thing.Status.self)
                        promise.completeWithTask {
                            let st = try await DucktAPI.status(of: thing, req: req)
                            return .init(st)
                        }
                        return promise.futureResult
                    case .kisi:
                        throw Abort(.conflict, reason: "Kisi has separate route")
                    }
                } catch {
                    return req.eventLoop.makeFailedFuture(error)
                }
            }
    }
    
    private func lock(req: Request) throws -> EventLoopFuture<Thing.Message> {
        try find(req: req)
            .flatMap { thing in
                do {
                    let vendor = try Thing.Vendor(thing)
                    switch vendor {
                    case .linka:
                        return try LinkaAPI.lock(thing: thing, req: req)
                            .map {Thing.Message(linka: $0)}
                    case .segway:
                        return try SegwayAPI.lock(thing.key, client: req.client)
                            .transform(to: Thing.Message(linka: nil))
                    case .segwayEU:
                        return try SegwayAPI.lock(thing.key, client: req.client, region: .eu)
                            .transform(to: Thing.Message(linka: nil))
                    case .grow:
                        return try GrowAPI.lock(vehicle: thing.key, req: req)
                            .transform(to: Thing.Message(linka: nil))
                    case .acton:
                        return try ActonAPI.lock(imei: thing.key, req: req)
                            .transform(to: Thing.Message(linka: nil))
                    case .omniLock:
                        return try OmniAPI.lock(imei: thing.key, req: req)
                            .transform(to: Thing.Message(linka: nil))
                    case .geotab:
                        return try GeotabAPI.lock(thing: thing, req: req)
                            .transform(to: Thing.Message(linka: nil))
                    case .comodule:
                        return ComoduleAPI.lock(thing.key, client: req.client)
                            .transform(to: Thing.Message(linka: nil))
                    case .manualLock:
                        throw Abort(.conflict, reason: "NO integration for \(vendor.rawValue)")
                    case .duckt:
                        throw Abort(.conflict, reason: "Lock is not possible with Duckt")
                    case .kisi:
                        throw Abort(.conflict, reason: "Kisi has separate route")
                    }
                } catch {
                    return req.eventLoop.makeFailedFuture(error)
                }
            }
    }
    
    private func unlock(req: Request) throws -> EventLoopFuture<Thing.Message> {
        try find(req: req)
            .flatMap { thing in
                do {
                    let vendor = try Thing.Vendor(thing)
                    switch vendor {
                    case .linka:
                        return try LinkaAPI.unlock(thing: thing, req: req)
                            .map {Thing.Message(linka: $0)}
                    case .segway:
                        return try SegwayAPI.unlock(thing.key, client: req.client)
                            .transform(to: Thing.Message(linka: nil))
                    case .segwayEU:
                        return try SegwayAPI.unlock(thing.key, client: req.client, region: .eu)
                            .transform(to: Thing.Message(linka: nil))
                    case .grow:
                        return try GrowAPI.unlock(vehicle: thing.key, req: req)
                            .transform(to: Thing.Message(linka: nil))
                    case .acton:
                        return try ActonAPI.unlock(imei: thing.key, req: req)
                            .transform(to: Thing.Message(linka: nil))
                    case .omniLock:
                        return try OmniAPI.unlock(imei: thing.key, req: req)
                            .transform(to: Thing.Message(linka: nil))
                    case .geotab:
                        return try GeotabAPI.unlock(thing: thing, req: req)
                            .transform(to: Thing.Message(linka: nil))
                    case .comodule:
                        return ComoduleAPI.unlock(thing.key, client: req.client)
                            .transform(to: Thing.Message(linka: nil))
                    case .manualLock:
                        throw Abort(.conflict, reason: "NO integration for \(vendor.rawValue)")
                    case .duckt:
                        return try DucktAPI.unlock(thing: thing, req: req)
                            .transform(to: Thing.Message(linka: nil))
                    case .kisi:
                        throw Abort(.conflict, reason: "Kisi has separate route")
                    }
                } catch {
                    return req.eventLoop.makeFailedFuture(error)
                }
            }
    }
    
    private func uncover(req: Request) throws -> EventLoopFuture<HTTPStatus> {
        try find(req: req)
            .flatMap { thing in
                do {
                    let vendor = try Thing.Vendor(thing)
                    switch vendor {
                    case .segway:
                        return try SegwayAPI.uncoverBattery(thing.key, client: req.client)
                    case .segwayEU:
                        return try SegwayAPI.uncoverBattery(thing.key, client: req.client, region: .eu)
                    default: throw Abort(.conflict, reason: "No integration for \(thing.vendor)")
                    }
                } catch {
                    return req.eventLoop.makeFailedFuture(error)
                }
            }
    }
    
    private func light(req: Request) throws -> EventLoopFuture<HTTPStatus> {
        let light = try req.content.decode(Thing.Light.self)
        return try find(req: req)
            .flatMap { thing in
                do {
                    let vendor = try Thing.Vendor(thing)
                    switch vendor {
                    case .segway:
                        return try SegwayAPI.light(.init(thing.key, light: light), client: req.client)
                    case .segwayEU:
                        return try SegwayAPI.light(.init(thing.key, light: light), client: req.client, region: .eu)
                    default: throw Abort(.conflict, reason: "No integration for \(thing.vendor)")
                    }
                } catch {
                    return req.eventLoop.makeFailedFuture(error)
                }
            }
    }
    
    private func sound(req: Request) throws -> EventLoopFuture<HTTPStatus> {
        let sound = try req.content.decode(Thing.Sound.self)
        return try find(req: req)
            .flatMap { thing in
                do {
                    let vendor = try Thing.Vendor(thing)
                    switch vendor {
                    case .segway:
                        return try SegwayAPI.sound(.init(thing.key, sound: sound), client: req.client)
                    case .segwayEU:
                        return try SegwayAPI.sound(.init(thing.key, sound: sound), client: req.client, region: .eu)
                    case .grow:
                        return try GrowAPI.control(sound: sound, on: thing.key, req: req)
                    default: throw Abort(.conflict, reason: "No integration for \(thing.vendor)")
                    }
                } catch {
                    return req.eventLoop.makeFailedFuture(error)
                }
            }
    }
        
    private func find(req: Request) throws -> EventLoopFuture<Thing> {
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.badRequest, reason: "id not found in request") }
        return Thing.query(on: req.db(.main))
            .filter(\.$id == id)
            .with(\.$fleet)
            .with(\.$bike)
            .first()
            .unwrap(or: Abort(.notFound, reason: "Could not find thing with id: \(id)"))
    }
    
    private func search(_ req: Request) async throws -> Thing {
        var query = Thing.query(on: req.db(.main))
        if let key = try? req.query.get(String.self, at: "key") {
            query = query.filter(\.$key == key)
        }
        if let vendor = try? req.query.get(String.self, at: "vendor") {
            query = query.filter(\.$vendor == vendor)
        }
        return try await query
            .first()
            .unwrap(or: Abort(.notFound))
            .get()
    }
    
    private func onboard(_ req: Request) async throws -> Thing {
        do {
            let thing = try req.content.decode(Thing.New.self)
            try await thing.create(on: req.db(.main))
            let res = try await Thing.find(thing.id, on: req.db(.main))
            return res!
        } catch {
            print(error)
            throw error
        }
    }
}

extension Thing.Status {
    init(_ segway: SegwayAPI.Status) {
        online = segway.data.online
        locked = segway.data.locked
        coordinate = segway.coordinate
        batteryLevel = segway.data.powerPercent
        charging = segway.data.charging
        lockStatus = segway.data.locked ? .locked : .unlocked
    }
    
    init(_ linka: LinkaAPI.Status) {
        online = linka.active
        locked = linka.lock_state == .locked
        coordinate = .init(latitude: linka.latitude, longitude: linka.longitude)
        batteryLevel = linka.lock_battery_percent
        charging = nil
        lockStatus = .init(linka.lock_state)
    }
}

extension Thing.LockStatus {
    init(_ linka: LinkaAPI.LockState) {
        switch linka {
        case .locked:
            self = .locked
        case .unlocked:
            self = .unlocked
        case .locking:
            self = .locking
        case .unlocking:
            self = .unlocting
        }
    }
}

extension Thing {
    struct Light: Content {
        let headLight: State?
        let tailLight: State?
        
        enum State: Int, Codable {
            case off, on, flicker
        }
    }
    
    struct Sound: Content {
        let controlType: Control?
        let workMode: Mode?
        
        enum Control: Int, Codable {
            case outOfGeofence = 1
            case toot = 2
            case lowBattery = 3
        }
        
        enum Mode: Int, Codable {
            case noSettings = 0
            case off = 1
            case on = 2
        }
    }
}

protocol CoordinateContaining {
    var latitude: Double? { get }
    var longitude: Double? { get }
}

extension CoordinateContaining {
    var coordinate: Coordinate? {
        guard let lat = latitude, let lon = longitude else { return nil }
        return .init(latitude: lat, longitude: lon)
    }
}
