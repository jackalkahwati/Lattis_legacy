//
//  NetworkAgent.swift
//  BLUF
//
//  Created by Ravil Khusainov on 20.09.2020.
//

import Foundation
import Combine
import HTTPStatusCodes

public struct NetworkAgent {
    
    public init(_ endpoint: String) {
        self.endpoint = endpoint
    }

    public let endpoint: String
    public var authorization: Authorization?
    
    public func run<T: Decodable>(_ requestBuilder: URLRequest.Builder,
                           decoder: JSONDecoder = JSONDecoder(),
                           queue: DispatchQueue = .main) -> AnyPublisher<T, Error> {
        actualRun(requestBuilder, queue: queue) { (data, response) in
            try response.success()
            decoder.dateDecodingStrategy = .secondsSince1970
            let value = try decoder.decode(T.self, from: data)
            return value
        }
    }
    
    public func run(_ requestBuilder: URLRequest.Builder,
                           queue: DispatchQueue = .main) -> AnyPublisher<Void, Error> {
        actualRun(requestBuilder, queue: queue) { (data, response) in
            try response.success()
            return ()
        }
    }
    
    private func actualRun<Value>(_ requestBuilder: URLRequest.Builder,
                           queue: DispatchQueue,
                           transform: @escaping ((Data, URLResponse) throws -> Value)) -> AnyPublisher<Value, Error> {
        do {
            let request = try requestBuilder.build(with: endpoint, authorization: authorization)
            return URLSession.shared
                .dataTaskPublisher(for: request)
                .tryMap({ (data, response) in
                    do {
                        #if DEBUG
                        self.log(request: request, response: response, data: data, error: nil)
                        #endif
                        return try transform(data, response)
                    } catch {
                        #if DEBUG
                        self.log(request: request, response: response, data: nil, error: error)
                        #endif
                        throw error
                    }
                })
                .receive(on: queue)
                .eraseToAnyPublisher()
        } catch {
            return Fail(error: error)
                .eraseToAnyPublisher()
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
    
    enum Failure: Error {
        case http(HTTPStatusCode)
        case notHTTPResponse
    }
        
    enum Authorization {
        case bearer(String)
        case lattis(String)
    }
}

extension Error {
    func isHTTP(_ code: HTTPStatusCode) -> Bool {
        guard let error = self as? NetworkAgent.Failure, case let .http(c) = error else { return false }
        return c == code
    }
}

extension URLResponse {
    func http() throws -> HTTPURLResponse {
        guard let http = self as? HTTPURLResponse else { throw NetworkAgent.Failure.notHTTPResponse }
        return http
    }
    
    func isSuccess() throws -> Bool {
        try http().statusCodeEnum.isSuccess
    }
    
    func success() throws {
        let code = try http().statusCodeEnum
        if !code.isSuccess {
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

