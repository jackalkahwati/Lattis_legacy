//
//  Hub.swift
//  
//
//  Created by Ravil Khusainov on 29.01.2021.
//

import Vapor
import Fluent

struct Hub: Content {
    let id: Int
    let fleet: Fleet
    let kuhmute: Kuhmute.Hub?
    let make: String
}

extension Hub {
    final class Meta: Model, Content {
        static var schema = "hubs_and_fleets"
        
        @ID(custom: "id")
        var id: Int?
        
        @OptionalParent(key: "fleet_id")
        var fleet: Fleet?
        
        @OptionalField(key: "hub_uuid")
        var hubUuid: String?
        
        @OptionalField(key: "make")
        var make: String?
    }
    
    final class Thing: Model, Content {
        static var schema = "hubs"
        
        @ID(custom: "hub_id")
        var id: Int?
        
        @OptionalField(key: "name")
        var name: String?
        
        @Field(key: "uuid")
        var uuid: String
        
        @Field(key: "latitude")
        var latitude: Double
        
        @Field(key: "longitude")
        var longitude: Double
    }
}

extension Kuhmute {
    struct Hub: Content {
        let id: Int
        let name: String?
        let uuid: String
        let latitude: Double
        let longitude: Double
        var ports: [Port]
    }
    
    struct Port: Content {
        let number: Int
        let vin: String
        let status: String
    }
    
    struct Dock: Codable {
        let vehicle_uuid: String
        let hub_uuid: String
        let port: Int
        
        func data() throws -> Data { try JSONEncoder().encode(self) }
    }
}

extension Kuhmute.Hub {
    init?(_ thing: Hub.Thing) {
        guard let id = thing.id else { return nil }
        self.init(id: id, name: thing.name, uuid: thing.uuid, latitude: thing.latitude, longitude: thing.longitude, ports: [])
        
    }
}

