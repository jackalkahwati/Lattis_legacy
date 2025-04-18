//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 01.06.2021.
//

import Vapor
import Fluent

struct Bbox {
    let sw: Coordinate
    let ne: Coordinate
    
    init(coordinates: [Double]) throws {
        guard coordinates.count == 4 else { throw Failure.bboxFomratError }
        sw = .init(latitude: coordinates[0], longitude: coordinates[2])
        ne = .init(latitude: coordinates[1], longitude: coordinates[3])
    }
    
    enum Failure: LocalizedError {
        case bboxFomratError
        
        var errorDescription: String? {
            switch self {
            case .bboxFomratError:
                return "Bbox is array of 4 coordinates: bbox = Array(Double), sw = (lat: bbox[0], lon: bbox[2]), ne = (lat: bbox[1], lon: bbox[3]) "
            }
        }
    }
}

struct MapController: RouteCollection {
    func boot(routes: RoutesBuilder) throws {
        let map = routes.grouped("map")
        let vehicles = map.grouped("vehicles")
        vehicles.get(use: search)
    }
    
    func search(req: Request) throws -> EventLoopFuture<[Bike]> {
        let string = try req.query.get(String.self, at: "bbox")
        let fleetId = try req.query.get(Int.self, at: "fleet_id")
        let coordinates = string.split(separator: ",").compactMap(Double.init)
        let bbox = try Bbox(coordinates: coordinates)
        var query = Bike.query(on: req.db(.main))
            .filter(\.$latitude != nil)
            .filter(\.$longitude != nil)
            .filter(\.$fleetId == fleetId)
            .filter(\.$latitude > bbox.sw.latitude)
            .filter(\.$latitude < bbox.ne.latitude)
            .filter(\.$longitude > bbox.sw.longitude)
            .filter(\.$longitude < bbox.ne.longitude)
        if let str = try? req.query.get(String.self, at: "usage") {
            let usages = str.split(separator: ",").map(String.init).compactMap(Bike.Usage.init)
            if !usages.isEmpty {
                query = query
                    .filter(\.$usage ~~ usages)
            }
        }
        if let str = try? req.query.get(String.self, at: "maintenance") {
            let mnt = str.split(separator: ",").map(String.init).compactMap(Bike.Filter.Maintenance.init)
            mnt.forEach { maint in
                switch maint {
                case .lowBattery:
                    query = query
                        .filter(\.$batteryLevel != nil)
                        .filter(\.$batteryLevel <= 15)
                }
            }
        }
        if let level = try? req.query.get(Double.self, at: "battery-level") {
            query = query
                .filter(\.$batteryLevel != nil)
                .filter(\.$batteryLevel <= level)
        }
        if let name = try? req.query.get(String.self, at: "name") {
            query = query.filter(\.$name, .contains(inverse: false, .anywhere), name)
        } else {
            query = query.group(.and) { $0.filter(\.$name != nil).filter(\.$name != "") }
        }
        return query
            .with(\.$group)
            .with(\.$things)
            .with(\.$ellipse)
            .all()
    }
}
