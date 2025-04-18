//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 16.02.2022.
//

import Vapor
import Fluent

protocol RequestClientProxy {
    func headers(_ req: Request) async throws -> HTTPHeaders
}

final class SASController: RouteCollection {
    
    fileprivate var storage: TokenStorage?
    
    fileprivate func uri(path: String, query: String? = nil) -> URI {
        var base = URI(string: "https://qa-api.smart-access-solutions.com")
        base.path = path
        base.query = query
        return base
    }
    
    fileprivate func fetchToken(_ req: Request) async throws -> String {
        if let storage = storage, storage.isValid {
            return storage.token
        }
        let uri = URI(string: "https://lattis-qa-sas-cloud-core.auth.eu-central-1.amazoncognito.com/oauth2/token?grant_type=client_credentials")
        var headers = HTTPHeaders([("Accept", "application/json"), ("Content-Type", "application/x-www-form-urlencoded")])
        headers.basicAuthorization = .init(username: "571d55immr0lj2u8ht52nv0a4c", password: "ktekledd3gvpmqhu97rnikchjvl95v9s4tc5ev0oqre8tulv6sk")
        let response = try await req.client.post(uri, headers: headers)
        let token = try response.content.decode(TokenResponse.self)
        storage = .init(token: token.access_token, validBy: Date().addingTimeInterval(token.expires_in))
        return token.access_token
    }
    
    func headers(_ req: Request) async throws -> HTTPHeaders {
        var value = HTTPHeaders()
        value.add(name: .accept, value: "application/json")
        value.add(name: .contentType, value: "application/json")
        let token = try await fetchToken(req)
        value.add(name: .authorization, value: token)
        return value
    }
    
    func boot(routes: RoutesBuilder) throws {
        routes.post("credentials", use: credentials)
    }
}

extension SASController: RequestClientProxy {
    func credentials(_ req: Request) async throws -> Credentials {
        return try await get(uri(path: ""), req: req)
    }
}

extension SASController {
    struct Credentials: Content {
        let token: String
    }
    
    struct TokenResponse: Content {
        let access_token: String
        let expires_in: Double
        let token_type: String
    }
    
    struct TokenStorage {
        let token: String
        let validBy: Date
        
        var isValid: Bool {
            validBy > Date()
        }
    }
    
    struct AccessRights: Content {
        let userid: String
        let lockid: String
    }
}

extension RequestClientProxy {
    func `get`<Value: Content>(_ uri: URI, req: Request) async throws -> Value {
        let head = try await headers(req)
        let resp = try await req.client.get(uri, headers: head)
        return try resp.content.decode(Value.self)
    }
    
    func post<Value: Content, Body: Content>(_ uri: URI, body: Body? = nil, req: Request) async throws -> Value {
        let head = try await headers(req)
        let resp = try await req.client.post(uri, headers: head) { req in
            if let body = body {
                try req.content.encode(body)
            }
        }
        return try resp.content.decode(Value.self)
    }
}
