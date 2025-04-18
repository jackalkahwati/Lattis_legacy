//
//  Address.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 11/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import CoreLocation
import MapboxGeocoder

struct Address {
    let name: String
    let coordinate: CLLocationCoordinate2D
    let street: String?
    let city: String?
    let country: String?
}


extension Address {
    init?(_ placemark: GeocodedPlacemark) {
        guard let coordinate = placemark.location?.coordinate else { return nil }
        let city = placemark.superiorPlacemarks?.filter({$0.scope == .place}).first
        let street = placemark.superiorPlacemarks?.filter({$0.scope == .address}).first
        let country = placemark.superiorPlacemarks?.filter({$0.scope == .country}).first
        self.name = placemark.formattedName
        self.street = street?.name ?? placemark.postalAddress?.street
        self.city = city?.name ?? placemark.postalAddress?.city
        self.country = country?.name ?? placemark.postalAddress?.country
        self.coordinate = coordinate
    }
}

extension Address: Equatable {
    static func == (lhs: Address, rhs: Address) -> Bool {
        return lhs.name == rhs.name && lhs.coordinate == rhs.coordinate
    }
}

extension Address: Codable {}
