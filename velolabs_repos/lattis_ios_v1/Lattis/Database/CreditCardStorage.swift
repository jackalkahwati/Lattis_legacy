//
//  CreditCardStorage.swift
//  Lattis
//
//  Created by Ravil Khusainov on 7/4/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

protocol CreditCardStorage {
    func save(card: CreditCard)
    var cards: [CreditCard] {get}
    func delete(card: CreditCard)
    var currentCard: CreditCard? {get}
    func setCurrent(card: CreditCard)
    func update(cards: [CreditCard])
}
