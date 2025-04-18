//
//  CardStorage.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 01/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import Model

class CardStorage {
    fileprivate let coreData = CoreDataStack.shared
    fileprivate var callback: (([Payment.Card]) -> ())?
    fileprivate var cached: [Payment.Card]?
    fileprivate let network: PaymentNetwork = AppRouter.shared.api()
    fileprivate var subscryber: CoreDataStack.Subscriber<CDCard>?
    
    init() {
        subscryber = coreData.subscribe(completion: { [weak self] (cards) in
            self?.handle(cards: cards)
        })
    }
    
    func fetch(completion: @escaping ([Payment.Card]) -> ()) {
        self.callback = completion
        if let c = cached {
            completion(c)
        }
    }
    
//    func save(card: Payment.Card.New, completion: @escaping (Error?) -> ()) {
//        network.add(card: card) { [weak self] (result) in
//            switch result {
//            case .success:
//                self?.refresh(completion: {_ in})
//                completion(nil)
//            case .failure(let error):
//                completion(error)
//            }
//        }
//    }
    
    func selectCurrent(card: Payment.Card, completion: @escaping (Error?) -> ()) {
        network.setPrimary(card: card) { (result) in
            switch result {
            case .success:
                completion(nil)
            case .failure(let e):
                completion(e)
            }
        }
    }
    
    func delete(card: Payment.Card, completion: @escaping (Error?) -> ()) {
        network.delete(card: card) { (result) in
            switch result {
            case .success:
                completion(nil)
            case .failure(let e):
                completion(e)
            }
        }
    }
    
    
    func update(card: Payment.Card.Update, completion: @escaping (Error?) -> ()) {
        network.update(card: card) { (result) in
            switch result {
            case .success:
                completion(nil)
            case .failure(let e):
                completion(e)
            }
        }
    }
    
    func refresh(completion: @escaping (Error?) -> ()) {
        network.getCards { [weak self] (result) in
            switch result {
            case .success(let cards):
                for card in cards where card.isPrimary || cards.count == 1 {
                    var payment = Payment.card(card)
                    payment.isCurrent = true
                }
                self?.coreData.save(cards: cards)
                completion(nil)
            case .failure(let error):
                completion(error)
            }
        }
    }
    
    fileprivate func handle(cards: [CDCard]) {
        let mapped = cards.map(Payment.Card.init)
        self.cached = mapped
        self.callback?(mapped)
    }
}

extension CoreDataStack {
    func save(cards: [Payment.Card]) {
        write(completion: { (context) in
            do {
                let current = try CDCard.all(in: context)
                for cd in current {
                    if let update = cards.first(where: {$0.id == Int(cd.cardId)}) {
                        cd.fill(update)
                    } else {
                        context.delete(cd)
                    }
                }
                let idS = current.map({Int($0.cardId)})
                let new = cards.filter({!idS.contains($0.id)})
                new.forEach({ (card) in
                    let cd = CDCard.create(in: context)
                    cd.fill(card)
                })
            } catch {
                Analytics.report(error)
            }
        }, fail: { error in
            Analytics.report(error)
        })
    }
}
