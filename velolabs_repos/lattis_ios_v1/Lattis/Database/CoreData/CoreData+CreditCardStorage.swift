//
//  CoreData+CreditCardStorage.swift
//  Lattis
//
//  Created by Ravil Khusainov on 7/4/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

let currentCardChanged = Notification.Name(rawValue: "currentCardChanged")

extension CoreDataStack: CreditCardStorage {
    func save(card: CreditCard) {
        write(completion: { (context) in
            do {
                var cdCard = try CDCreditCard.find(in: context, with: NSPredicate(format: "cardId = %@", NSNumber(value: card.cardId)))
                if cdCard == nil {
                    cdCard = CDCreditCard.create(in: context)
                }
                cdCard?.fill(card: card)
            } catch {
                print(error)
            }
        }, fail: {print($0)})
    }
    
    var cards: [CreditCard] {
        do {
            let cards = try CDCreditCard.all(in: mainContext, sortetBy: [NSSortDescriptor(key: "cardId", ascending: true)])
            return cards.map(CreditCard.init)
        } catch {
            print(error)
            return []
        }
    }
    
    var currentCard: CreditCard? {
        return cards.first(where: {$0.isCurrent})
    }
    
    func delete(card: CreditCard) {
        write(completion: { (context) in
            do {
                let cdCard = try CDCreditCard.find(in: context, with: NSPredicate(format: "cardId = %@", NSNumber(value: card.cardId)))
                if let cc = cdCard {
                    context.delete(cc)
                }
            } catch {
                print(error)
            }
        }, fail: {print($0)})
    }
    
    func setCurrent(card: CreditCard) {
        var mutable = card
        mutable.isCurrent = true
        NotificationCenter.default.post(name: currentCardChanged, object: card, userInfo: nil)
    }
    
    func update(cards: [CreditCard]) {
        write(completion: { (context) in
            let ids = cards.map({ $0.cardId })
            do {
                let delete = try CDCreditCard.all(in: context, with: NSPredicate(format: "NOT(cardId in %@)", ids))
                delete.forEach({ context.delete($0) })
                for card in cards {
                    var cdCard = try CDCreditCard.find(in: context, with: NSPredicate(format: "cardId = %@", NSNumber(value: card.cardId)))
                    if cdCard == nil {
                        cdCard = CDCreditCard.create(in: context)
                    }
                    cdCard?.fill(card: card)
                }
            } catch {
                print(error)
            }
        }, fail: {print($0)})
    }
}

extension CreditCard {
    init(card: CDCreditCard) {
        self.month = UInt(card.month)
        self.year = UInt(card.year)
        self.number = card.number
        self.cardId = Int(card.cardId)
        if let type = card.cardType {
            self.cardType = CreditCard.CardType(rawValue: type)
        }
        self.systemId = card.systemId
    }
}

extension CDCreditCard {
    func fill(card: CreditCard) {
        self.cardType = card.cardType?.rawValue
        self.month = Int32(card.month ?? 0)
        self.year = Int32(card.year ?? 0)
        self.number = card.number?.substring(fromReverse: 4)
        self.cardId = Int32(card.cardId)
        self.systemId = card.systemId
    }
}
