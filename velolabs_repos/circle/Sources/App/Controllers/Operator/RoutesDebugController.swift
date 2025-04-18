//
//  RoutesDebugController.swift
//  
//
//  Created by Ravil Khusainov on 16.03.2021.
//

import Vapor

struct RoutesDebugController: RouteCollection {
    func boot(routes: RoutesBuilder) throws {
        let debug = routes.grouped("debug")
        
        let grow = debug.grouped("grow")
        grow.group(":id") { iot in
            iot.get(use: growStatus)
            iot.put("lock", use: lock)
        }
        
        
        let kuhmute = debug.grouped("kuhmute")
        kuhmute.put("location", use: kuhmuteUpdateLocation)
        
    }
    
//    BCDDC2D0A27E
//    -BCDDC2CF8F8E
//    -BCDDC2D42B86
//    -BCDDC2D5426A
//    -BCDDC2CF2303
    func growStatus(req: Request) throws -> EventLoopFuture<GrowAPI.Status> {
        guard let id = req.parameters.get("id") else { throw Abort(.badRequest) }
        return try req.mqtt.receive(from: "s/h/17/\(id)")
            .flatMapThrowing { buffer in
                try JSONDecoder().decode(GrowAPI.Status.self, from: buffer)
            }
    }
    
    func lock(req: Request) throws -> EventLoopFuture<String> {
        guard let id = req.parameters.get("id") else { throw Abort(.badRequest) }
        return try req.mqtt.run(command: "1,0", on: "s/c/\(id)", subscribing: "s/a/17/\(id)")
            .flatMapThrowing { buffer in
                let data = Data(buffer: buffer)
                guard let str = String(data: data, encoding: .utf8) else { throw Abort(.conflict)}
                return str
            }
    }
    
    func kuhmuteUpdateLocation(req: Request) throws -> EventLoopFuture<String> {
        let location = try req.content.decode(Kuhmute.LocationUpdate.self)
        return Kuhmute.update(location: location, with: req.client)
    }
}
