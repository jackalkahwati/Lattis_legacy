//
//  CreditCardCreditCardInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/06/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

protocol CreditCardInteractorInput {
    func viewLoaded()
    func validate(text: String, type: CreditCard.Validation) -> String?
    func save()
    func scan()
    func delete()
    var firstResponder: CreditCard.Validation? {get}
}

protocol CreditCardInteractorOutput: BaseInteractorOutput {
    func show(card: CreditCard, edit: Bool)
    func show(type: CreditCard.CardType?)
    func set(canSave: Bool)
    func switchFocusTo(field: CreditCard.Validation)
}

extension CreditCard {
    enum Validation {
        case card, date, cvv
    }
}
