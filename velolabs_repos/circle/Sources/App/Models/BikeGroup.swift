//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 18.10.2020.
//

import Vapor
import Fluent

extension Bike {
    final class Group: Model, Content {
        static var schema: String = "bike_group"
        
        @ID(custom: "bike_group_id")
        var id: Int?
        
        @Field(key: "type")
        var type: String
        
        @OptionalField(key: "make")
        var make: String?
        
        @OptionalField(key: "model")
        var model: String?
        
        @OptionalField(key: "description")
        var description: String?
        
        @OptionalField(key: "pic")
        var image: String?
        
        @Field(key: "fleet_id")
        var fleetId: Int
    }
}
