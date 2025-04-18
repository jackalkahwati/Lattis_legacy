//
//  Fleet.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 05/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

public struct Fleet: Codable {
    public let fleetId: Int
    public var name: String?
    public var isCurrent: Bool? = nil
    
    public enum CodingKeys: String, CodingKey {
        case fleetId
        case name = "fleetName"
    }
}
