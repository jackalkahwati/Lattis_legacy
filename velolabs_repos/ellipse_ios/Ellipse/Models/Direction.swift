//
//  Direction.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

struct Direction: Decodable {
    let distance: String
    let duration: String
    let startAddress: String
    let endAddress: String
    let instructions: String
    let steps: [Step]
    
    struct Step: Decodable {
        let htmlInstructions: String
    }
}

extension Direction {
    public init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        let routes = try values.decode([Route].self, forKey: .routes)
        guard routes.count > 0 else { throw  Err.routesNotFound }
        let legs = routes[0].legs
        guard legs.count > 0 else { throw Err.routesNotFound }
        let leg = legs[0]
        steps = leg.steps
        distance = leg.distance
        duration = leg.duration
        instructions = steps.map({$0.htmlInstructions.htmlClean}).joined(separator: "\n")
        startAddress = leg.startAddress
        endAddress = leg.endAddress
    }
    
    enum CodingKeys: String, CodingKey {
        case routes
    }
    
    enum ValueKeys: String, CodingKey {
        case text, value
    }
    
    struct Route: Decodable {
        let legs: [Leg]
        struct Leg: Decodable {
            let startAddress: String
            let endAddress: String
            let distance: String
            let duration: String
            let steps: [Step]
            
            public init(from decoder: Decoder) throws {
                let values = try decoder.container(keyedBy: CodingKeys.self)
                steps = try values.decode([Step].self, forKey: .steps)
                let distance = try values.nestedContainer(keyedBy: ValueKeys.self, forKey: .distance)
                self.distance = try distance.decode(String.self, forKey: .text)
                let duration = try values.nestedContainer(keyedBy: ValueKeys.self, forKey: .duration)
                self.duration = try duration.decode(String.self, forKey: .text)
                startAddress = try values.decode(String.self, forKey: .startAddress)
                endAddress = try values.decode(String.self, forKey: .endAddress)
            }
            
            enum CodingKeys: String, CodingKey {
                case distance, duration, steps, startAddress, endAddress
            }
        }
    }
    
    enum Err: Error {
        case routesNotFound
    }
}

extension String {
    var htmlClean: String {
        return self.replacingOccurrences(of: "<[^>]+>", with: "", options: .regularExpression, range: nil)
    }
}

