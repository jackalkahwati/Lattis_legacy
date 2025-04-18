//
//  SegwayAPI.swift
//  
//
//  Created by Ravil Khusainov on 10.03.2021.
//

import Vapor
import Foundation

struct SegwayAPI {
    
    private static var storage: [Region: Store] = [:]
    
    private static func getToken(_ client: Client, region: Region) throws -> EventLoopFuture<RequestBuilder> {
        guard let endpoint = region.endpoint else { throw Abort(.conflict, reason: "No value set for SEGWAY_API_URL") }
        guard let id = region.clientId else { throw Abort(.conflict, reason: "No value set for SEGWAY_CLIENT_ID") }
        guard let secret = region.clientSecret else { throw Abort(.conflict, reason: "No value set for SEGWAY_CLIENT_SECRET") }
        if let store = storage[region], store.valid {
            return client.eventLoop.makeSucceededFuture(.init(token: store.token.access_token, endpoint: endpoint))
        }
        let url = URI(string: endpoint + "/oauth/token")
        let bodyString = "client_id=\(id)&client_secret=\(secret)&grant_type=client_credentials".addingPercentEncoding(withAllowedCharacters: .urlFragmentAllowed)
        let body = bodyString?.data(using: .utf8)
        let headers: HTTPHeaders = .init(
            [
                ("Accept", "application/json"),
                ("Content-Type", "application/x-www-form-urlencoded")
            ]
        )
        return client.post(url, headers: headers) { (request) in
            request.body = .init(data: body!)
        }
        .flatMapThrowing { response in
            let result = try response.content.decode(Token.self)
            storage[region] = .init(token: result)
            return .init(token: result.access_token, endpoint: endpoint)
        }
    }
    
    static func getStatus(_ imei: String, client: Client, region: Region = .us) throws -> EventLoopFuture<Status> {
        try getToken(client, region: region)
            .flatMap { builder in
                var url = URI(string: builder.endpoint + "/api/v2/vehicle/query/current/status")
                url.query = "iotCode=\(imei)"
                return client.get(url, headers: builder.headers())
                    .flatMapThrowing { response -> Status in
                        let status = try response.content.decode(Status.self)
                        return status
                    }
                    .flatMap { status in
                        var locationURL = URI(string: builder.endpoint + "/api/v2/vehicle/query/current/location")
                        locationURL.query = "iotCode=\(imei)"
                        return client.get(locationURL, headers: builder.headers())
                            .flatMapThrowing { response in
                                let loc = try response.content.decode(Location.self)
                                var st = status
                                st.latitude = loc.data?.latitude
                                st.longitude = loc.data?.longitude
                                return status
                            }
                    }
            }
    }
    
    static func lock(_ imei: String, client: Client, region: Region = .us) throws -> EventLoopFuture<HTTPStatus> {
        try getToken(client, region: region)
            .flatMap {
                client.post(imei: imei, path: "/api/v2/vehicle/control/lock", builder: $0)
                    .flatMapThrowing(handleV2)
            }
    }
    
    static func unlock(_ imei: String, client: Client, region: Region = .us) throws -> EventLoopFuture<HTTPStatus> {
        try getToken(client, region: region)
            .flatMap {
                client.post(imei: imei, path: "/api/v2/vehicle/control/unlock", builder: $0)
                    .flatMapThrowing(handleV2)
            }
    }
    
    static func uncoverBattery(_ imei: String, client: Client, region: Region = .us) throws -> EventLoopFuture<HTTPStatus> {
        try getToken(client, region: region)
            .flatMap {
                client.post(imei: imei, path: "/api/v2/vehicle/control/battery-cover", builder: $0)
                    .flatMapThrowing(handleV2)
            }
    }
    
    static func light(_ body: Light, client: Client, region: Region = .us) throws -> EventLoopFuture<HTTPStatus> {
        try getToken(client, region: region)
            .flatMap {
                return client.control(body, path: "/api/v2/vehicle/control/light", builder: $0)
                    .flatMapThrowing(handleV2)
            }
    }
    
    static func sound(_ body: Sound, client: Client, region: Region = .us) throws -> EventLoopFuture<HTTPStatus> {
        try getToken(client, region: region)
            .flatMap {
                let path = body.workMode != nil ? "/api/v2/vehicle/setting/sound" : "/api/v2/vehicle/control/sound"
                return client.control(body, path: path, builder: $0)
                    .flatMapThrowing(handleV2)
            }
    }
    
    private static func handle(resp: ClientResponse) throws -> HTTPStatus {
        let reply = try resp.content.decode(Reply.self)
        if reply.success { return .ok }
        if let message = reply.message {
            throw Abort(.conflict, reason: message)
        }
        return resp.status
    }
    
    private static func handleV2(resp: ClientResponse) throws -> HTTPStatus {
        let reply = try resp.content.decode(ReplyV2.self)
        if reply.msg == "Success" { return .ok }
        throw Abort(.conflict, reason: reply.msg)
    }
}

fileprivate extension Client {
    func post(imei: String, path: String, builder: SegwayAPI.RequestBuilder) -> EventLoopFuture<ClientResponse> {
        let url = URI(string: builder.endpoint + path)
        return self.post(url, headers: builder.headers()) { request in
            try request.content.encode(SegwayAPI.Body(imei))
        }
    }
    
    func control<Body: Content>(_ body: Body, path: String, builder: SegwayAPI.RequestBuilder) -> EventLoopFuture<ClientResponse> {
        let url = URI(string: builder.endpoint + path)
        return self.post(url, headers: builder.headers()) { request in
            try request.content.encode(body)
        }
    }
}

extension SegwayAPI {
    enum Failure: Error {
        case badControlRequest
    }
    struct Status: Content, CoordinateContaining {
        let data: Data
        var latitude: Double?
        var longitude: Double?
        
        struct Data: Content {
            let online: Bool?
            let locked: Bool
            let powerPercent: Int
            let charging: Bool
        }
    }
    
    struct Location: Content {
        let data: Data?
        
        struct Data: Codable, CoordinateContaining {
            let longitude: Double?
            let latitude: Double?
        }
    }
    
    struct Token: Content {
        let access_token: String
        let expires_in: TimeInterval
    }
    
    struct Store {
        let token: Token
        let receivedAt = Date()
        
        var valid: Bool {
            -receivedAt.timeIntervalSinceNow < token.expires_in
        }
    }
    
    struct Body: Content {
        let iotCode: String
        let vehicleCode: String
        
        init(_ imei: String) {
            iotCode = imei
            vehicleCode = imei
        }
    }
    
    struct Light: Content {
        let iotCode: String
        let headLight: Thing.Light.State?
        let tailLight: Thing.Light.State?
        
        init(_ code: String, light: Thing.Light) {
            self.iotCode = code
            self.headLight = light.headLight
            self.tailLight = light.tailLight
        }
    }
    
    struct Sound: Content {
        let iotCode: String
        let controlType: Thing.Sound.Control?
        let workMode: Thing.Sound.Mode?
        
        init(_ code: String, sound: Thing.Sound) {
            self.iotCode = code
            self.controlType = sound.controlType
            self.workMode = sound.workMode
        }
    }
    
    struct Reply: Content {
        let success: Bool
        let message: String?
        let code: Int?
    }
    
    struct ReplyV2: Content {
        let msg: String
        let code: Int
    }
    
    struct RequestBuilder: Content {
        let token: String
        let endpoint: String
        
        func headers() -> HTTPHeaders {
            .init([("Authorization","Bearer \(token)")])
        }
    }
    
    enum Region {
        case us
        case eu
        
        var endpoint: String? {
            switch self {
            case .eu: return Environment.get("SEGWAY_EU_API_URL")
            case .us: return Environment.get("SEGWAY_API_URL")
            }
        }
        
        var clientId: String? {
            switch self {
            case .eu: return Environment.get("SEGWAY_EU_CLIENT_ID")
            case .us: return Environment.get("SEGWAY_CLIENT_ID")
            }
        }
        
        var clientSecret: String? {
            switch self {
            case .eu: return Environment.get("SEGWAY_EU_CLIENT_SECRET")
            case .us: return Environment.get("SEGWAY_CLIENT_SECRET")
            }
        }
    }
}
