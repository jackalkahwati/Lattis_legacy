//
//  DirectionsInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 14/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

final class DirectionsInteractor {
    weak var view: DirectionsInteractorOutput!
    var router: DirectionsRouter!
    weak var delegate: DirectionsInteractorDelegate?
    let storage: DirectionsStorage
    
    init(storage: DirectionsStorage = CoreDataStack.shared) {
        self.storage = storage
    }
}

extension DirectionsInteractor: DirectionsInteractorInput {
    func select(_ direction: Direction) {
        if let del = delegate, del.didSelect(direction: direction) {
            router.dismiss()
        }
        storage.save(direction)
    }
    
    func getRecientDirections() {
        view.show(directions: storage.recient)
    }
    
    func selectCurrentLocation() {
        if let del = delegate, del.didSelectCurrentLocation() {
            router.dismiss()
        }
    }
}
