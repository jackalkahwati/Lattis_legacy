//
//  Oval+Promotion.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 28.01.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import Foundation
import OvalAPI
import Model

extension API {
    static let promotions = API(path: "promotions")
    static func redeem(code: String) -> API {
        .init(path: "promotions/\(code)/redeem")
    }
}

extension Session: PromotionAPI {
    func fetchPromotions(completion: @escaping (Result<[Promotion], Error>) -> Void) {
        send(.get(.promotions), completion: completion)
    }
    
    func redeem(promoCode: String, completion: @escaping (Result<Promotion, Error>) -> Void) {
        send(.patch(Empty(), api: .redeem(code: promoCode)), completion: completion)
    }
    
    
}
