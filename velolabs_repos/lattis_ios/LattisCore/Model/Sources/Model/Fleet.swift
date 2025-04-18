//
//  Fleet.swift
//  
//
//  Created by Ravil Khusainov on 28.08.2020.
//

import Foundation

public struct Fleet: Codable {
    public let fleetId: Int
    public let name: String?
    public let email: String?
    public let customer: String?
    public let logo: URL?
    public let legal: String?
    public let type: ChargeType
    public let requirePhoneNumber: Bool?
    public let reservationSettings: Reservation.Settings?
    public let paymentSettings: Payment.Settings?
    public let address: Address?
    public let pricingOptions: [Pricing]?
    
    public init(fleetId: Int, name: String?, email: String?, customer: String?, logo: URL?, legal: String?, requirePhoneNumber: Bool?, type: ChargeType, reservationSettings: Reservation.Settings?, paymentSettings: Payment.Settings? = nil, address: Fleet.Address = .planetEarth) {
        self.fleetId = fleetId
        self.name = name
        self.email = email
        self.customer = customer
        self.logo = logo
        self.legal = legal
        self.type = type
        self.requirePhoneNumber = requirePhoneNumber
        self.reservationSettings = reservationSettings
        self.paymentSettings = paymentSettings
        self.address = address
        self.pricingOptions = nil
    }
}

public extension Fleet {
    enum CodingKeys: String, CodingKey {
        case name = "fleetName"
        case email
        case customer = "customerName"
        case logo
        case fleetId
        case reservationSettings
        case paymentSettings = "fleetPaymentSettings"
        case address
        case legal = "tAndC"
        case type
        case pricingOptions
        case requirePhoneNumber
    }
    
    enum ChargeType: String, Codable {
        case privateFree = "private_no_payment"
        case publicPay = "public"
        case privatePay = "private"
        case publicFree = "public_no_payment"
    }
    
    struct Address: Codable {
        public let country: String
        public let city: String
        public let state: String?
        public let postalCode: String?
        
        public static let planetEarth = Address(country: "Planet Earth", city: "Everywhere", state: "None", postalCode: nil)
    }
    
    var isReservationEnabled: Bool {
        guard let settings = reservationSettings else { return false }
        return settings.deactivatedAt == nil
    }
}

