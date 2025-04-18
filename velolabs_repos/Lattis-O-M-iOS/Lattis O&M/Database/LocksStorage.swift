//
//  LockStorage.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/11/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol LocksStorage {
    func lock(with macId: String) -> Ellipse?
    func save(_ locks: [Ellipse], update: Bool, completion: @escaping () -> ())
    func subscribe(in fleet: Fleet, completion: @escaping ([Ellipse]) -> ()) -> StorageHandler
}
