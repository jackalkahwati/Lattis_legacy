//
//  Thing.swift
//  BLUF
//
//  Created by Ravil Khusainov on 22.08.2020.
//

import Foundation

struct Thing: Identifiable, Codable {
    let id: Int
    let key: String
    let vendor: String
    let qrCode: String?
}
