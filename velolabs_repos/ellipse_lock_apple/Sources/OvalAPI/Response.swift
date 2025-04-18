//
//  Response.swift
//  OvalExample
//
//  Created by Ravil Khusainov on 7/10/18.
//  Copyright © 2018 Lattis inc. All rights reserved.
//

import Foundation

enum Response<A: Decodable>: Decodable {
    case none(Int)
    case result(A)
    case error(ServerError)
    
    enum CodingKeys: String, CodingKey {
        case error
        case payload
        case status
    }
    
    init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        if let error = try values.decodeIfPresent(ServerError.self, forKey: .error) {
            self = .error(error)
            return
        }
        if let result = try values.decodeIfPresent(A.self, forKey: .payload) {
            self = .result(result)
            return
        }
        let code = try values.decode(Int.self, forKey: .status)
        self = .none(code)
    }
}

public struct Token: Decodable {
    let restToken: String
    let refreshToken: String
}

struct TokenRefreshParams: Encodable {
    let userId: Int
    let refreshToken: String
}
