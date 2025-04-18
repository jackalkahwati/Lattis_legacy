//
//  Oval+Cards.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 01/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import OvalAPI
import Stripe
import Model

extension API {
    enum CardPath: String {
        case add = "add-cards"
        case all = "get-cards"
        case primary = "set-card-primary"
        case delete = "delete-card"
        case update = "update-card"
    }
    
    static func cards(_ path: CardPath, query: String = "") -> API {
        return .init(path: "users/" + path.rawValue + query)
    }
}

extension Session: PaymentNetwork {
    func createIntent(request: Payment.Intent.Request, completion: @escaping (Result<String, Error>) -> ()) {
        var query = "?action=setup_intent"
        if let gateway = request.paymentGateway {
            query += "&payment_gateway=\(gateway.rawValue)"
        }
        switch request.paymentGateway {
        case .mercadopago:
            struct MPKey: Decodable {
                let publicKey: String
            }
            send(.post(json: Empty(), api: .cards(.add, query: query))) { (result: Result<MPKey, Error>) in
                switch result {
                case .failure(let error):
                    completion(.failure(error))
                case .success(let key):
                    completion(.success(key.publicKey))
                }
            }
        default:
            send(.post(json: Empty(), api: .cards(.add, query: query))) { (result: Result<Payment.Intent, Error>) in
                switch result {
                case .success(let intent):
                    completion(.success(intent.clientSecret))
                case .failure(let error):
                    completion(.failure(error))
                }
            }
        }
    }
    
    func add(card: Payment.Card.New, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: card, api: .cards(.add)), completion: completion)
    }
    
    func add(mpCard: MercadoPago.TokenizedCard, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: mpCard, api: .cards(.add)), completion: completion)
    }
    
    func getCards(completion: @escaping (Result<[Payment.Card], Error>) -> ()) {
        send(.post(json: Empty(), api: .cards(.all)), completion: completion)
    }
    
    func delete(card: Payment.Card, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: card, api: .cards(.delete)), completion: completion)
    }
    
    func update(card: Payment.Card.Update, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: card, api: .cards(.update)), completion: completion)
    }
    
    func setPrimary(card: Payment.Card, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: card, api: .cards(.primary)), completion: completion)
    }
}

extension Payment.Card {
    struct New: Encodable {
        let number: String
        let month: Int
        let year: Int
        let code: String
        let system: Payment.System
        let intent: STPSetupIntent
        
        enum CodingKeys: String, CodingKey {
            case number = "ccNo"
            case month = "expMonth"
            case year = "expYear"
            case system = "ccType"
            case code = "cvc"
            case intent
        }
        
        init?(params: STPPaymentMethodCardParams, intent: STPSetupIntent) {
            guard let number = params.number,
                let month = params.expMonth,
                let year = params.expYear,
                let code = params.cvc,
                let system = Payment.System(STPCardValidator.brand(forNumber: number)) else {
                return nil
            }
            self.number = number
            self.month = month.intValue
            self.year = year.intValue
            self.code = code
            self.intent = intent
            self.system = system
        }
        
        var title: String {
            return "XXXX" + String(number.suffix(4)) + String(format: " %02d/%02d", month, year)
        }
    }
    
    struct Update: Encodable {
        let cardId: String
        let month: Int
        let year: Int
        
        enum CodingKeys: String, CodingKey {
            case cardId = "card_id"
            case month = "exp_month"
            case year = "exp_year"
        }
        
        init?(params: Payment.Card) {
            self.cardId = params.cardId
            self.month = params.month
            self.year = params.year
        }
        
        init?(cardId: String, month: Int, year: Int) {
            self.cardId = cardId
            self.month = month
            self.year = year
        }
    }
}

extension Payment.System {
    init?(_ brand: STPCardBrand) {
        guard let str = STPCardBrandUtilities.stringFrom(brand),
              let system = Payment.System(rawValue: str) else {
            self = .mir
            return
        }
        self = system
    }
    
    var brand: STPCardBrand {
        switch self {
        case .visa:
            return .visa
        case .mastercard, .maestro, .mastercardMP:
            return .mastercard
        case .discover:
            return .discover
        case .dinersClub:
            return .dinersClub
        case .amex:
            return .amex
        case .jcb:
            return .JCB
        case .unionPay:
            return .unionPay
        default:
            return .unknown
        }
    }
    
    var icon: UIImage? {
        return STPPaymentCardTextField.brandImage(for: brand)
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
//        case metadata
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
//        try intent.encode(metadata, forKey: .metadata)
        try intent.encode(paymentMethodID, forKey: .paymentMethod)
        try intent.encode(paymentMethodTypes.map({$0.intValue}), forKey: .paymentMethodTypes)
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
