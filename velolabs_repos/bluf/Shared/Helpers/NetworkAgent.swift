//
//  NetworkAgent.swift
//  BLUF
//
//  Created by Ravil Khusainov on 20.09.2020.
//

import Foundation
import Combine

public struct Agent {
    
    public enum ErrorCode: Int, Error {
        case notFound = 404
        case acceessDenied = 401
        case badRequest = 400
    }
    
    public struct Empty: Codable {}
    
    public init(_ endpoint: String) {
        self.endpoint = endpoint
    }

    public let endpoint: String
    
    public func run<T: Decodable>(_ requestBuilder: URLRequest.Builder,
                           decoder: JSONDecoder = JSONDecoder(),
                           queue: DispatchQueue = .main) -> AnyPublisher<T, Error> {
        do {
            let request = try requestBuilder.build(with: endpoint)
            return URLSession.shared
                .dataTaskPublisher(for: request)
                .tryMap { result -> T in
                    if let ht = result.response as? HTTPURLResponse,
                       let err = ErrorCode(rawValue: ht.statusCode) {
                        throw err
                    }
                    let value = try decoder.decode(T.self, from: result.data)
//                    print(value)
                    return value
                }
                .receive(on: queue)
                .eraseToAnyPublisher()
        } catch {
            return Fail(error: error)
                .eraseToAnyPublisher()
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
        
        public func build(with endpoint: String) throws -> URLRequest {
            guard var components = URLComponents(string: endpoint) else { throw BuilderError.invalidEndpoint(endpoint) }
            components.path = "/" + path
            components.queryItems = queryItems
            guard let url = components.url else { throw BuilderError.invalidURL(components) }
            var request = URLRequest(url: url)
            request.httpBody = body
            request.httpMethod = method
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
        
        public static func put<T: Encodable>(_ json: T, path: String, encoder: JSONEncoder = JSONEncoder()) -> Builder {
            .init(path: path, method: "PUT", body: try? encoder.encode(json))
        }
        
        public static func patch<T: Encodable>(_ json: T, path: String, encoder: JSONEncoder = JSONEncoder()) -> Builder {
            .init(path: path, method: "PATCH", body: try? encoder.encode(json))
        }
    }
    
    public enum BuilderError: Error {
        case invalidEndpoint(String)
        case invalidURL(URLComponents)
    }
}


