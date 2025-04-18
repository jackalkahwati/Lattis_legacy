//
//  Error.swift
//  OvalExample
//
//  Created by Ravil Khusainov on 7/10/18.
//  Copyright © 2018 Lattis inc. All rights reserved.
//

import Foundation

public struct ServerError: LocalizedError, Decodable {
    public let code: Int
    public let message: String
    public let name: String
    public let api: API?
    
    func error(_ api: API) -> Error {
        if let c = SessionError.Code(code) {
            return api.error(code: c)
        }
        return ServerError(code: code, message: message, name: name, api: api)
    }
    
    public var errorDescription: String? {
        let desc = api?.path ?? ""
        return "type: Oval.ServerError, code: \(code), name: \(name), message: \(message), description: \(desc)"
    }
    
    enum CodingKeys: String, CodingKey {
        case code, message, name
    }
}

public extension ServerError {
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        code = try container.decode(Int.self, forKey: .code)
        message = try container.decode(String.self, forKey: .message)
        name = try container.decode(String.self, forKey: .name)
        api = nil
    }
}

extension API {
    func error(code: SessionError.Code) -> SessionError {
        return .init(code: code, api: self)
    }
}

public struct SessionError: LocalizedError {
    public let code: Code
    public let api: API
    
    public var errorDescription: String? {
        return "{type: Oval.SessionError, code: \(code), description: \(api.path)}"
    }
    
    public func check(_ errors: Code...) -> Bool {
        return errors.contains(code)
    }
}

public extension SessionError {
    enum Code: LocalizedError {
        case unexpectedResponse
        case emptyData
        case refreshFailed(Int?, String?)
        case badRequest
        case unauthorized
        case forbidden
        case resourceNotFound
        case methodNotAllowed
        case conflict
        case lengthRequired
        case invalidToken
        case internalServer
        case decodingFailed(String)
        
        init?(_ status: Int) {
            switch status {
            case 400:
                self = .badRequest
            case 401:
                self = .unauthorized
            case 403:
                self = .forbidden
            case 404:
                self = .resourceNotFound
            case 405:
                self = .methodNotAllowed
            case 409:
                self = .conflict
            case 411:
                self = .lengthRequired
            case 412:
                self = .invalidToken
            case 500, 502:
                self = .internalServer
            default:
                return nil
            }
        }
        
        public var errorDescription: String? {
            return "Oval.SessionError.Code." + String(describing: self)
        }
    }
}

extension SessionError.Code: Equatable {}
public func ==(lhs: SessionError.Code, rhs: SessionError.Code) -> Bool {
    return String(describing: lhs) == String(describing: rhs)
}

public extension Error {
    var asNSError: NSError {
        if let session = self as? SessionError {
            return NSError(domain: session.errorDescription!, code: 0, userInfo: nil)
        }
        if let server = self as? ServerError {
            return NSError(domain: server.errorDescription!, code: 0, userInfo: nil)
        }
        if let code = self as? SessionError.Code {
            return NSError(domain: code.errorDescription!, code: 0, userInfo: nil)
        }
        return self as NSError
    }
}


