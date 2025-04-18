//
//  PaymentMethodsLogicController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 13.01.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import UIKit
import Model
import Stripe

final class PaymentMethodsLogicController {
    typealias API = PromotionAPI & PaymentNetwork
    weak var context: STPAuthenticationContext!
    let gateway: Payment.Gateway
    fileprivate(set) var promotions: [Promotion] = []
    fileprivate let api: API = AppRouter.shared.api()
    fileprivate let mpAPI: MPPaymentAPI = MercadoPago.API()
    
    init(gateway: Payment.Gateway = UITheme.theme.paymentGateway) {
        // Matk: - This will fallback to predefined settings for the payment system
        self.gateway = UITheme.theme.paymentGateway
    }
    
    func fetch(completion: @escaping () -> Void) {
        api.fetchPromotions { [weak self]  (result) in
            switch result {
            case .failure(let error):
                Analytics.report(error)
            case .success(let promotions):
                self?.promotions = promotions
                completion()
            }
        }
    }
    
    func addPromo(code: String, completion: @escaping (Result<[IndexPath], Error>) -> ()) {
        api.redeem(promoCode: code) { [weak self] (result) in
            switch result {
            case .failure(let error):
                completion(.failure(error))
            case .success(let promotion):
                if let paths = self?.handle(promotion: promotion) {
                    completion(.success(paths))
                }
            }
        }
    }
    
    func createIntent(with params: STPPaymentMethodCardParams, completion: @escaping (Result<Void, Error>, String) -> ()) {
        api.createIntent(request: .init(action: .setup_intent, paymentGateway: gateway)) { [weak self] result in
            switch result {
            case .success(let key):
                self?.handle(secret: key, params: params, completion: completion)
            case .failure(let error):
                completion(.failure(error), params.title)
            }
        }
    }
    
    func addCard(with controller: PaymentMethodsViewController, card: Payment.Card? = nil) {
        Analytics.log(.addCard())
        let add = AddPaymentViewController(self, replacing: card)
        controller.present(add, animated: true)
    }
    
    fileprivate func stripe(_ params: STPPaymentMethodCardParams, _ completion: @escaping (Result<Void, Error>, String) -> (), _ secret: String) {
        let paymentMethodParams = STPPaymentMethodParams(card: params, billingDetails: nil, metadata: nil)
        STPAPIClient.shared.createPaymentMethod(with: paymentMethodParams) { [weak self] paymentMethod, error in
            if let e = error {
                completion(.failure(e), params.title)
                return
            }
            guard let `self` = self, let context = self.context else { return }
            let setupIntentParams = STPSetupIntentConfirmParams(clientSecret: secret)
            setupIntentParams.paymentMethodID = paymentMethod?.stripeId
            let paymentManager = STPPaymentHandler.shared()
            paymentManager.confirmSetupIntent(setupIntentParams, with: context, completion: { [weak self] (status, int, error) in
                if let e = error {
                    Analytics.report(e)
                    completion(.failure(e), params.title)
                    return
                }
                guard let intent = int, let card = Payment.Card.New(params: params, intent: intent) else {
                    completion(.failure(PaymentAddingError.emptyIntent), params.title)
                    return
                }
                switch status {
                case .succeeded:
                    self?.add(card: card, completion: completion)
                default:
                    completion(.failure(PaymentAddingError.stripeFailed), params.title)
                    break
                }
            })
        }
    }
    
    fileprivate func meracadopago(_ params: STPPaymentMethodCardParams, _ completion: @escaping (Result<Void, Error>, String) -> (), _ secret: String) {
        mpAPI.tokenize(.init(params), with: secret) { [unowned self] result in
            switch result {
            case .failure(let error):
                completion(.failure(error), params.title)
            case .success(let token):
                self.api.add(mpCard: .init(token: token, fleetId: MercadoPago.fleetId)) { res in
                    switch res {
                    case .failure(let error):
                        completion(.failure(error), params.title)
                    case .success:
                        completion(.success(()), params.title)
                    }
                }
            }
        }
    }
    
    fileprivate func handle(secret: String, params: STPPaymentMethodCardParams, completion: @escaping (Result<Void, Error>, String) -> ()) {
        switch gateway {
        case .stripe:
            stripe(params, completion, secret)
        case .mercadopago:
            meracadopago(params, completion, secret)
        }
    }
    
    fileprivate func add(card: Payment.Card.New, completion: @escaping (Result<Void, Error>, String) -> ()) {
        api.add(card: card) { (result) in
            switch result {
            case .success:
                completion(.success(()), card.title)
            case .failure(let error):
                completion(.failure(error), card.title)
            }
        }
    }
    
    fileprivate func handle(promotion: Promotion) -> [IndexPath] {
        var result: [IndexPath] = []
        if !promotions.contains(promotion) {
            result.append(.init(row: promotions.count, section: 1))
            promotions.append(promotion)
        }
        return result
    }
}

fileprivate enum PaymentAddingError: String, Error {
    case emptyIntent
    case stripeFailed
}

extension STPPaymentMethodCardParams {
    var title: String {
        guard let number = number, let month = expMonth?.intValue, let year = expYear?.intValue else { return "No title" }
        return "XXXX" + String(number.suffix(4)) + String(format: " %02d/%02d", month, year)
    }
}
