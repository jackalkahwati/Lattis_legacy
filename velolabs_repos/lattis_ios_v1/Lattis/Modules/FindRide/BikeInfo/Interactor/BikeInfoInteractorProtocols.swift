//
//  BikeInfoBikeInfoInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 20/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

protocol BikeInfoInteractorInput {
    func bookBike()
    func viewWillAppear()
    func addCreditCard()
    func select(card: CreditCard)
    func showZones(for network: PrivateNetwork)
    func openTerms(for bike: Bike)
}

protocol BikeInfoInteractorOutput: BaseInteractorOutput {
    func update(info: Bike, cards: [CreditCard])
//    func showLocations(pickUp: String?, dropOf: String?)
}
