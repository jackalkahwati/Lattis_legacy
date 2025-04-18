//
//  API.swift
//  OvalExample
//
//  Created by Ravil Khusainov on 7/10/18.
//  Copyright © 2018 Lattis inc. All rights reserved.
//

import Foundation

fileprivate var defaultEndpoint: String = {
    let defaultServer = "http://lattisapp-development.lattisapi.io/api"
    guard let endpoint = Bundle.main.infoDictionary?["OvalApiEndpoint"] as? String  else {
        print("Error: 'OvalApiEndpoint' key is not specified in info.plist, using default endpoint: \(defaultServer)")
        return defaultServer
    }
    return endpoint
}()

public struct API {
    let endpoint: String
    let path: String
    let version: Version
    public let url: URL
    
    public init(endpoint: String? = nil, version: Version = .none, path: String) {
        if let e = endpoint {
            self.endpoint = e
        } else {
            self.endpoint = defaultEndpoint
        }
        self.version = version
        self.path = path
        let str = self.endpoint + "/" + path
        self.url = URL(string: str)!
    }
    
    public init(url: URL) {
        self.url = url
        self.path = url.path
        self.endpoint = url.host!
        self.version = .none
    }
    
    public enum Version: String {
        case none
        case v1
    }
}

public extension API {
    static var refreshToken: API{
        return .init(path: "users/refresh-tokens")
    }
}
