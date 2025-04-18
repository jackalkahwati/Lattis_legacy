//
//  PaymentMethodPaymentMethodInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/06/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

protocol PaymentMethodInteractorInput {
    var accessory: CreditCardCell.Accessory {get}
    func open(card: CreditCard?)
    func viewWillAppear()
    func delete(card: CreditCard)
    func select(card: CreditCard)
    func close()
}

protocol PaymentMethodInteractorOutput: BaseInteractorOutput {
    func show(cards: [CreditCard])
}
