//
//  CircleAPI+Ellipse.swift
//  Operator
//
//  Created by Ravil Khusainov on 25.10.2021.
//

import Foundation
import Combine

extension CircleAPI {
    static func credentials(lockId: Int) -> AnyPublisher<EllipseDevice.Credentials, Error> {
        agent.run(.get("operator/things/ellipse/\(lockId)/credentials"))
    }
    
    static func pin(lockId: Int) -> AnyPublisher<[EllipseDevice.Pin], Error> {
        agent.run(.get("operator/things/ellipse/\(lockId)/pin"))
    }
}

extension EllipseDevice {
    struct Credentials: Codable {
        let macId: String
        let signedMessage: String
        let publicKey: String
        let secret: String
    }
    
    enum Pin: String, Codable {
        case up, down, left, right
    }
}
