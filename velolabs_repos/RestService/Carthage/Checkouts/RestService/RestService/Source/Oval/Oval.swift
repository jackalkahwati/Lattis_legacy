//
//  Oval.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 19/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import Foundation
import SwiftyJSON
import KeychainSwift

public struct Oval {
    public typealias fail = (Swift.Error) -> ()
    private static let tokenKey = "rest.token"
    private static let refreshTokenKey = "refresh.token"
    private static let userIdKey = "user.id"
    private static let keychain = KeychainSwift()
    
    public internal(set) static var restToken: String? {
        set {
            if let token = newValue {
                keychain.set(token, forKey: tokenKey, withAccess: .accessibleAfterFirstUnlock)
            } else {
                keychain.delete(tokenKey)
            }
        }
        get {
            return keychain.get(tokenKey)
        }
    }
    
    public internal(set) static var refreshToken: String? {
        set {
            if let token = newValue {
                keychain.set(token, forKey: refreshTokenKey, withAccess: .accessibleAfterFirstUnlock)
            } else {
                keychain.delete(refreshTokenKey)
            }
        }
        get {
            return keychain.get(refreshTokenKey)
        }
    }
    
    public internal(set) static var userId: Int32? {
        set {
            if let userId = newValue {
                keychain.set(String(userId), forKey: userIdKey, withAccess: .accessibleAfterFirstUnlock)
            } else {
                keychain.delete(userIdKey)
            }
        }
        get {
            if let userId = keychain.get(userIdKey) {
                return Int32(userId)
            }
            return nil
        }
    }
    
    public static func login(with userId: Int32, restToken: String, refreshToken: String) {
        self.userId = userId
        self.restToken = restToken
        self.refreshToken = refreshToken
    }
    
    public static func logout() {
        userId = nil
        restToken = nil
        refreshToken = nil
    }
    
    open class Route {
        public init() {}
        internal let restService = RestService.oval
        
        internal func map<A>(result: Oval.Responce, parse: (JSON) -> A?) throws -> A {
            guard result.error == nil else { throw Error(rawValue: result.statusCode) ?? result.error! }
            guard let payload = result.payload else { throw Error.noPayload }
            guard let resp = parse(payload) else { throw Error.badResponce }
            return resp
        }
        
        internal func check(status: Int, of result: Oval.Responce) throws {
            guard result.error == nil else { throw Error(rawValue: result.statusCode) ?? result.error! }
            guard result.statusCode == status else { throw Error.missingStatusCode }
        }
        
        internal func refreshTokens(success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.refreshToken, let userId = Oval.userId else { return fail(Error.missingRefreshToken) }
            let reg = Oval.post(path: "users/refresh-tokens/", post: ["user_id": userId, "refresh_token": token])
            let parse: (JSON) -> (String, String)? = { json in
                if let restToken = json["rest_token"].string, let refreshToken = json["refresh_token"].string {
                    return (restToken, refreshToken)
                }
                return nil
            }
            perform(resource: reg, parse: parse, fail: fail) { (response) in
                Oval.restToken = response.0
                Oval.refreshToken = response.1
                success()
            }
        }
        
        internal func handle(_ error: Swift.Error, retry: @escaping () -> (), fail: @escaping fail) {
            if let error = error as? Error, error == .invalidToken {
                refreshTokens(success: retry, fail: fail)
            } else {
                main { fail(error) }
            }
        }
        
        open func perform<A>(resource: Resource<Responce>, parse: @escaping (JSON) -> A?, retry: (() -> ())? = nil, fail: @escaping fail, mapped: @escaping (A) -> ()) {
            restService.load(resource, success: { (result) in
                do {
                    let responce = try self.map(result: result, parse: parse)
                    main { mapped(responce) }
                } catch {
                    if let retry = retry {
                        self.handle(error, retry: retry, fail: { error in main { fail(error) } })
                    } else {
                        main { fail(error) }
                    }
                }
            }, fail: { error in main { fail(error) } })
        }
        
        open func perform(resource: Resource<Responce>, check: Int, retry: (() -> ())? = nil, fail: @escaping fail, checked: @escaping (() -> ())) {
            restService.load(resource, success: { (result) in
                do {
                    try self.check(status: check, of: result)
                    main { checked() }
                } catch {
                    if let retry = retry {
                        self.handle(error, retry: retry, fail: { error in main { fail(error) } })
                    } else {
                        main { fail(error) }
                    }
                }
            }, fail: { error in main { fail(error) } })
        }
    }
    
    public struct Responce {
        let statusCode: Int
        let payload: JSON?
        let error: Swift.Error?
    }
    
    public enum Error: Int, Swift.Error {
        case badResponce
        case noPayload
        case missingStatusCode
        case missingRefreshToken
        case badRequest = 400
        case unauthorized = 401
        case forbidden = 403
        case resourceNotFound = 404
        case methodNotAllowed = 405
        case conflict = 409
        case lengthRequired = 411
        case invalidToken = 412
        case internalServer = 500
    }
    
    public static func post(path: String, post: [String: Any]? = nil, restToken: String? = nil) -> Resource<Responce> {
        let params: [String: Any] = restToken == nil ? [:] : ["authorization": restToken!]
        return Resource<Responce>(path: path, params: params, jsonData: post, parse: Responce.parse, httpMethod: .post)
    }
    
    public static func get(path: String, post: [String: Any]? = nil, restToken: String? = nil) -> Resource<Responce> {
        let params: [String: Any] = restToken == nil ? [:] : ["authorization": restToken!]
        return Resource<Responce>(path: path, params: params, jsonData: post, parse: Responce.parse)
    }
    
    public static func put(path: String, post: [String: Any]? = nil, restToken: String? = nil) -> Resource<Responce> {
        let params: [String: Any] = restToken == nil ? [:] : ["authorization": restToken!]
        return Resource<Responce>(path: path, params: params, jsonData: post, parse: Responce.parse, httpMethod: .put)
    }
}

public extension Oval.Responce {
    public var success: Bool {
        return statusCode == 200 || statusCode == 201
    }
    
    public static let parse: (JSON) -> Oval.Responce? = { dict in
        guard let statusCode = dict["status"].int else { return nil }
        var error: RestService.Error?
        if let message = dict["error"]["message"].string {
            error = .server(message)
        }
        return Oval.Responce(statusCode: statusCode, payload: dict["payload"], error: error)
    }
}

public extension RestService {
    private static var endpoint: String {
        let defaultServer = "http://oval-dev.us-west-1.elasticbeanstalk.com/api/"
        guard let endpoint = Bundle.main.infoDictionary?["OvalApiEndpoint"] as? String else {
            print("Error: 'OvalApiEndpoint' key is not specified in info.plist, using default endpoint: \(defaultServer)")
            return defaultServer
        }
        return endpoint
    }
    public static let oval = RestService(endpoint)
}

internal func main(_ closure: @escaping () -> ()) {
    DispatchQueue.main.async(execute: closure)
}
