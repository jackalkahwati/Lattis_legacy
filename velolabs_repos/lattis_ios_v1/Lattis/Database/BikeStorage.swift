//
//  BikeStorage.swift
//  Lattis
//
//  Created by Ravil Khusainov on 5/1/18.
//  Copyright © 2018 Velo Labs. All rights reserved.
//

import Foundation

protocol BikeStorage {
    func save(_ bike: Bike)
    func deleteAll()
    func bike(by id: Int) -> Bike?
}
