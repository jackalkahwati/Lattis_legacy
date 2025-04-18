//
//  CircleAPI+Invers.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.11.2021.
//

import Foundation
import Combine

extension CircleAPI {
    static func status(_ qnr: String) -> AnyPublisher<InversDevice.Status, Error> {
        agent.run(.get("operator/things/invers/\(qnr)/status"))
    }
    
    static func change(_ status: InversDevice.Status, inverse qnr: String) -> AnyPublisher<Void, Error> {
        agent.run(.put(status, path: "operator/things/invers/\(qnr)/status"))
    }
}
