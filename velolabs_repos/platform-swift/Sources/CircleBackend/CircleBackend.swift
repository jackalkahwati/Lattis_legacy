

import Foundation
import HTTPClient

public struct CircleBackend {
    
    let client: HTTP.Client
    
    public init(_ baseURL: String, access: AccessLevel = .operator) {
        client = .init(baseURL + "/\(access.rawValue)")
    }
    
    public init(_ baseURL: URL, access: AccessLevel = .operator) {
        self.init(baseURL.absoluteString, access: access)
    }
    
    public func signIn(with token: String) {
        client.settings.authorization = .bearer(token: token)
    }
    
    public func get<Value: Decodable>(_ endpoint: Endpoint) async throws -> Value! {
        try await client.get(endpoint.fullPath, queryItems: endpoint.query)
    }
    
    public func post<JSON: Encodable, Value: Decodable>(_ json: JSON, endpoint: Endpoint) async throws -> Value! {
        try await client.post(json, path: endpoint.fullPath, queryItems: endpoint.query)
    }
    
    public func put<JSON: Encodable, Value: Decodable>(_ json: JSON, endpoint: Endpoint) async throws -> Value! {
        try await client.put(json, path: endpoint.fullPath, queryItems: endpoint.query)
    }
    
    public func patch<JSON: Encodable, Value: Decodable>(_ json: JSON, endpoint: Endpoint) async throws -> Value! {
        try await client.patch(json, path: endpoint.fullPath, queryItems: endpoint.query)
    }
    
    public func delete<Value: Decodable>(endpoint: Endpoint) async throws -> Value! {
        try await client.delete(path: endpoint.fullPath, queryItems: endpoint.query)
    }
}
