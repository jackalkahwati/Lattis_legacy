//
//  CDCard+CoreDataClass.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 01/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//
//

import Foundation
import CoreData
import Model

@objc(CDCard)
public class CDCard: NSManagedObject {

}

extension CDCard: CoreDataObject {
    static var entityName: String {
        return "CDCard"
    }
}

extension Payment.Card {
    init(_ cd: CDCard) {
        self.init(id: Int(cd.cardId), number: cd.number ?? "", month: Int(cd.month), year: Int(cd.year), system: cd.system!, isPrimary: false, cardId: cd.cardStringId ?? "", gateway: Payment.Gateway(rawValue: cd.gateway ?? "stripe") ?? .stripe)
    }
}

extension CDCard {
    func fill(_ card: Payment.Card) {
        self.cardId = Int32(card.id)
        self.month = Int32(card.month)
        self.year = Int32(card.year)
        self.number = card.number
        self.system = card.system
        var p = Payment.card(card)
        p.isCurrent = card.isPrimary
        self.cardStringId = card.cardId
        self.gateway = card.gateway?.rawValue
    }
}
