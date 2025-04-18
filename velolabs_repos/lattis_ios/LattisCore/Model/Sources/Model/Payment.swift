//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 31.08.2020.
//

import Foundation

public enum Payment {
    case applePay
    case card(Card)
    case addNew
}

public extension Payment {
    struct Settings: Codable {
        public init(priceForPenaltyOutsideParking: Double, currency: String) {
            self.excessUsageTypeValue = 0
            self.id = 0
            self.paymentMode = ""
            self.currency = currency
            self.priceForPenaltyOutsideParking = priceForPenaltyOutsideParking
            self.priceForBikeUnlock = 0
            self.priceTypeValue = 0
            self.excessUsageTypeAfterType = ""
            self.excessUsageTypeAfterValue = 0
            self.priceType = ""
            self.priceForMembership = 0
            self.excessUsageType = ""
            self.priceForPenaltyOutsideZone = 0
            self.priceForRideDeposit = 0
            self.excessUsageFees = 0
            self.paymetnGateway = .stripe
            self.preauthAmount = nil
            self.enablePreauth = nil
        }
        
        public let excessUsageTypeValue: Int
        public let id: Int
        public let paymentMode: String
        public let currency: String
        public let priceForPenaltyOutsideParking: Double?
        public let priceForBikeUnlock: Double?
        public let priceTypeValue: Int
        public let excessUsageTypeAfterType: String?
        public let excessUsageTypeAfterValue: Int?
        public let priceType: String
        public let priceForMembership: Double
        public let excessUsageType: String
        public let priceForPenaltyOutsideZone: Double?
        public let priceForRideDeposit: Double
        public let excessUsageFees: Double?
        public let paymetnGateway: Gateway?
        public let preauthAmount: Double?
        public let enablePreauth: Bool?
    }
    
    struct Card: Codable {
        public init(id: Int, number: String, month: Int, year: Int, system: String, isPrimary: Bool, cardId: String, gateway: Gateway?) {
            self.id = id
            self.number = number
            self.month = month
            self.year = year
            self.system = system
            self.isPrimary = isPrimary
            self.cardId = cardId
            self.gateway = gateway
        }
        
        public let id: Int
        public let number: String
        public let month: Int
        public let year: Int
        public let system: String
        public let isPrimary: Bool
        public let cardId: String
        public let gateway: Gateway?
        
        enum CodingKeys: String, CodingKey {
            case id
            case number = "ccNo"
            case month = "expMonth"
            case year = "expYear"
            case system = "ccType"
            case isPrimary
            case cardId
            case gateway = "paymentGateway"
        }
    }
    
    enum System: String, Codable {
        case mastercard = "MasterCard"
        case mastercardMP = "Mastercard"
        case amex = "American Express"
        case visa = "Visa"
        case maestro = "Maestro"
        case jcb = "JCB"
        case discover = "Discover"
        case unionPay = "UnionPay"
        case mir = "Mir"
        case dinersClub = "Diners Club"
    }
    
    struct Intent: Codable {
        public let clientSecret: String
        
        public struct Request: Codable {
            public init(action: Payment.Intent.Action, paymentGateway: Payment.Gateway? = nil) {
                self.action = action
                self.paymentGateway = paymentGateway
            }
            
            public let action: Action
            public let paymentGateway: Gateway?
        }
        
        public enum Action: String, Codable {
            case setup_intent
        }
    }
    
    enum Gateway: String, Codable {
        case stripe, mercadopago
    }
}

