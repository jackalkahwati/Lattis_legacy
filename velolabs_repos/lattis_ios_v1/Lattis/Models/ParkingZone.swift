//
//  ParkingZone.swift
//  Lattis
//
//  Created by Ravil Khusainov on 5/31/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import CoreLocation

public struct ParkingZone: Decodable {
    public let areaId: Int
    public var name: String?
    public var geometry: Shape
    
    public init(from decoder: Decoder) throws {
        struct Geo: Decodable {
            let latitude: Double
            let longitude: Double
            let radius: Double?
        }
        func transform(_ geo: Geo) -> CLLocationCoordinate2D {
            return .init(latitude: geo.latitude, longitude: geo.longitude)
        }
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.areaId = try container.decode(Int.self, forKey: .parkingAreaId)
        self.name = try container.decodeIfPresent(String.self, forKey: .name)
        let geometry = try container.decode([Geo].self, forKey: .geometry)
        let dots = geometry.map(transform)
        if let geo = geometry.first, let radius = geo.radius {
            let center = transform(geo)
            self.geometry = .circle(.init(center: center, radius: radius))
        } else {
            let type = try container.decode(String.self, forKey: .type)
            switch type {
            case "polygon" where dots.count > 2:
                self.geometry = .polygon(dots)
            case "rectangle" where dots.count > 2:
                self.geometry = .rectangle(dots)
            default:
                self.geometry = .none
            }
        }
    }
    
    public enum CodingKeys: String, CodingKey {
        case parkingAreaId
        case name
        case geometry
        case type
    }
}

public extension ParkingZone {
    struct Circle {
        let center: CLLocationCoordinate2D
        let radius: Double
    }
    enum Shape {
        case none
        case polygon([CLLocationCoordinate2D])
        case circle(Circle)
        case rectangle([CLLocationCoordinate2D])
    }
}
