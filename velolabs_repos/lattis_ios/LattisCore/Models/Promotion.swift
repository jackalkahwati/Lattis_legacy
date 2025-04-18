//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 28.01.2021.
//

import Foundation

public struct Promotion: Codable {
    public let promotionId: Int
    public let fleetId: Int
    public let promotionCode: String
    public let amount: Double
    let fleet: Fleet?

    public enum CodingKeys: String, CodingKey {

        case promotionId
        case fleetId
        case promotionCode
        case amount
        case fleet
    }
}

extension Promotion: Equatable {
    public static func == (lhs: Promotion, rhs: Promotion) -> Bool {
        lhs.promotionId == rhs.promotionId
    }
}
