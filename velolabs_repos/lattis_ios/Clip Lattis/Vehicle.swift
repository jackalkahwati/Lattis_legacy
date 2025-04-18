//
//  Vehicle.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 03.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation

struct Vehicle: Codable {
    let id: Int
    let name: String
    let qrCode: Int?
    let fleet: Fleet
    let things: [Thing]?
}

struct Fleet: Codable {
    let id: Int
    let name: String
    let paymentSettings: PaymentSettings?
}

struct Thing: Codable {
    let id: Int
    let key: String
}


extension Fleet {
    struct PaymentSettings: Codable {
        let id: Int
    }
}


extension Vehicle {
    struct Status: Codable {
        let locked: Bool
    }
}


