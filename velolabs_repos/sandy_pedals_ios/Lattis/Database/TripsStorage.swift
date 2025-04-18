//
//  TripsStorage.swift
//  Lattis
//
//  Created by Ravil Khusainov on 15/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

protocol TripsStorage {
    func save(_ trip: Trip)
    func update(trips: [Trip])
    func subsribe(target: AnyHashable, callback: @escaping ([Trip]) -> ())
    func unsubscribe(target: AnyHashable)
    func trip(by id: Int) -> Trip?
}
