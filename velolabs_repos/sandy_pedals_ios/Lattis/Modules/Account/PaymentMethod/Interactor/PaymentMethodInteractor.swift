//
//  PaymentMethodPaymentMethodInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/06/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation
import Oval

class PaymentMethodInteractor {
    var onClose: () -> () = {}
    weak var view: PaymentMethodInteractorOutput!
    var router: PaymentMethodRouter!
    
    internal let accessory: CreditCardCell.Accessory
    fileprivate let storage: CreditCardStorage
    fileprivate let network: CardsNetwork
    
    init(network: CardsNetwork = Session.shared, storage: CreditCardStorage = CoreDataStack.shared, accessory: CreditCardCell.Accessory = .none) {
        self.network = network
        self.storage = storage
        self.accessory = accessory
    }
}

extension PaymentMethodInteractor: PaymentMethodInteractorInput {
    func viewWillAppear() {
        AppRouter.shared.refreshCards { [weak self] (cards) in
            self?.view.show(cards: cards)
        }
    }

    func open(card: CreditCard?) {
        router.open(card: card)
    }
    
    func delete(card: CreditCard) {
        view.startLoading(with: "credit_card_delete_loading".localized())
        network.delete(card: card) { [weak self] (result) in
            switch result {
            case .success:
                self?.view.stopLoading(completion: nil)
                self?.storage.delete(card: card)
                self?.viewWillAppear()
            case .failure(let error):
                self?.view.show(error: error, file: #file, line: #line)
            }
        }
    }
    
    func select(card: CreditCard) {
        view.startLoading(with: "credit_card_select_loading".localized())
        network.setPrimary(card: card) { [weak self] (result) in
            switch result {
            case .success:
                self?.view.stopLoading(completion: nil)
                self?.storage.setCurrent(card: card)
                self?.viewWillAppear()
            case .failure(let error):
                self?.view.show(error: error, file: #file, line: #line)
            }
        }
    }
    
    func close() {
        onClose()
    }
}
