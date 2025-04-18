//
//  Resource.swift
//  RestService
//
//  Created by Ravil Khusainov on 18/12/2016.
//  Copyright © 2016 Lattis. All rights reserved.
//

import Foundation
import SwiftyJSON

public struct Resource<A> {
    public let path: String
    public let params: [String: Any]
    public let jsonData: [String: Any]?
    public let parse: (JSON) -> A?
    public var method: HttpMethod
    
    public init(path: String, params: [String: Any] = [:], jsonData: [String: Any]? = nil, parse: @escaping (JSON) -> A?, httpMethod: HttpMethod = .get) {
        self.path = path
        self.parse = parse
        self.params = params + httpMethod.params
        self.jsonData = jsonData
        method = httpMethod
    }
}

public enum HttpMethod: String {
    case post = "POST"
    case get = "GET"
    case put = "PUT"
}

public extension Resource {
    internal func request(for endpoint: String) -> URLRequest? {
        
        guard let url = URL(string: endpoint + path) else { return nil }
        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        if let data = jsonData {
            print("parameters:\n\(data)")
            request.httpBody = try? JSON(data).rawData()
        }
        
        
        for (key, value) in  params {
            request.setValue(value as? String, forHTTPHeaderField: key)
        }
        return request
    }
}

extension HttpMethod {
    var params: [String: Any] {
        switch self {
        case .post, .put:
            return [
                "Content-Type": "application/json",
                "Accept": "application/json",
            ]
        default:
            return [:]
        }
    }
}


func + <K, V> ( left: [K:V], right: [K:V]) -> [K:V] {
    var result = left
    for (k, v) in right {
        result.updateValue(v, forKey: k)
    }
    return result
}
