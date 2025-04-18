//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 29.01.2021.
//

import Vapor
import Fluent

struct HubController: RouteCollection {
    func boot(routes: RoutesBuilder) throws {
        let hubs = routes.grouped("hubs")
        hubs.get(use: index)
        hubs.get("blitz", use: blitz)
        hubs.group(":id") { hub in
            hub.get(use: find)
            hub.put("dock", use: dock)
        }
    }
    
    fileprivate func index(req: Request) throws -> EventLoopFuture<[Hub.Meta]> {
        Hub.Meta.query(on: req.db(.main))
            .with(\.$fleet)
            .all()
    }
    
    fileprivate func dock(req: Request) throws -> EventLoopFuture<String> {
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.badRequest) }
        struct Docking: Content {
            let uuid: String
            let port: Int
        }
        let docking = try req.content.decode(Docking.self)
        return Hub.Meta.find(id, on: req.db(.main))
            .flatMapThrowing { hub in
                guard let uuid = hub?.hubUuid else { throw Abort(.notFound, reason: "hub_uuid is empty for hub: \(id)") }
                return Kuhmute.Dock(vehicle_uuid: docking.uuid, hub_uuid: uuid, port: docking.port)
            }
            .flatMapThrowing { (vehicle: Kuhmute.Dock) in
                do {
                    return try Kuhmute.dock(vehicle: vehicle, with: req.client)
                } catch {
                    throw Abort(.conflict, reason: error.localizedDescription)
                }
            }
            .flatMap({$0})
    }
    
    fileprivate func find(req: Request) throws -> EventLoopFuture<Hub> {
        guard let id = req.parameters.get("id", as: Int.self) else { throw Abort(.badRequest, reason: "Id is mandatory") }
        var meta: Hub.Meta?
        var kuhmute: Kuhmute.Hub?
        return Hub.Meta.query(on: req.db(.main))
            .filter(\.$id == id)
            .with(\.$fleet)
            .first()
            .unwrap(or: Abort(.notFound, reason: "Can't find hub: \(id) metadata"))
            .flatMapThrowing { hub in
                guard let uuid = hub.hubUuid else { throw Abort(.notFound, reason: "hub_uuid is empty for hub: \(id)") }
                meta = hub
                return uuid
            }
            .flatMap { (uuid: String) in
                Hub.Thing.query(on: req.db(.main))
                    .filter(\.$uuid == uuid)
                    .first()
                    .unwrap(or: Abort(.notFound, reason: "Can't find hub: \(id) thing by it's uuid: \(uuid)"))
            }
            .map { (hub) in
                kuhmute = Kuhmute.Hub.init(hub)
                return hub.uuid
            }
            .flatMap { uuid in
                Kuhmute.fetchPorts(hubUuid: uuid, whith: req.client)
            }
            .flatMapThrowing { (ports) in
                guard let fleet = meta?.fleet else {
                    throw Abort(.notFound, reason: "No fleet found for hub: \(id)")
                }
                guard let make = meta?.make else {
                    throw Abort(.notFound, reason: "No make found for hub: \(id)")
                }
                kuhmute?.ports = ports
                return Hub(id: id, fleet: fleet, kuhmute: kuhmute, make: make)
            }
    }
    
    // Switch this off when testing finished for Kuhmute
    fileprivate func blitz(req: Request) throws -> EventLoopFuture<String> {
        let vehicle = Kuhmute.Dock(vehicle_uuid: "na-est-2.veh_b32d64cd-b745-4be3-bda5-a64112114802",
                                   hub_uuid: "na_est_2.hub_33eab7e7-57b0-4e4f-abb5-dd8a8837c7e2",
                                   port: 2)
        do {
            return try Kuhmute.dock(vehicle: vehicle, with: req.client)
                .transform(to: "Vehicle is docked")
        } catch {
            throw Abort(.conflict, reason: error.localizedDescription)
        }
    }
}
