
import Foundation
import HTTPClient

public struct OvalBackend {
    let client: HTTP.Client
    
    public init(_ baseURL: String) {
        client = .init(baseURL)
        
        configure()
    }
    
    public func signIn(with token: String) {
        client.settings.authorization = .custom(value: token)
    }
    
    public var userAgent: String? {
        set {
            client.settings.headers[.userAgent] = newValue
        }
        get {
            client.settings.headers[.userAgent]
        }
    }
    
    public func update<Value>(_ settingsKey: WritableKeyPath<HTTP.Settings, Value>, value: Value) {
        client.settings[keyPath: settingsKey] = value
    }
    
    public func update(header: HTTP.Header, with value: String) {
        client.settings.headers[header] = value
    }
    
    public func get(header: HTTP.Header) -> String? {
        client.settings.headers[header]
    }
    
    public func get<Value: Decodable>(_ endpoint: Endpoint) async throws -> Value! {
        let envelope: Envelope<Value> = try await client.get(endpoint.fullPath, queryItems: endpoint.query)
        return try unwrap(envelope)
    }
    
    public func post<JSON: Encodable, Value: Decodable>(_ json: JSON, endpoint: Endpoint) async throws -> Value! {
        let envelope: Envelope<Value> = try await client.post(json, path: endpoint.fullPath, queryItems: endpoint.query)
        return try unwrap(envelope)
    }
    
    public func put<JSON: Encodable, Value: Decodable>(_ json: JSON, endpoint: Endpoint) async throws -> Value! {
        let envelope: Envelope<Value> = try await client.put(json, path: endpoint.fullPath, queryItems: endpoint.query)
        return try unwrap(envelope)
    }
    
    public func patch<JSON: Encodable, Value: Decodable>(_ json: JSON, endpoint: Endpoint) async throws -> Value! {
        let envelope: Envelope<Value> = try await client.patch(json, path: endpoint.fullPath, queryItems: endpoint.query)
        return try unwrap(envelope)
    }
    
    public func delete<Value: Decodable>(endpoint: Endpoint) async throws -> Value! {
        let envelope: Envelope<Value> = try await client.delete(path: endpoint.fullPath, queryItems: endpoint.query)
        return try unwrap(envelope)
    }
    
    public func upload(data: Data, endpoint: Endpoint, type: UploadType) async throws -> String {
        struct Wrap: Decodable {
            let uploadedUrl: String
        }
        let envelope: Envelope<Wrap> = try await client.upload(multipart: .init(data: data), path: endpoint.fullPath, queryItems: [.init(name: "type", value: type.rawValue)])
        return envelope.payload!.uploadedUrl
    }
    
    func unwrap<Value: Decodable>(_ envelope: Envelope<Value>) throws -> Value! {
        if let error = envelope.error {
            throw error
        } else if let payload = envelope.payload {
            return payload
        } else if Value.self is EmptyJSON.Type {
            return nil
        }
        throw Failure.emptyResponse
    }
    
    func configure() {
        client.settings.decoder.keyDecodingStrategy = .convertFromSnakeCase
        client.settings.decoder.dateDecodingStrategy = .secondsSince1970
        
        client.settings.encoder.keyEncodingStrategy = .convertToSnakeCase
        client.settings.encoder.outputFormatting = .prettyPrinted
    }
}

extension Envelope where Payload == EmptyJSON {
    var ignore: Payload {
        EmptyJSON()
    }
}

extension HTTP.Settings {
    
    public enum DateRepresentation {
        case secondsSince1970
        case iso8601
    }
    
    public var dateRpresentation: DateRepresentation {
        set {
            switch newValue {
            case .iso8601:
                decoder.dateDecodingStrategy = .iso8601
                encoder.dateEncodingStrategy = .iso8601
            case .secondsSince1970:
                decoder.dateDecodingStrategy = .secondsSince1970
                encoder.dateEncodingStrategy = .secondsSince1970
            }
        }
        get {
            switch decoder.dateDecodingStrategy {
            case .iso8601:
                return .iso8601
            default:
                return .secondsSince1970
            }
        }
    }
}



