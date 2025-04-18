//
//  BikeInfo.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 05/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import Model

enum BikeInfo {
    case bike(Bike)
    case bike_v2(Model.Bike)
    case tuple(String, String)
    case info(String)
    case fleet(String?)
    case details(String)
    case parkingSpots
    case termsOfUse
    
    var identifier: String {
        switch self {
        case .bike:
            return "bike"
        case .bike_v2:
            return "bike"
        case .tuple:
            return "tuple"
        case .info:
            return "info"
        case .details:
            return "details"
        default:
            return "disclosure"
        }
    }
}

