//
//  Bike.swift
//  BLUF
//
//  Created by Ravil Khusainov on 06.11.2020.
//

import Foundation

struct Bike: Identifiable, Codable {
    let id: Int
    let name: String?
    let qrCode: Int?
    let lockId: Int?
    let things: [Thing]
    let fleet: Fleet?
}
