//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 03.02.2022.
//

import Vapor
import Fluent

struct ClipVehiclesController: RouteCollection {
    func boot(routes: RoutesBuilder) throws {
        let vehicles = routes.grouped("vehicles")
        vehicles.get(use: search)
        vehicles.group(":id") { vehicle in
            vehicle.get(use: self.vehicle)
        }
    }
    
    func vehicle(_ req: Request) async throws -> Bike {
        guard let id = req.parameters.get("id", as: Int.self) else {
            throw Abort(.badRequest)
        }
        let veh = try await Bike.query(on: req.db(.main))
            .filter(\.$id == id)
            .with(\.$fleet) { fleet in
                fleet.with(\.$paymentSettings)
            }
            .with(\.$things)
            .first()
            .unwrap(or: Abort(.notFound))
            .get()
        return veh
    }
    
    func search(_ req: Request) async throws -> Bike {
        if let code = try? req.query.get(Int.self, at: "qrCode") {
            let veh = try await Bike.query(on: req.db(.main))
                .filter(\.$qrCode == code)
                .with(\.$fleet) { fleet in
                    fleet.with(\.$paymentSettings)
                }
                .with(\.$things)
                .first()
                .unwrap(or: Abort(.notFound))
                .get()
            return veh
        }
        if let code = try? req.query.get(String.self, at: "controllerCode") {
            let controller = try await Thing.query(on: req.db(.main))
                .filter(\.$qrCode == code)
                .with(\.$bike) { bike in
                    bike.with(\.$fleet) { fleet in
                        fleet.with(\.$paymentSettings)
                    }
                    bike.with(\.$things)
                }
                .first()
                .unwrap(or: Abort(.notFound))
                .get()
            if let bike = controller.bike {
                return bike
            }
        }
        throw Abort(.notFound)
    }
}
