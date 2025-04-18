//
//  Legacy.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 08.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation

enum Legacy {
    struct Vehicle: Codable {
        let bikeName: String
        let fleetName: String
    }
    
    struct Trip: Codable {
        let bike_id: Int
        let trip_id: Int
    }
}
