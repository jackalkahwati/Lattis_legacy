//
//  Route+Lattis.swift
//  Lattis
//
//  Created by Ravil Khusainov on 25/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import CoreLocation

extension CLLocationDistance {
    var string: String {
        if self < 1000 {
            return "\(Int(self)) " + "general_distance_metters".localized()
        }
        let km = self/1000
        return String(format: "%0.1f ", km) + "general_distance_km".localized()
    }
}
