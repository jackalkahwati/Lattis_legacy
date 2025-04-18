//
//  DirectionsStorage.swift
//  Lattis
//
//  Created by Ravil Khusainov on 22/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

protocol DirectionsStorage {
    var recient: [Direction] { get }
    func save(_ direction: Direction)
}
