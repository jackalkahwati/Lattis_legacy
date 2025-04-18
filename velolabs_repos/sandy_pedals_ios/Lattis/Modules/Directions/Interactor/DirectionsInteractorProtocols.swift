//
//  DirectionsInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 14/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

protocol DirectionsInteractorDelegate: class {
    func didSelect(direction: Direction) -> Bool
    func didSelectCurrentLocation() -> Bool
}

protocol DirectionsInteractorInput {
    func select(_ direction: Direction)
    func getRecientDirections()
    func selectCurrentLocation()
}

protocol DirectionsInteractorOutput: class {
    func show(directions: [Direction])
}
