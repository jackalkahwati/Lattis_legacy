//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 16.06.2021.
//

import Vapor
import Fluent

enum GeotabAPI {
    
    static func metadata(_ fleetId: Int, req: Request) throws -> EventLoopFuture<RequestBuilder> {
        return Integration<Metadata>.query(on: req.db(.main))
            .filter(\.$fleetId == fleetId)
            .filter(\.$type == .geotab)
            .first()
            .unwrap(or: Abort(.notFound, reason: "No integration found for Geotab IoT"))
            .flatMapThrowing { integration in
//                guard let data = integration.metadeta.data(using: .utf8) else { throw Abort(.conflict, reason: "No metadata found for Geotab IoT on fleet \(fleetId)")}
                guard let session = integration.sessionId else { throw Abort(.conflict, reason: "No session found for Geotab IoT on fleet\(fleetId)")}
                guard let email = integration.email else { throw Abort(.conflict, reason: "No email found for Geotab IoT on fleet\(fleetId)")}
                guard let meta = integration.metadeta else { throw Abort(.conflict, reason: "No metadata found for Geotab IoT on fleet \(fleetId)")}
//                let meta = try JSONDecoder().decode(Metadata.self, from: data)
                return RequestBuilder(credentials: Credentials(database: meta.database, sessionId: session, userName: email), server: meta.server_name)
            }
    }
    
    static func status(thing: Thing, req: Request) throws -> EventLoopFuture<Status> {
        try metadata(thing.fleetId, req: req)
            .flatMap { builder in
                builder.get(params: builder.params(.device), with: req.client)
                    .flatMap { (devices: Result<[Device]>) in
                        guard let device = devices.result.first(where: {$0.serialNumber == thing.key.replacingOccurrences(of: "-", with: "")}) else { return req.eventLoop.makeFailedFuture(Abort(.notFound))}
                        return builder.get(params: builder.search(.deviceStatus, device: device), with: req.client)
                            .flatMapThrowing { (status: Result<[Status]>) in
                                guard let s = status.result.first(where: { $0.device.id == device.id }) else { throw Abort(.notFound) }
                                return s
                            }
                    }
            }
    }
    
    static func lock(thing: Thing, req: Request) throws -> EventLoopFuture<HTTPStatus> {
        try metadata(thing.fleetId, req: req)
            .flatMap { builder in
                builder.get(params: builder.params(.device), with: req.client)
                    .flatMap { (devices: Result<[Device]>) in
                        guard let device = devices.result.first(where: {$0.serialNumber == thing.key.replacingOccurrences(of: "-", with: "")}) else { return req.eventLoop.makeFailedFuture(Abort(.notFound))}
                        return builder.add(params: builder.textMessage(content: .init(isRelayOn: true), device: device), whit: req.client)
                            .map { (message: Result<String>) in
                                return HTTPStatus.ok
                            }
                    }
            }
    }
    
    static func unlock(thing: Thing, req: Request) throws -> EventLoopFuture<HTTPStatus> {
        try metadata(thing.fleetId, req: req)
            .flatMap { builder in
                builder.get(params: builder.params(.device), with: req.client)
                    .flatMap { (devices: Result<[Device]>) in
                        guard let device = devices.result.first(where: {$0.serialNumber == thing.key.replacingOccurrences(of: "-", with: "")}) else { return req.eventLoop.makeFailedFuture(Abort(.notFound))}
                        return builder.add(params: builder.textMessage(content: .init(isRelayOn: false), device: device), whit: req.client)
                            .map { (message: Result<String>) in
                                return HTTPStatus.ok
                            }
                    }
            }
    }
}

extension GeotabAPI {
    
    struct RequestBuilder {
        let credentials: Credentials
        let server: String
        
        var uri: URI {
            .init(string: "https://\(server)/apiv1/")
        }
        
        let headers: HTTPHeaders = .init([
            ("Accept", "application/json"),
            ("Content-Type", "application/json")
        ])
        
        func params(_ typeName: TypeName) -> Params {
            .init(typeName: typeName, credentials: credentials)
        }
        
        func textMessage(content: TextMessageParams.MessageContent, device: Device) -> TextMessageParams {
            .init(typeName: .textMessage, credentials: credentials, entity: .init(device: device, messageContent: content, isDirectionToVehicle: true))
        }
        
        func search(_ typeName: TypeName, device: Device) -> Search {
            .init(typeName: typeName, credentials: credentials, search: .init(deviceSearch: device))
        }
        
        func get<P: GeotabParams, Value: Content>(params: P, with client: Client) -> EventLoopFuture<Value> {
            run(command: .init(method: .get, params: params), with: client)
        }
        
        func add<P: GeotabParams, Value: Content>(params: P, whit client: Client) -> EventLoopFuture<Value> {
            run(command: .init(method: .add, params: params), with: client)
        }
        
        fileprivate func run<P: Codable, Value: Content>(command: Command<P>, with client: Client) -> EventLoopFuture<Value> {
            client.post(uri, headers: headers) { req in
                try req.content.encode(command)
            }
            .flatMapThrowing { resp in
                let string = String(buffer: resp.body!)
                print(string)
                return try resp.content.decode(Value.self)
            }
        }
    }
    
    struct Metadata: Codable {
        let server_name: String
        let database: String
    }
    
    struct Status: Content {
        let latitude: Double
        let longitude: Double
        let isDeviceCommunicating: Bool
        let isDriving: Bool
        let device: Device
        
        struct Device: Codable {
            let id: String
        }
    }
    
    struct Credentials: Codable {
        let database: String
        let sessionId: String
        let userName: String
    }
    
    struct Command<P: Codable>: Content {
        let method: Method
        let params: P
    }
    
    struct Result<C: Codable>: Content {
        let result: C
    }
    
    enum Method: String, Codable {
        case get = "Get"
        case add = "Add"
    }
    
    enum TypeName: String, Codable {
        case deviceSearch = "DeviceSearch"
        case device = "Device"
        case deviceStatus = "DeviceStatusInfo"
        case textMessage = "TextMessage"
    }
    
//    struct DeviceStatusInfoSearch: GeotabParams {
//        var typeName: GeotabAPI.TypeName = .deviceStatus
//        var credentials: GeotabAPI.Credentials
//        let deviceSearch: Search
//
//        struct Search: Codable {
//            let Id: String
//        }
//    }
    
    struct Params: GeotabParams {
        let typeName: GeotabAPI.TypeName
        let credentials: GeotabAPI.Credentials
    }
    
    struct Search: GeotabParams {
        let typeName: GeotabAPI.TypeName
        let credentials: GeotabAPI.Credentials
        let search: DeviceSearch
        
        struct DeviceSearch: Codable {
            let deviceSearch: Device
        }
    }
    
    struct TextMessageParams: GeotabParams {
        let typeName: GeotabAPI.TypeName
        let credentials: GeotabAPI.Credentials
        let entity: Entity
        
        struct MessageContent: Codable {
            var contentType: String = "IoxOutput"
            let isRelayOn: Bool
            var channel: String = "1"
        }
        
        struct Entity: Codable {
            let device: Device
            let messageContent: MessageContent
            let isDirectionToVehicle: Bool
        }
    }
    
    struct Message: Content {
        let id: String
    }
//    struct DeviceSearch: GeotabParams {
//        var typeName: GeotabAPI.TypeName = .device
//        var credentials: GeotabAPI.Credentials
//        let serialNumber: String?
//    }
    
    struct Device: Content {
        let id: String
        let serialNumber: String
    }
}

protocol GeotabParams: Codable {
    var typeName: GeotabAPI.TypeName {get}
    var credentials: GeotabAPI.Credentials {get}
}

extension Thing.Status {
    init(_ geotab: GeotabAPI.Status) {
        self.locked = !geotab.isDriving
        self.lockStatus = geotab.isDriving ? .unlocked : .locked
        self.online = geotab.isDeviceCommunicating
        self.coordinate = .init(latitude: geotab.latitude, longitude: geotab.longitude)
        self.charging = nil
        self.batteryLevel = nil
    }
}

