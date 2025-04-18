//
//  CreditCard.swift
//  Lattis
//
//  Created by Ravil Khusainov on 6/30/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Stripe

struct CreditCard: Codable {
    let formatter = Formatter()
    var number: String? = nil {
        didSet {
            formatter.number = number
        }
    }
    var cardType: CardType? = nil
    var month: UInt? = nil
    var year: UInt? = nil
    var cvv: String? = nil
    var cardId: Int = 0
    var systemId: String? = nil
    var intent: STPSetupIntent? = nil
    
    public enum CodingKeys: String, CodingKey {
        case ccNo
        case expMonth
        case expYear
        case cvc
        case id
        case ccType
        case cardId
        case isPrimary
        case intent
    }
    
    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(number, forKey: .ccNo)
        try container.encode(month, forKey: .expMonth)
        let year = self.year! > 2000 ? self.year! : self.year! + 2000
        try container.encode(year, forKey: .expYear)
        try container.encode(cvv, forKey: .cvc)
        try container.encode(intent, forKey: .intent)
    }
}

extension CreditCard {
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        cardId = try container.decode(Int.self, forKey: .id)
        number = try container.decode(String.self, forKey: .ccNo)
        month = try container.decode(UInt.self, forKey: .expMonth)
        let year = try container.decode(UInt.self, forKey: .expYear)
        self.year = year > 2000 ? year - 2000 : year
        systemId = try container.decode(String.self, forKey: .cardId)
        cardType = try container.decode(CardType.self, forKey: .ccType)
        isCurrent = try container.decode(Bool.self, forKey: .isPrimary)
    }
    
    init(number: String, typeString: String?) {
        self.number = number
        self.formatter.number = number
        self.cardType = CardType(string: typeString)
    }
    
    enum CardType: String, Codable {
        case mastercard = "MasterCard"
        case amex = "Amex"
        case visa = "Visa"
        case maestro = "Maestro"
        case jcb = "JCB"
        case discover = "Discover"
        case unionPay = "UnionPay"
        case mir = "Mir"
        case dinersClub = "Diners Club"
        
        var icon: UIImage {
            switch self {
            case .mastercard: return #imageLiteral(resourceName: "icon_mastercard")
            case .visa: return #imageLiteral(resourceName: "icon_visa")
            case .amex: return #imageLiteral(resourceName: "icon_amex")
            case .maestro: return #imageLiteral(resourceName: "icon_maestro")
            case .jcb: return #imageLiteral(resourceName: "icon_jbc")
            case .discover: return #imageLiteral(resourceName: "icon_discover")
            case .unionPay: return #imageLiteral(resourceName: "icon_union_pay")
            case .mir: return #imageLiteral(resourceName: "icon_mir")
            case .dinersClub: return #imageLiteral(resourceName: "icon_dinersclub")
            }
        }
        
        var limit: Int {
            switch self {
            case .amex: return 15
            case .maestro: return 19
            case .dinersClub: return 14
            default: return 16
            }
        }
        
        init?(string: String?) {
            guard let str = string else { return nil }
            if str == "American Express" {
                self = .amex
            } else if let type = CardType(rawValue: str) {
                self = type
            } else {
                return nil
            }
        }
    }
    
    var isCurrent: Bool {
        get {
            return Int32(UserDefaults.standard.integer(forKey: "currentCard")) == cardId && cardId != 0
        }
        set {
            if newValue {
                UserDefaults.standard.set(cardId, forKey: "currentCard")
            } else if Int32(UserDefaults.standard.integer(forKey: "currentCard")) == cardId {
                UserDefaults.standard.set(0, forKey: "currentCard")
            }
            UserDefaults.standard.synchronize()
            
        }
    }
    
    var maskNumber: String? {
        guard let number = number else { return nil }
        return String(repeating: "•", count: 4) + " " + String(repeating: "•", count: 4) + " " + String(repeating: "•", count: 4) + " " + number.substring(fromReverse: 4)
    }
    
    var shortMaskNumber: String? {
        guard let number = number else { return nil }
        return String(repeating: "•", count: 4) + " " + number.substring(fromReverse: 4)
    }
    
    var smallMaskNumber: String? {
        guard let number = number else { return nil }
        return "*" + number.substring(fromReverse: 4)
    }
    
    var expire: String? {
        guard let month = month, let year = year else { return nil }
        return String(format: "%02d/%d", month, year)
    }
    
    var formattedNumber: String? {
        guard let type = cardType else { return nil }
        return formatter.formated(with: type)
    }
}

extension CreditCard {
    class Formatter {
        var number: String?
        func formated(with type: CardType) -> String {
            var result = number ?? ""
            switch type {
            case .amex:
                if result.count > 10 {
                    result.insert(" ", at: result.index(result.startIndex, offsetBy: 10))
                }
                if result.count > 4 {
                    result.insert(" ", at: result.index(result.startIndex, offsetBy: 4))
                }
            default:
                if result.count > 12 {
                    result.insert(" ", at: result.index(result.startIndex, offsetBy: 12))
                }
                if result.count > 8 {
                    result.insert(" ", at: result.index(result.startIndex, offsetBy: 8))
                }
                if result.count > 4 {
                    result.insert(" ", at: result.index(result.startIndex, offsetBy: 4))
                }
            }
            return result
        }
    }
}

extension STPSetupIntent: Encodable {
    enum IntentKeys: String, CodingKey {
        case stripeID
        case clientSecret
        case created
        case customerID
        case stripeDescription
        case livemode
        case metadata
        case paymentMethod
        case paymentMethodTypes
        case nextAction
        case status
        case usage
        case lastSetupError
    }

    enum ActionKeys: String, CodingKey {
        case type
        case redirectToURL
    }

    enum RedirectKeys: String, CodingKey {
        case returnURL
        case url
    }
    
    enum ErrorKeys: String, CodingKey {
        case code
    }

    public func encode(to encoder: Encoder) throws {
        var intent = encoder.container(keyedBy: IntentKeys.self)
        try intent.encode(stripeID, forKey: .stripeID)
        try intent.encode(clientSecret, forKey: .clientSecret)
        try intent.encode(created, forKey: .created)
        try intent.encode(customerID, forKey: .customerID)
        try intent.encode(stripeDescription, forKey: .stripeDescription)
        try intent.encode(livemode, forKey: .livemode)
        try intent.encode(metadata, forKey: .metadata)
        try intent.encode(paymentMethodID, forKey: .paymentMethod)
        try intent.encode(paymentMethodTypes.map{$0.intValue}, forKey: .paymentMethodTypes)
        if let next = nextAction {
            var action = intent.nestedContainer(keyedBy: ActionKeys.self, forKey: .nextAction)
            try action.encode(next.type.rawValue, forKey: .type)
            if let redirect = next.redirectToURL {
                var red = action.nestedContainer(keyedBy: RedirectKeys.self, forKey: .redirectToURL)
                try red.encode(redirect.url, forKey: .url)
                try red.encode(redirect.returnURL, forKey: .returnURL)
            }
        }
        try intent.encode(status.rawValue, forKey: .status)
        try intent.encode(usage.rawValue, forKey: .usage)
        if let error = lastSetupError {
            var err = intent.nestedContainer(keyedBy: ErrorKeys.self, forKey: .lastSetupError)
            try err.encode(error.code, forKey: .code)
        }
    }
}

