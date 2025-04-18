//
//  Route.swift
//  RestService
//
//  Created by Ravil Khusainov on 9/23/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import SwiftyJSON

public protocol OvalRoute {
    
}

public extension OvalRoute {
    typealias Error = Oval.Error
    typealias fail = Oval.fail
    internal var restService: RestService {
        return RestService.oval
    }
    
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
    
    public func perform<A>(resource: Resource<Oval.Responce>, parse: @escaping (JSON) -> A?, retry: (() -> ())? = nil, fail: @escaping fail, mapped: @escaping (A) -> ()) {
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
    
    public func perform(resource: Resource<Oval.Responce>, check: Int, retry: (() -> ())? = nil, fail: @escaping fail, checked: @escaping (() -> ())) {
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
