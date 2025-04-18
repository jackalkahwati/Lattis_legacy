//
//  BlufAPI.swift
//  BLUF
//
//  Created by Ravil Khusainov on 20.09.2020.
//

import Foundation
import Combine


enum CircleAPI {
    static let agent = Agent("http://garry.local:8000")
}

extension CircleAPI {
    static func lock(_ id: Int) -> AnyPublisher<EllipseLock.Metadata, Error> {
        agent.run(.get("lock/\(id)"))
    }
    
    static func locks(_ filter: LockFilter? = nil) -> AnyPublisher<[EllipseLock.Metadata], Error> {
        var items = [URLQueryItem]()
        if let f = filter {
            switch f {
            case .macId(let mac):
                items.append(.init(name: "mac_id", value: mac))
            case .fleet(let id):
                items.append(.init(name: "fleet_id", value: "\(id)"))
            }
        }
        return agent.run(.get("locks", queryItems: items))
    }
    
    enum LockFilter {
        case macId(String)
        case fleet(Int)
    }
}

extension CircleAPI {
    static func fleet(_ id: Int) -> AnyPublisher<Fleet, Error> {
        agent.run(.get("fleet/\(id)"))
    }
    
    static func fleets() -> AnyPublisher<[Fleet], Error> {
        agent.run(.get("fleets"))
    }
}

extension CircleAPI {
    static func bikes(_ filters: [Bike.Filter] = []) -> AnyPublisher<[Bike], Error> {
        var items = [URLQueryItem]()
        for f in filters {
            guard !f.value.isEmpty else { continue }
            items.append(.init(name: f.key.rawValue, value: f.value))
        }
        return agent.run(.get("bike", queryItems: items))
    }
    
    static func bike(_ id: Int) -> AnyPublisher<Bike, Error> {
        agent.run(.get("bike/\(id)"))
    }
    
    static func deleteBike(_ id: Int) -> AnyPublisher<Agent.Empty, Error> {
        agent.run(.delete("bike/\(id)"))
    }
}

extension Bike {
    struct Filter {
        let key: Key
        let value: String
        
        enum Key: String {
            case name
            case status
            case usage
            case lock
            case fleet
        }
    }
}

extension Bike.Filter.Key: Identifiable {
    var id: String { rawValue }
    var title: String { rawValue.capitalized }
}

extension Bike.Filter: Identifiable {
    var id: String { key.id }
}
