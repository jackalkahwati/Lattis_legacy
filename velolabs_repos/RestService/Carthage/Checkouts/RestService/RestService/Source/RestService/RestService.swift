//
//  RestService.swift
//  RestService
//
//  Created by Ravil Khusainov on 18/12/2016.
//  Copyright © 2016 Lattis. All rights reserved.
//

import Foundation
import SwiftyJSON

public final class RestService {
    
    internal let session: URLSession = {
        let configuration = URLSessionConfiguration.default
        configuration.httpAdditionalHeaders = ["Accept": "application/json"]
        let session = URLSession(configuration: configuration)
        return session
    }()
    let endpoint: String
    private let defaultTimeout: TimeInterval = 30
    private var parser: ((Data) -> JSON?) = { data in
        return JSON(data: data)
    }
    
    public init(_ endpoint: String, parser: ((Data) -> JSON?)? = nil) {
        self.endpoint = endpoint
        if parser != nil {
            self.parser = parser!
        }
    }
    
    public func load<A>(_ resource: Resource<A>, success: @escaping (A) -> (), fail: @escaping (Swift.Error) -> ()) {
        guard var request = resource.request(for: endpoint) else { return fail(Error.invalidURL( endpoint + resource.path)) }
        request.timeoutInterval = defaultTimeout
        perform(request: request, parse: parser, success: { json in
            guard let result = resource.parse(json) else { return fail(Error.invalidParsing(json.string)) }
            success(result)
        }, fail: fail)
    }
    
    internal func perform<A>(request: URLRequest, parse: @escaping (Data) -> A?, success: @escaping (A) -> (), fail: @escaping (Swift.Error) -> ()) {
        session.dataTask(with: request, completionHandler: { (rawData, _, error) in
            guard error == nil else { return fail(error!) }
            guard let data = rawData else { return fail(Error.invalidData) }
            guard let result = parse(data) else { return fail(Error.invalidParsing(String(data: data, encoding: .utf8))) }
            print("request: \(String(describing: request.url))\nresponce:\n\(result)")
            success(result)
        }).resume()
    }
}

public extension RestService {
    public enum Error: Swift.Error {
        case invalidURL(String)
        case invalidData
        case invalidParsing(String?)
        case server(String)
    }
    
    public enum ResponseCode: UInt {
        case forbidden = 401
        case notFound = 400
        case ststusOk = 800
    }
    
}


