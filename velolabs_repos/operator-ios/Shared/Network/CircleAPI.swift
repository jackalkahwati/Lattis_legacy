//
//  CircleAPI.swift
//  Operator
//
//  Created by Ravil Khusainov on 02.03.2021.
//

import Foundation
import Combine
import CoreLocation

enum CircleAPI {
    
    static fileprivate(set) var agent = NetworkAgent(Env.rootURL)
    
    static func logOut() {
        agent.authorization = nil
    }
    
    static func logIn(_ token: String) {
        agent.authorization = .bearer(token)
    }
}

extension CircleAPI {
    static func login(user: FleetOperator.LogIn) -> AnyPublisher<FleetOperator.Auth, Error> {
        agent.run(.post(user, path: "operator/login"))
    }
    
    static func user() -> AnyPublisher<FleetOperator, Error> {
        agent.run(.get("operator/me"))
    }
    
    static func fleets() -> AnyPublisher<[Fleet], Error> {
        agent.run(.get("operator/fleets/"))
    }
    
    static func fleet(_ id: Int) -> AnyPublisher<Fleet, Error> {
        agent.run(.get("operator/fleets/\(id)"))
    }
    
    static func tickets(_ fleetId: Int, searchTags: [Ticket.SearchTag]) -> AnyPublisher<[Ticket.Metadata], Error> {
        var items: [URLQueryItem] = [.init(name: "fleet_id", value: String(fleetId))]
        items += searchTags.map({ tag in
            switch tag {
            case .assignee(let op) where op == .unassigned:
                return URLQueryItem(name: "assignee", value: nil)
            case .assignee(let op):
                return URLQueryItem(name: "assignee", value: "\(op.id)")
            case .vehicle(let id):
                return URLQueryItem(name: "vehicle_id", value: "\(id)")
            case .status(let status):
                return URLQueryItem(name: "status", value: status.rawValue)
            }
        })
        return agent.run(.get("operator/tickets", queryItems: items))
    }
    
    static func vehicles(_ fleetId: Int, tags: [Vehicle.SearchTag], page: Int = 1, per: Int = 20) -> AnyPublisher<[Vehicle.Metadata], Error> {
        var items: [URLQueryItem] = [.init(name: "fleet_id", value: String(fleetId))]
        items += tags.map { tag in
            switch tag {
            case .name(let name):
                return URLQueryItem(name: "name", value: name)
            case .status(let status):
                return URLQueryItem(name: "status", value: status.rawValue)
            }
        }
        items.append(.init(name: "page", value: "\(page)"))
        items.append(.init(name: "per", value: "\(per)"))
        return agent.run(.get("operator/vehicles", queryItems: items))
    }
    
    static func vehicles(_ fleetId: Int, filters: [Vehicle.Filter], page: Int = 1, per: Int = 20) -> AnyPublisher<[Vehicle.Metadata], Error> {
        var items: [URLQueryItem] = [.init(name: "fleet_id", value: String(fleetId))]
        items += filters.map { filter in
            switch filter {
            case .name(let name):
                return URLQueryItem(name: "name", value: name)
            case .usage(let usages):
                return URLQueryItem(name: "usage", value: usages.map(\.rawValue).joined(separator: ","))
            case .batterLevel(let level):
                return URLQueryItem(name: "battery-level", value: "\(level)")
            case .maintenance:
                #warning("Remove this one")
                return URLQueryItem(name: "old", value: "value")
            }
        }
        items.append(.init(name: "page", value: "\(page)"))
        items.append(.init(name: "per", value: "\(per)"))
        return agent.run(.get("operator/vehicles", queryItems: items))
    }
    
    static func vehicle(_ id: Int) -> AnyPublisher<Vehicle.Metadata, Error> {
        agent.run(.get("operator/vehicles/\(id)"))
    }
    
    static func location(vehicleId: Int) -> AnyPublisher<CLLocationCoordinate2D, Error> {
        agent.run(.get("operator/vehicles/\(vehicleId)/location"))
    }
    
    static func findVehicle(_ fleetId: Int, qrId: Int? = nil, thingQrCode: String? = nil) -> AnyPublisher<Vehicle.Metadata, Error> {
        var items: [URLQueryItem] = [.init(name: "fleet_id", value: String(fleetId))]
        if let id = qrId  {
            items.append(.init(name: "qr_code", value: "\(id)"))
        }
        if let id = thingQrCode {
            items.append(.init(name: "thing_qr_code", value: id))
        }
        return agent.run(.get("operator/vehicles/find", queryItems: items))
    }
    
    static func status(thingId: Int) -> AnyPublisher<Thing.Status, Error> {
        agent.run(.get("operator/things/\(thingId)/status"))
    }
    
    static func lock(thingId: Int) -> AnyPublisher<Thing.Message, Error> {
        agent.run(.put("operator/things/\(thingId)/lock"))
    }
    
    static func unlock(thingId: Int) -> AnyPublisher<Thing.Message, Error> {
        agent.run(.put("operator/things/\(thingId)/unlock"))
    }
    
    static func uncover(thingId: Int) -> AnyPublisher<Void, Error> {
        agent.run(.put("operator/things/\(thingId)/uncover"))
    }
    
    static func trackLinka(command: String, fleetId: Int) -> AnyPublisher<Thing.Linka.CommandInfo, Error> {
        agent.run(.get("operator/things/linka/command/\(command)", queryItems: [.init(name: "fleet_id", value: "\(fleetId)")]))
    }
    
    static func oper(id: Int) -> AnyPublisher<FleetOperator, Error> {
        agent.run(.get("operator/colleagues/\(id)"))
    }
    
    static func colleagues(fleetId: Int) -> AnyPublisher<[FleetOperator], Error> {
        agent.run(.get("operator/colleagues", queryItems: [.init(name: "fleet_id", value: String(fleetId))]))
    }
    
    static func patch(ticket: Int, json: Ticket.Patch) -> AnyPublisher<Ticket.Metadata, Error> {
        agent.run(.patch(json, path: "operator/tickets/\(ticket)"))
    }
    
    static func create(ticket: Ticket.Create) -> AnyPublisher<Ticket.Metadata, Error> {
        agent.run(.post(ticket, path: "operator/tickets"))
    }
    
    static func trips(vehicleId: Int, active: Bool = false) -> AnyPublisher<[Trip], Error> {
        agent.run(.get("operator/trips", queryItems: [.init(name: "vehicle_id", value: String(vehicleId)), .init(name: "active", value: String(active))]))
    }
    
    static func tripMeta(vehicleId: Int) -> AnyPublisher<Vehicle.TripMeta, Error> {
        agent.run(.get("operator/vehicles/\(vehicleId)/trips"))
    }
    
    static func control(light: Thing.Lighth, thingId: Int) -> AnyPublisher<Void, Error> {
        agent.run(.put(light, path: "operator/things/\(thingId)/light"))
    }
    
    static func control(sound: Thing.Sound, thingId: Int) -> AnyPublisher<Void, Error> {
        agent.run(.put(sound, path: "operator/things/\(thingId)/sound"))
    }
    
    static func patch(vehicle: Int, json: Vehicle.Patch) -> AnyPublisher<Void, Error> {
        agent.run(.patch(json, path: "operator/vehicles/\(vehicle)"))
    }
    
    static func update(vehicles: [Int], json: Vehicle.Patch) -> AnyPublisher<Void, Error> {
        agent.run(.patch(json, path: "operator/vehicles", queryItems: [.init(name: "batch", value: vehicles.map(String.init).joined(separator: ","))]))
    }
    
    static func end(trip: Int) -> AnyPublisher<Void, Error> {
        agent.run(.put("operator/trips/\(trip)/end"))
    }
    
    static func cancel(booking: Int) -> AnyPublisher<Void, Error> {
        agent.run(.put("operator/bookings/\(booking)/cancel"))
    }
}


extension Thing {
    struct Lighth: Codable {
        let headLight: State?
        let tailLight: State?
        
        enum State: Int, Codable {
            case off, on, flicker
        }
    }
    
    struct Sound: Codable {
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
