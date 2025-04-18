//
//  Ellipse.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/11/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import CoreLocation

protocol LocationPresentable {
    var latitude: Double? {get set}
    var longitude: Double? {get set}
}

public struct Ellipse: Decodable, LocationPresentable {
    var latitude: Double?
    var longitude: Double?
    let macId: String
    let lockId: Int
    let fleetId: Int
    var fleetKey: String?
    var name: String?
    var bikeName: String?
    var bikeId: Int?
    var eBikeKey: String?
    var emptyPin: Bool?
}

extension LocationPresentable {
    var coordinate: CLLocationCoordinate2D {
        set {
            latitude = newValue.latitude
            longitude = newValue.longitude
        }
        get {
            guard let lat = latitude, let lng = longitude else { return kCLLocationCoordinate2DInvalid }
            return .init(latitude: lat, longitude: lng)
        }
    }
}

