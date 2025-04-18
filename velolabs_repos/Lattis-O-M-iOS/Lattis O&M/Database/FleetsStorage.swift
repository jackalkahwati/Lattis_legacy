//
//  FleetsStorage.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/13/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol FleetsStorage {
    var currentFleet: Fleet? {get}
    func fleet(with fleetId: Int32) -> Fleet?
    func save(_ fleets: [Fleet], update: Bool, completion: @escaping () -> ())
    func subscribe(completion: @escaping ([Fleet]) -> ()) -> StorageHandler
}
