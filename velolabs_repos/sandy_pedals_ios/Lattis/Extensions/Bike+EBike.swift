//
//  Bike+EBike.swift
//  Lattis
//
//  Created by Ravil Khusainov on 8/1/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

extension Bike {
    var batteryLevelString: String? {
        guard let level = batteryLevel else { return nil }
        return "\(Int(level*100.0)) %"
    }
    
    var bikeBatteryLevelString: String? {
        guard let level = bikeBatteryLevel, bikeType == .eBike else { return "general_no_info".localized() }
        return "\(Int(level*100.0)) %"
    }
}
