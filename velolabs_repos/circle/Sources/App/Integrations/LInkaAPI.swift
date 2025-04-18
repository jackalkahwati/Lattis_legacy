//
//  LinkaAPI.swift
//  
//
//  Created by Ravil Khusainov on 04.06.2021.
//

import Vapor
import Fluent

enum LinkaAPI {
    
    static let headers: HTTPHeaders = .init([
        ("Accept", "application/json"),
        ("Content-Type", "application/json")
    ])
    
    static var storage: [Int: Token] = [:]
    
    static func metadata(_ fleetId: Int, req: Request) throws -> EventLoopFuture<RequestBuilder> {
        guard let endpoint = Environment.get("LINKA_API_URL") else { throw Abort(.conflict, reason: "No LINKA_API_URL found") }
        if let token = storage[fleetId], token.access_token_expireAt > Date() {
            return req.eventLoop.makeSucceededFuture(.init(token: token.access_token, endpoint: endpoint))
        }
        return Integration<Meta>.query(on: req.db(.main))
            .filter(\.$fleetId == fleetId)
            .filter(\.$type == .linka)
            .first()
            .unwrap(or: Abort(.notFound))
            .flatMapThrowing { integration in
                guard let meta = integration.metadeta else { throw Abort(.notFound) }
//                guard let data = integration.metadeta.data(using: .utf8) else { throw Abort(.notFound)}
//                let meta = try JSONDecoder().decode(Meta.self, from: data)
                let d = try JSONEncoder().encode(LinkaMeta(meta))
                return d
            }
            .flatMap { (meta: Data) in
                let uri = URI(string: endpoint + "/api/merchant_api/fetch_access_token")
                return req.client.post(uri, headers: headers) { request in
                    request.body = .init(data: meta)
                }
                .flatMapThrowing { response in
                    let token = try response.content.decode(Payload<Token>.self, using: JSONDecoder(.safeISO8601)).data
                    storage[fleetId] = token
                    return RequestBuilder(token: token.access_token, endpoint: endpoint)
                }
            }
    }
    
    static func status(thing: Thing, req: Request) throws -> EventLoopFuture<Status> {
        try metadata(thing.fleet.id!, req: req)
            .flatMap { builder in
                let uri = URI(string: builder.endpoint + "/api/merchants/fetch_lock")
                return req.client.put(uri, headers: headers) { request in
                    request.body = builder.body(serial: thing.key)
                }
                .flatMapThrowing { response in
                    let status = try response.content.decode(Payload<Status>.self, using: JSONDecoder(.safeISO8601)).data
                    return status
                }
            }
    }
    
    static func lock(thing: Thing, req: Request) throws -> EventLoopFuture<Command> {
        try metadata(thing.fleet.id!, req: req)
            .flatMap{ builder in
                let uri = URI(string: builder.endpoint + "/api/merchant_api/command_lock")
                return req.client.post(uri, headers: headers) { request in
                    request.body = builder.body(mac: thing.key)
                }
                .flatMapThrowing { response in
                    let command = try response.content.decode(Payload<Command>.self).data
                    return command
                }
            }
    }
    
    static func unlock(thing: Thing, req: Request) throws -> EventLoopFuture<Command> {
        try metadata(thing.fleet.id!, req: req)
            .flatMap{ builder in
                let uri = URI(string: builder.endpoint + "/api/merchant_api/command_unlock")
                return req.client.post(uri, headers: headers) { request in
                    request.body = builder.body(mac: thing.key)
                }
                .flatMapThrowing { response in
                    let command = try response.content.decode(Payload<Command>.self).data
                    return command
                }
            }
    }
    
    static func command(req: Request) throws -> EventLoopFuture<CommandInfo> {
        guard let id = req.parameters.get("id", as: String.self) else { throw Abort(.badRequest, reason: "id not found in request") }
        let fleetId = try req.query.get(Int.self, at: "fleet_id")
        return try metadata(fleetId, req: req)
            .flatMap { builder in
                let uri = URI(string: builder.endpoint + "/api/merchant_api/get_remote_command")
                return req.client.post(uri, headers: headers) { request in
                    request.body = builder.body(command: id)
                }
                .flatMapThrowing { response in
                    let command = try response.content.decode(Payload<CommandInfo>.self).data
                    return command
                }
            }
    }
}

extension LinkaAPI {
    
    struct Status: Content {
        let lock_serial_no: String
        let lock_number: Int
        let latitude: Double
        let longitude: Double
        let lastActivity: Date
        let lastLocation: Date
        let lock_battery_percent: Int
        let active: Bool
        let fw_version: String
        let lastCellPing: Date
        let lastCellLocation: Date
        let lock_state: LockState
    }
    
    enum LockState: String, Codable {
        case locked = "Locked"
        case locking = "Locking"
        case unlocked = "Unlocked"
        case unlocking = "Unlocking"
    }
    
    struct Token: Codable {
        let name: String
        let access_token: String
        let access_token_expireAt: Date
    }
    
    struct Payload<D: Codable>: Codable {
        let data: D
    }
    
    struct RequestBuilder: Content {
        let token: String
        let endpoint: String
        
        func body(serial: String? = nil, mac: String? = nil, command: String? = nil) -> ByteBuffer {
            struct Auth: Codable {
                let access_token: String
                let lock_serial_no: String?
                let mac_addr: String?
                let command_id: String?
            }
            let data = try! JSONEncoder().encode(Auth(access_token: token, lock_serial_no: serial, mac_addr: mac, command_id: command))
            return .init(data: data)
        }
    }
    
    struct Meta: Codable {
        let apikey: String
        let secretKey: String
    }
    
    struct LinkaMeta: Codable {
        let api_key: String
        let secret_key: String
        
        init(_ meta: Meta) {
            api_key = meta.apikey
            secret_key = meta.secretKey
        }
    }
    
    struct Command: Content {
        let command_id: String
    }
    
    struct CommandInfo: Content {
        let status: CommandStatus
//        let date: Date
        let mac_addr: String
        let command: String
    }
    
    enum CommandStatus: Int, Codable {
        case sent, operating, finished
    }
}

extension JSONDecoder.DateDecodingStrategy {
    static let safeISO8601 = custom {
        let container = try $0.singleValueContainer()
        let string = try container.decode(String.self)
        let formatter = ISO8601DateFormatter()
        if let date = formatter.date(from: string) {
            return date
        }
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        if let date = formatter.date(from: string) {
            return date
        }
        throw DecodingError.dataCorruptedError(in: container, debugDescription: "Invalid date: \(string)")
    }
}

extension JSONDecoder {
    convenience init(_ strategy: DateDecodingStrategy) {
        self.init()
        self.dateDecodingStrategy = strategy
    }
}

