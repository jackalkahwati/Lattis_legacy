//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 15.01.2021.
//

import Foundation
import Model

public struct Rentals: Decodable {
    public let bikes: [Bike]
    public let hubs: [Hub]
}

