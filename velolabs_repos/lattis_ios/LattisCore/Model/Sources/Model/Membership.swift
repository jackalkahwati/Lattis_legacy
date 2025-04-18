//
//  Membership.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation


public struct Membership: Codable {
    public let id: Int
    public let fleet: Fleet
    public let frequency: Frequency
    public let incentive: Double
    public let currency: String
    public let price: Double
    public let payments: [MembershipPayment]?
    
    public var activeSubs: MembershipPayment? {
        guard let payments = payments else {
            return nil
        }
        let laters = payments.sorted(
            by: { $0.periodStart! > $1.periodStart!})
        return laters.first
    }
}

public extension Membership {
    enum CodingKeys: String, CodingKey {
        case id = "fleetMembershipId"
        case fleet
        case frequency = "paymentFrequency"
        case incentive = "membershipIncentive"
        case currency = "membershipPriceCurrency"
        case price = "membershipPrice"
        case payments = "membershipSubscriptionPayments"
    }
    
    enum Frequency: String, Codable {
        case weekly
        case monthly
        case yearly
    }
}

public extension Double {
    func string(fraction: Int = 0) -> String {
        let formatter = NumberFormatter()
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = fraction
        guard let form = formatter.string(from: .init(value: self)) else { return "\(self)" }
        return form
    }
}
