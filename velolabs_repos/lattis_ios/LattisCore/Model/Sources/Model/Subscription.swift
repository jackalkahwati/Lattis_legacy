//
//  Subscription.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation


public struct Subscription: Codable {
    public let id: Int
    public let activatedAt: Date
    public let deactivatedAt: Date?
    public let periodStart: Date
    public let periodEnd: Date
    public let membership: Membership
}

public extension Subscription {
    enum CodingKeys: String, CodingKey {
        case id = "membershipSubscriptionId"
        case activatedAt = "activationDate"
        case deactivatedAt = "deactivationDate"
        case periodStart
        case periodEnd
        case membership = "fleetMembership"
    }
}
