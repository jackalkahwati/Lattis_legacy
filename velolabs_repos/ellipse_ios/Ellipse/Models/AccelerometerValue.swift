//
//  AccelerometerValue.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/21/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

extension Ellipse {
    struct AccelerometerValue: Codable {
        let ave: Coordinate
        let dev: Coordinate
        
        init(ave: Coordinate, dev: Coordinate) {
            self.ave = ave
            self.dev = dev
        }
        
        struct Coordinate {
            let x:Float
            let y:Float
            let z:Float
        }
        
        init(from decoder: Decoder) throws {
            let values = try decoder.container(keyedBy: CodingKeys.self)
            let x_ave = try values.decode(Float.self, forKey: .xAve)
            let y_ave = try values.decode(Float.self, forKey: .yAve)
            let z_ave = try values.decode(Float.self, forKey: .zAve)
            self.ave = Coordinate(x: x_ave, y: y_ave, z: z_ave)
            let x_dev = try values.decode(Float.self, forKey: .xDev)
            let y_dev = try values.decode(Float.self, forKey: .yDev)
            let z_dev = try values.decode(Float.self, forKey: .zDev)
            self.dev = Coordinate(x: x_dev, y: y_dev, z: z_dev)
        }
        
        func encode(to encoder: Encoder) throws {
            var container = encoder.container(keyedBy: CodingKeys.self)
            try container.encode(ave.x, forKey: .xAve)
            try container.encode(ave.y, forKey: .yAve)
            try container.encode(ave.z, forKey: .zAve)
            try container.encode(dev.x, forKey: .xDev)
            try container.encode(dev.y, forKey: .yDev)
            try container.encode(dev.z, forKey: .zDev)
        }
        
        enum CodingKeys: String, CodingKey {
            case xAve, yAve, zAve, xDev, yDev, zDev
        }
    }
}
