//
//  Oval+Subscription.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import OvalAPI
import Model

fileprivate extension API {
    static var memberships: API {
        .init(path: "memberships")
    }
    
    static func subscribe(_ membership: Membership) -> API {
        .init(path: "memberships/\(membership.id)/subscribe")
    }
    
    static func unsubscribe(_ membership: Membership) -> API {
        .init(path: "memberships/\(membership.id)/unsubscribe")
    }
    
    static let subscriptions: API = .init(path: "subscriptions")
}

extension Session: SubscriptionsAPI {
    func fetchMemberships(completion: @escaping (Result<[Membership], Error>) -> ()) {
        send(.get(.memberships, dateAsTimestamp: false), completion: completion)
    }
    
    func fetchSubscriptions(completion: @escaping (Result<[Subscription], Error>) -> ()) {
        send(.get(.subscriptions, dateAsTimestamp: false), completion: completion)
    }
    
    func subscribe(to membership: Membership, completion: @escaping (Result<Subscription, Error>) -> ()) {
        send(.post(json: Empty(), api: .subscribe(membership), dateAsTimestamp: false), completion: completion)
    }
    
    func unsubscribe(from membership: Membership, completion: @escaping (Result<Subscription, Error>) -> ()) {
        send(.patch(Empty(), api: .unsubscribe(membership), dateAsTimestamp: false), completion: completion)
    }
}
