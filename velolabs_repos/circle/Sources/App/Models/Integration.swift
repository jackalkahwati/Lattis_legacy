//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 04.06.2021.
//

import Vapor
import Fluent

final class Integration<Meta: Codable>: Model, Content {
    static var schema: String { "integrations" }
    
    @ID(custom: "integrations_id")
    var id: Int?
    
    @Field(key: "fleet_id")
    var fleetId: Int
    
    @Enum(key: "integration_type")
    var type: Kind
    
    @OptionalField(key: "metadata")
    var metadeta: Meta?
    
    @OptionalField(key: "session_id")
    var sessionId: String?
    
    @OptionalField(key: "email")
    var email: String?
    
    @OptionalField(key: "api_key")
    var apiKey: String?
}

extension Integration {
    enum Kind: String, Codable {
        case kisi
        case tapkey
        case segway
        case duckt
        case acton = "ACTON"
        case linka
        case geotab
    }
}

