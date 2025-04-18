//
//  TripManager+Stripe.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 14.05.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//


import Foundation
import Stripe

extension TripManager {
    func authenticateStripe(with secret: String) {
        guard let context = stripeContext else { return }
        STPAPIClient.shared.stripeAccount = "acct_1AXxHAHaFQpoUkOU"
        let params = STPPaymentIntentParams(clientSecret: secret)
        params.paymentMethodId = "pm_1IsO6nHaFQpoUkOU9EqMzmRn"
        STPPaymentHandler.shared().confirmPayment(params, with: context) { [unowned self] status, intent, error in
//        STPPaymentHandler.shared().handleNextAction(forPayment: secret, with: context, returnURL: nil) { [unowned self] status, intent, error in
            if let error = error {
                print(error)
                self.handle(error: error)
            }
            if status == .succeeded, let id = intent?.stripeId {
                _ = self.endTrip(parking: false, force: true, chargeId: id)
            }
        }
    }
}
