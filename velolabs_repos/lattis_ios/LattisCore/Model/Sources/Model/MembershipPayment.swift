//
//  MembershipPayment.swift
//  LattisCore
//
//  Created by Roger Molas on 10/20/22.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation


public struct MembershipPayment: Codable {
    public let transactionId: String
    public let lastBilling: Date?
    public let nextBilling: Date?
    public let periodStart: Date?
    public let currency: String
}

public extension MembershipPayment {
    enum CodingKeys: String, CodingKey {
        case transactionId = "transactionId"
        case lastBilling = "paidOn"
        case nextBilling = "periodEnd"
        case periodStart = "periodStart"
        case currency
    }
}
