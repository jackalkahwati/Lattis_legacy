//
//  NetworkAgent.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 25.01.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import Combine
//import HTTPStatusCodes

public struct NetworkAgent {
    
    public init(_ endpoint: String) {
        self.endpoint = endpoint
    }

    public let endpoint: String
    public var authorization: Authorization?
    
    public func run<T: Decodable>(_ requestBuilder: URLRequest.Builder,
                                  decoder: JSONDecoder = JSONDecoder(),
                                  queue: DispatchQueue = .main) async throws -> T {
        let request = try requestBuilder.build(with: endpoint, authorization: authorization)
        let (data, response) = try await URLSession.shared.data(for: request)
        do {
            try response.success()
            decoder.dateDecodingStrategy = .secondsSince1970
            let value = try decoder.decode(T.self, from: data)
            #if DEBUG
            self.log(request: request, response: response, data: data, error: nil)
            #endif
            return value
        } catch {
            #if DEBUG
            self.log(request: request, response: response, data: data, error: error)
            #endif
            throw error
        }
    }
    
    public func run(_ requestBuilder: URLRequest.Builder, queue: DispatchQueue = .main) async throws {
        let request = try requestBuilder.build(with: endpoint, authorization: authorization)
        let (data, response) = try await URLSession.shared.data(for: request)
        do {
            try response.success()
            #if DEBUG
            self.log(request: request, response: response, data: data, error: nil)
            #endif
        } catch {
            #if DEBUG
            self.log(request: request, response: response, data: data, error: error)
            #endif
            throw error
        }
    }
    
    private func log(request: URLRequest, response: URLResponse, data: Data?, error: Error?) {
        if let data = request.httpBody, let str = String(prettyPrint: data) {
            print(str)
        }
        if let method = request.httpMethod, let endpoint = request.url?.absoluteString {
            print(method, endpoint)
        }
        if let resp = response as? HTTPURLResponse {
            print("status code:", resp.statusCode)
        }
        if let data = data, let str = String(prettyPrint: data) {
            print("response body:", str)
        } else if let data = data, let str = String(data: data, encoding: .utf8) {
            print("response body:", str)
        } else if let error = error {
            print("Failed with:", error)
        }
    }
}

extension URLRequest {
    public struct Builder {
        internal init(path: String, method: String = "GET", body: Data? = nil, queryItems: [URLQueryItem] = []) {
            self.path = path
            self.method = method
            self.body = body
            self.queryItems = queryItems
        }
        
        let path: String
        let method: String
        let body: Data?
        let queryItems: [URLQueryItem]
        
        public func build(with endpoint: String, authorization: NetworkAgent.Authorization? = nil) throws -> URLRequest {
            guard var components = URLComponents(string: endpoint) else { throw BuilderError.invalidEndpoint(endpoint) }
            components.path = "/" + path
            components.queryItems = queryItems
            guard let url = components.url else { throw BuilderError.invalidURL(components) }
            var request = URLRequest(url: url)
            request.httpBody = body
            request.httpMethod = method
            request.addValue("application/json", forHTTPHeaderField: "Content-Type")
            request.addValue("application/json", forHTTPHeaderField: "Accept")
            request.addValue("lattis", forHTTPHeaderField: "User-Agent")
            if let count = body?.count {
                request.addValue("\(count)", forHTTPHeaderField: "Content-Length")
            }
            if let auth = authorization {
                switch auth {
                case .bearer(let token):
                    request.addValue("Bearer " + token, forHTTPHeaderField: "Authorization")
                case .lattis(let token):
                    request.addValue(token, forHTTPHeaderField: "Authorization")
                }
            }
            return request
        }
        
        public static func get(_ path: String, queryItems: [URLQueryItem] = []) -> Builder {
            .init(path: path, queryItems: queryItems)
        }
        
        public static func delete(_ path: String) -> Builder {
            .init(path: path, method: "DELETE")
        }
        
        public static func post<T: Encodable>(_ json: T, path: String, encoder: JSONEncoder = JSONEncoder()) -> Builder {
            .init(path: path, method: "POST", body: try? encoder.encode(json))
        }
        
        public static func put<T: Encodable>(_ json: T, path: String, queryItems: [URLQueryItem] = [], encoder: JSONEncoder = JSONEncoder()) -> Builder {
            .init(path: path, method: "PUT", body: try? encoder.encode(json), queryItems: queryItems)
        }
        
        public static func put(_ path: String, queryItems: [URLQueryItem] = [], encoder: JSONEncoder = JSONEncoder()) -> Builder {
            .init(path: path, method: "PUT", queryItems: queryItems)
        }
        
        public static func patch<T: Encodable>(_ json: T, path: String, encoder: JSONEncoder = JSONEncoder(), queryItems: [URLQueryItem] = []) -> Builder {
            encoder.dateEncodingStrategy = .secondsSince1970
            return .init(path: path, method: "PATCH", body: try? encoder.encode(json), queryItems: queryItems)
        }
    }
    
    public enum BuilderError: Error {
        case invalidEndpoint(String)
        case invalidURL(URLComponents)
    }
}

public extension NetworkAgent {
    init(_ url: URL) {
        self.init(url.absoluteString)
    }
        
    enum Authorization {
        case bearer(String)
        case lattis(String)
    }
    
    enum Failure: Error {
        case http(Int)
        case notHTTPResponse
    }
}

//extension Error {
//    func isHTTP(_ code: HTTPStatusCode) -> Bool {
//        guard let error = self as? NetworkAgent.Failure, case let .http(c) = error else { return false }
//        return c == code
//    }
//}

extension URLResponse {
    func http() throws -> HTTPURLResponse {
        guard let http = self as? HTTPURLResponse else { throw NetworkAgent.Failure.notHTTPResponse }
        return http
    }
//
//    func isSuccess() throws -> Bool {
//        try http().statusCodeEnum.isSuccess
//    }

    func success() throws {
        let code = try http().statusCode
        if !code.in([200, 201, 202]) {
            throw NetworkAgent.Failure.http(code)
        }
    }
}

extension String {
    init?(prettyPrint: Data) {
        if let json = try? JSONSerialization.jsonObject(with: prettyPrint, options: .mutableContainers) {
            if let prettyPrintedData = try? JSONSerialization.data(withJSONObject: json, options: .prettyPrinted) {
                self.init(data: prettyPrintedData, encoding: .utf8)
                return
            }
        }
        return nil
    }
}


extension Int {
    func `in`(_ array: [Self]) -> Bool {
        return array.contains(self)
    }
}
    
