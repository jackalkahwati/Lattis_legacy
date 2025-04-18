//
//  Kuhmute.swift
//  
//
//  Created by Ravil Khusainov on 29.01.2021.
//

import Vapor

struct Kuhmute {
    static let endpoint = "https://sandbox.blitz.kuhmute.dev"
    static let apiKey = "pk_test_42e36535-9960-49d8-a6b6-fedd254d083d"
    static let headers: HTTPHeaders = .init(
        [
            ("Authorization", apiKey),
            ("Content-Type", "application/json")
        ]
    )
    
    static func dock(vehicle: Dock, with client: Client) throws -> EventLoopFuture<String> {
        let url = URI(string: endpoint + "/api/v1/event/vehicle/dock")
        let data = try vehicle.data()
        return client.put(url, headers: headers, beforeSend: { (request) in
            request.body = .init(data: data)
        })
        .transform(to: "Vehicle is docked!")
    }
    
    static func fetchPorts(hubUuid: String, whith client: Client) -> EventLoopFuture<[Port]> {
        let url = URI(string: endpoint + "/api/v1/provider/hub/\(hubUuid)")
        struct RawHub: Decodable {
            let ports: [Port]
        }
        struct Payload: Decodable {
            let hub: RawHub
        }
        return client.get(url, headers: headers)
            .flatMapThrowing { (response) in
                do {
                    let payload = try response.content.decode(Payload.self)
                    return payload.hub.ports
                } catch {
                    throw Abort(.conflict, reason: response.description)
                }
            }
    }
    
    static func update(location: LocationUpdate, with client: Client) -> EventLoopFuture<String> {
        let url = URI(string: endpoint + "api/v1/event/hub/location")
        return client.post(url, headers: headers) { req in
            var buffer: ByteBuffer!
            try? JSONEncoder().encode(location, into: &buffer)
            req.body = buffer
        }
        .transform(to: "Hub location updated")
    }
}

extension Kuhmute {
    struct LocationUpdate: Content {
        let uuid: String
        let lat: Double
        let lng: Double
    }
}
