//
//  MercadoPago.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 31.05.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import Foundation
import Model
import Stripe

enum MercadoPago {
    final class API {
        fileprivate let endpoint = "https://api.mercadopago.com/"
        fileprivate let session = URLSession.shared
    }
    
    static var fleetId: Int? {
        switch UITheme.theme.userAgent {
        case "grin":
            return 260
        case "grin-santiago":
            return 263
        default:
            return nil
        }
    }
}

extension MercadoPago.API: MPPaymentAPI {
    func tokenize(_ card: MercadoPago.CustomerCard, with publicKey: String, completion: @escaping (Result<String, Error>) -> ()) {
        let url = URL(string: endpoint + "v1/card_tokens?public_key=" + publicKey)!
        var request = URLRequest(url: url)
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        request.httpMethod = "POST"
        
        struct Resp: Decodable {
            let id: String
        }
        
        do {
            request.httpBody = try JSONEncoder().encode(card)
            session.dataTask(with: request) { d, r, e in
                if let error = e {
                    DispatchQueue.main.async {
                        completion(.failure(error))
                    }
                    return
                }
                if let data = d, let result = try? JSONDecoder().decode(Resp.self, from: data) {
                    DispatchQueue.main.async {
                        completion(.success(result.id))
                    }
                    return
                }
                if let data = d, let str = String(data: data, encoding: .utf8) {
                    DispatchQueue.main.async {
                        completion(.failure(MercadoPago.Failure.raw(str)))
                    }
                }
            }.resume()
        } catch {
            completion(.failure(error))
        }
    }
}

extension MercadoPago {
    struct CustomerCard: Codable {
        let card_number: String
        let security_code: String
        let expiration_month: Int
        let expiration_year: Int
    }
    
    struct TokenizedCard: Encodable {
        let token: String
        let paymentGateway: String = "mercadopago"
        let fleetId: Int?
    }
    
    enum Failure: Error {
        case raw(String)
    }
}

extension MercadoPago.CustomerCard {
    init(_ params: STPPaymentMethodCardParams) {
        self.card_number = params.number!
        self.security_code = params.cvc!
        self.expiration_month = params.expMonth!.intValue
        self.expiration_year = params.expYear!.intValue + 2000
    }
}
