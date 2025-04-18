//
//  FleetNetwork.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/13/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Oval

protocol FleetNetwork {
    func getFleets(completion: @escaping (Result<[Fleet], Error>) -> ())
}

fileprivate extension API {
    static let fleets = API(path: "fleet/get-fleets")
}

extension Session: FleetNetwork {
    func getFleets(completion: @escaping (Result<[Fleet], Error>) -> ()) {
        send(.get(.fleets), completion: completion)
    }
}
