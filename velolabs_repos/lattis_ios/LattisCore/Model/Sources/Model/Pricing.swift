//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 03.08.2021.
//

import Foundation


public struct Pricing: Codable {
    public let fleetId: Int
    public let durationUnit: String
    public let gracePeriod: String?
    public let pricingOptionId: Int
    public let price: Double
//    public let createdAt: Date
    public let duration: Int
    public let priceCurrency: String
    public let deactivationReason: String?
    public let gracePeriodUnit: String?
//    public let deactivatedAt: String?
}
