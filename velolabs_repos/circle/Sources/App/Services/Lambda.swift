//
//  Lambda.swift
//  
//
//  Created by Ravil Khusainov on 20.10.2021.
//

import Vapor

enum Lambda {
    struct ECDH: RestAPI {
        static func builder(_ path: String) throws -> RESTBuilder {
            guard let apiKey = Environment.get("ECDH_API_KEY") else { throw Abort(.conflict, reason: "No ECDH_API_KEY found") }
            guard let baseUrl = Environment.get("ECDH_API_URL") else { throw Abort(.conflict, reason: "No ECDH_API_URL found") }
            let uri = URI(string: baseUrl + path)
            return .init(headers: .init([
                ("x-api-key", apiKey),
                ("Accept", "application/json"),
                ("Content-Type", "application/json"),
                ("Connection", "keep-alive")
            ]), uri: uri)
        }
        
        static func decrypt(_ value: String, client: Client) throws -> EventLoopFuture<String> {
            try builder("/ecdh/decrypt").build { uri, headers in
                client.put(uri, headers: headers) { request in
                    try request.content.encode(Body(value: value, type: nil))
                }
                .flatMapThrowing { response in
                    let body = try response.content.decode(Body.self)
                    return body.value
                }
            }
        }
        
        static func encrypt(_ value: String, client: Client) throws -> EventLoopFuture<String> {
            try builder("/ecdh/encrypt").build { uri, headers in
                client.put(uri, headers: headers) { request in
                    try request.content.encode(Body(value: value, type: nil))
                }
                .flatMapThrowing { response in
                    let body = try response.content.decode(Body.self)
                    return body.value
                }
            }
        }
        
        static func publicKey(_ privateKey: String, client: Client) throws -> EventLoopFuture<String> {
            try builder("/ecdh/key").build { uri, headers in
                client.put(uri, headers: headers) { request in
                    try request.content.encode(Body(value: privateKey, type: nil))
                }
                .flatMapThrowing { response in
                    let body = try response.content.decode(Body.self)
                    return body.value
                }
            }
        }
        
        struct Body: Content {
            let value: String
            let type: String?
        }
    }
    
    struct KeyMaster: RestAPI {
        static func builder(_ path: String) throws -> RESTBuilder {
            guard let baseUrl = Environment.get("KEY_MASTER_API_URL") else { throw Abort(.conflict, reason: "No KEY_MASTER_API_URL found") }
            let uri = URI(string: baseUrl + path)
            return .init(headers: .init([
                ("Accept", "application/json"),
                ("Content-Type", "application/json")
            ]), uri: uri)
        }
        
        static func signedMessage(client: Client, req: KeyRequest) throws -> EventLoopFuture<String> {
            try builder("/api/v1/signed-message").build { uri, headers in
                client.post(uri, headers: headers) { request in
                    try request.content.encode(req)
                }
                .flatMapThrowing { response in
                    let result = try response.content.decode(Body.self)
                    return result.payload.signed_message
                }
            }
        }
        
        struct Body: Codable {
            let payload: Payload
        }
        
        struct Payload: Codable {
            let signed_message: String
        }
        
        struct KeyRequest: Content {
            internal init(mac_id: String, user_id: String, private_key: String, public_key: String, time: String = "ffffffff", security: String = "00", owner: String = "00") {
                self.mac_id = mac_id
                self.user_id = user_id
                self.private_key = private_key
                self.public_key = public_key
                self.time = time
                self.security = security
                self.owner = owner
            }
            
            let mac_id: String
            let user_id: String
            let private_key: String
            let public_key: String
            let time: String
            let security: String
            let owner: String
        }
    }
}

struct RESTBuilder {
    let headers: HTTPHeaders
    let uri: URI
    
    func build<T>(comletion: (URI, HTTPHeaders) -> EventLoopFuture<T>) -> EventLoopFuture<T> {
        comletion(uri, headers)
    }
}

protocol RestAPI {
    static func builder(_ path: String) throws -> RESTBuilder
}
