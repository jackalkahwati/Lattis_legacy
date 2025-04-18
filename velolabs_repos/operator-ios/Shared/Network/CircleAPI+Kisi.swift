//
//  CircleAPI+Kisi.swift
//  Operator
//
//  Created by Ravil Khusainov on 24.11.2021.
//

import Foundation
import Combine

extension CircleAPI {
    static func device(_ thing: Thing) -> AnyPublisher<KisiDevice.Lock, Error> {
        agent.run(.get("operator/things/kisi/\(thing.metadata.key)", queryItems: [.init(name: "fleetId", value: thing.fleetId)]))
    }
    
    static func unlock(_ thing: Thing) -> AnyPublisher<Void, Error> {
        agent.run(.put("operator/things/kisi/\(thing.metadata.key)/unlock", queryItems: [.init(name: "fleetId", value: thing.fleetId)]))
    }
}

fileprivate extension Thing {
    var fleetId: String? {
        guard let id = metadata.fleetId else { return nil }
        return "\(id)"
    }
}
