//
//  Oval+Geofence.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 27.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import OvalAPI

fileprivate extension API {
    static func geofences(_ fleetId: Int) -> API {
        .init(path: "fleet/geofences?fleet_id=\(fleetId)")
    }
}

extension Session: GeofenceAPI {
    func fetch(by fleetId: Int, completion: @escaping (Result<[Geofence], Error>) -> ()) {
        send(.get(.geofences(fleetId)), completion: completion)
    }
}
