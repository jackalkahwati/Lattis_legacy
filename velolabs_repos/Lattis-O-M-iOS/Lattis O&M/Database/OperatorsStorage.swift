//
//  OperatorsStorage.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/17/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol OperatorsStorage {
    func save(_ operators: [Operator], for fleet: Fleet, update: Bool)
    func getOperators(by fleet: Fleet) -> [Operator]
}
