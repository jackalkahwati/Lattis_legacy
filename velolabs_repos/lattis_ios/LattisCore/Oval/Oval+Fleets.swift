//
//  Oval+Fleets.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 21.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import OvalAPI
import Model

extension API {
    static let fleets = API(path: "fleet")
}

extension Session: FleetsNetwork {
    func fetchFleets(completion: @escaping (Result<[Model.Fleet], Error>) -> ()) {
        send(.get(.fleets, dateAsTimestamp: false), completion: completion)
    }
}
