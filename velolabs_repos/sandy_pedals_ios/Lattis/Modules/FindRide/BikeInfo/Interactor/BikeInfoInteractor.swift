//
//  BikeInfoBikeInfoInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 20/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Oval

protocol BikeInfoInteractorDelegate: class {
    func bikeInfoBook()
}

final class BikeInfoInteractor {
    weak var view: BikeInfoInteractorOutput!
    var router: BikeInfoRouter!
    var bike: Bike! {
        didSet {
            bike.network = storage.privateNetwork(by: bike.fleetId)
        }
    }
    weak var delegate: BikeInfoInteractorDelegate?
    
    fileprivate let storage: UserStorage & CreditCardStorage
    fileprivate let network: CardsNetwork
    init(storage: UserStorage & CreditCardStorage = CoreDataStack.shared, network: CardsNetwork = Session.shared) {
        self.storage = storage
        self.network = network
    }
}

extension BikeInfoInteractor: BikeInfoInteractorInput {
    func bookBike() {
        delegate?.bikeInfoBook()
    }
    
    func showZones(for network: PrivateNetwork) {
        router.openZones(of: network.fleetId)
    }
    
    func openTerms(for bike: Bike) {
        guard let terms = bike.termsLink else { return }
//        let url = URL(string: "https://s3-us-west-1.amazonaws.com/terms.and.conditions/sidlee_t_and_c.txt")!
        router.openTems(with: terms)
    }
    
    func viewWillAppear() {
        AppRouter.shared.refreshCards { [weak self] (cards) in
            guard let bike = self?.bike else { return }
            self?.view.update(info: bike, cards: cards)
        }
    }
    
    func addCreditCard() {
        router.addCreditCard {_ in}
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
}

