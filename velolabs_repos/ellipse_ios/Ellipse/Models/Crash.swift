//
//  Crash.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/21/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreLocation

struct Crash: Decodable {
    let crahId: Int
    let accelerometerValue: Ellipse.AccelerometerValue
    let messageSent: Bool
    let lockId: Int
    let userId: Int
    let date: Date
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.crahId = try container.decode(Int.self, forKey: .crashId)
        self.lockId = try container.decode(Int.self, forKey: .lockId)
        self.userId = try container.decode(Int.self, forKey: .userId)
        let x_ave = try container.decode(Float.self, forKey: .xAve)
        let y_ave = try container.decode(Float.self, forKey: .yAve)
        let z_ave = try container.decode(Float.self, forKey: .zAve)
        let ave = Ellipse.AccelerometerValue.Coordinate(x: x_ave, y: y_ave, z: z_ave)
        let x_dev = try container.decode(Float.self, forKey: .xDev)
        let y_dev = try container.decode(Float.self, forKey: .yDev)
        let z_dev = try container.decode(Float.self, forKey: .zDev)
        let dev = Ellipse.AccelerometerValue.Coordinate(x: x_dev, y: y_dev, z: z_dev)
        self.accelerometerValue = Ellipse.AccelerometerValue(ave: ave, dev: dev)
        self.date = try container.decode(Date.self, forKey: .date)
        let sent = try container.decode(Int.self, forKey: .messageSent)
        self.messageSent = sent > 0
    }
    
    enum CodingKeys: String, CodingKey {
        case crashId, lockId, userId, messageSent, date, xAve, yAve, zAve, xDev, yDev, zDev
    }
}

extension Crash {
    struct Info: Codable {
        let macId: String
        let accelerometerValue: Ellipse.AccelerometerValue
        var location: CLLocationCoordinate2D = kCLLocationCoordinate2DInvalid
        init(macId: String, accelerometerValue: Ellipse.AccelerometerValue, location: CLLocationCoordinate2D) {
            self.macId = macId
            self.accelerometerValue = accelerometerValue
            self.location = location
        }
        
        init(from decoder: Decoder) throws {
            let values = try decoder.container(keyedBy: CodingKeys.self)
            macId = try values.decode(String.self, forKey: .macId)
            accelerometerValue = try values.decode(Ellipse.AccelerometerValue.self, forKey: .accelerometerData)
            
            let location = try values.nestedContainer(keyedBy: LocationKeys.self, forKey: .location)
            let latitude = try location.decode(Double.self, forKey: .latitude)
            let longitude = try location.decode(Double.self, forKey: .longitude)
            self.location = CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
        }
        
        func encode(to encoder: Encoder) throws {
            var container = encoder.container(keyedBy: CodingKeys.self)
            try container.encode(macId, forKey: .macId)
            try container.encode(accelerometerValue, forKey: .accelerometerData)
            
            var location = container.nestedContainer(keyedBy: LocationKeys.self, forKey: .location)
            try location.encode(self.location.latitude, forKey: .latitude)
            try location.encode(self.location.longitude, forKey: .longitude)
        }
        
        enum CodingKeys: String, CodingKey {
            case macId, accelerometerData, location
        }
        enum LocationKeys: String, CodingKey {
            case latitude, longitude
        }
    }
}
