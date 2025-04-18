//
//  User.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 08.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation

struct User: Codable {
    let token: String
}

struct Trip: Codable {
    
}


extension Trip {
    struct Start: Codable {
        let bike_id: Int
        let latitude: Double
        let longitude: Double
    }
    
    struct End: Codable {
        let trip_id: Int
        let latitude: Double
        let longitude: Double
        let accuracy: Double
    }
    
    struct Status: Codable {
        let trip_id: Int
    }
}
