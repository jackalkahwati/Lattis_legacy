//
//  CircleAPI+Map.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.06.2021.
//

import Foundation
import Combine
import CoreLocation

struct BBox {
    let coordinates: [CLLocationDegrees]
    var query: String {
        coordinates.map({String(format: "%.6f", $0)}).joined(separator: ",")
    }
}

extension BBox {
    init(ne: CLLocationCoordinate2D, sw: CLLocationCoordinate2D) {
        self.init(coordinates: [sw.latitude, ne.latitude, sw.longitude, ne.longitude])
    }
}

extension CircleAPI {
    static func vehicles(map bbox: BBox, fleetId: Int, filters: [Vehicle.Filter]) -> AnyPublisher<[Vehicle.Metadata], Error> {
        var items: [URLQueryItem] = [
            .init(name: "bbox", value: bbox.query),
            .init(name: "fleet_id", value: "\(fleetId)")
        ]
        filters.forEach { filter in
            switch filter {
            case .maintenance(let mnt):
                items.append(.init(name: "maintenance", value: mnt.map(\.rawValue).joined(separator: ",")))
            case .usage(let usages):
                items.append(.init(name: "usage", value: usages.map(\.rawValue).joined(separator: ",")))
            case .name(let name):
                items.append(.init(name: "name", value: name))
            case .batterLevel(let level):
                items.append(.init(name: "battery-level", value: "\(level)"))
            }
        }
        return agent.run(.get("operator/map/vehicles", queryItems: items))
    }
}
