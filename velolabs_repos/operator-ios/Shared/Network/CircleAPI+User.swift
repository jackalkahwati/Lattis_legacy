//
//  CircleAPI+User.swift
//  Operator
//
//  Created by Ravil Khusainov on 27.08.2021.
//

import Foundation
import Combine

extension CircleAPI {
    static func getBlockedUser(id: Int) -> AnyPublisher<String, Error> {
        struct BlockedUser: Codable {
            let phoneNumber: String
        }
        return agent.run(.get("operator/users/\(id)/blocked"))
            .map { (user: BlockedUser) in
                user.phoneNumber
            }
            .eraseToAnyPublisher()
    }
    
    static func blockUser(id: Int) -> AnyPublisher<Void, Error> {
        agent.run(.put("operator/users/\(id)/block"))
    }
    
    static func unBlockUser(id: Int) -> AnyPublisher<Void, Error> {
        agent.run(.put("operator/users/\(id)/unblock"))
    }
}
