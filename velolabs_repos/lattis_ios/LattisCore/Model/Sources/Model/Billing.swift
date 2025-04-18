//
//  Billing.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 14.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation

public struct Billing: Codable {
    public let id: Int
    public let overUsageFees: Double
    public let deposit: Double
    public let chargeForDuration: Double
    public let penaltyFees: Double
    public let total: Double
    public let bikeUnlockFee: Double
    public let membershipDiscount: Double
    public let currency: String
    public let tripId: Int
    public let cardId: String
}
