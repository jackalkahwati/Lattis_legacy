//
//  Address.swift
//  
//
//  Created by Ravil Khusainov on 02.03.2021.
//

import Vapor
import Fluent


final class Address: Model, Content {
    static var schema: String = "addresses"
    
    @ID(custom: "address_id")
    var id: Int?
    
    @OptionalField(key: "city")
    var city: String?
    
    @OptionalField(key: "address1")
    var address1: String?
    
    @OptionalField(key: "address2")
    var address2: String?
    
    @OptionalField(key: "state")
    var state: String?
    
    @OptionalField(key: "country")
    var country: String?
    
    @OptionalField(key: "postal_code")
    var postalCode: String?
}
