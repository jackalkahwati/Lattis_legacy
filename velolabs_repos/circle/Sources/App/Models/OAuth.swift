//
//  OAuth.swift
//  
//
//  Created by Ravil Khusainov on 19.01.2022.
//

import Vapor
import Fluent

final class OAuth: Model, Content {
    static var schema: String = "oauth"
    
    @ID(custom: .id)
    var id: Int?
    
    @Enum(key: "provider")
    var provider: Provider
    
    @Enum(key: "service")
    var service: Service
    
    @Field(key: "user_id")
    var userId: Int
    
    @Field(key: "provider_id")
    var providerId: String
}

extension OAuth {
    enum Provider: String, Codable {
        case google
        case apple
        case facebook
    }
    
    enum Service: String, Codable {
        case lattis
        case `operator`
        case ellipse
    }
    
    struct User: Codable {
        let firstName: String?
        let lastName: String?
        let email: String?
        let user: String
        let identityToken: String
        let authorizationCode: String
        let countryCode: String
    }
}

extension UserModel {
    convenience init(_ oauth: OAuth.User) {
        self.init()
        self.email = oauth.email
        self.scope = .lattis
        self.firstName = oauth.firstName
        self.lastName = oauth.lastName
        self.password = UUID().uuidString
        self.verified = 1
        self.acceptedTerms = 1
        self.countryCode = oauth.countryCode.lowercased()
        self.identifier = oauth.email
    }
}

extension OAuth {
    convenience init(provider: Provider, service: Service, userId: Int, providerId: String) {
        self.init()
        self.provider = provider
        self.service = service
        self.userId = userId
        self.providerId = providerId
    }
}
