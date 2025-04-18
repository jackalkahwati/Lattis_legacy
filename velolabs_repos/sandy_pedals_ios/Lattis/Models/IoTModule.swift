//
//  IoTModule.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 24.03.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import Foundation
import AXALock

public struct IoTModule: Codable {
    let controllerId: Int?
    let bikeId: Int?
    let fleetId: Int
    let key: String
    let vendor: Vendor
    let deviceType: String
    let fwVersion: String?
    let hwVersion: String?
    let make: String?
    let model: String?
    let status: String
    let batteryLevel: Int?
    
    struct Query {
        let vendor: Vendor?
        let key: String?
        
        init(vendor: Vendor? = nil, key: String? = nil) {
            self.vendor = vendor
            self.key = key
        }
        
        var stringValue: String {
            if let key = key {
                return "?key=\(key)"
            }
            if let vendor = vendor {
                return "?vendor=\(vendor.rawValue)"
            }
            return ""
        }
        
        static func vendor(_ vendor: Vendor) -> Query {
            .init(vendor: vendor, key: nil)
        }
        
        static func key(_ key: String) -> Query {
            .init(vendor: nil, key: key)
        }
    }
    
    enum Vendor: String, Codable {
        case AXA
    }
}
