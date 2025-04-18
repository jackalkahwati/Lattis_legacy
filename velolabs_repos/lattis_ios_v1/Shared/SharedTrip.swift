//
//  SharedTrip.swift
//  Lattis
//
//  Created by Ravil Khusainov on 17.01.2020.
//  Copyright © 2020 Velo Labs. All rights reserved.
//

import Foundation

struct SharedTrip: Codable {
    let duration: String
    let fare: String?
    let bikeName: String
    let isLocked: Bool
}

extension SharedTrip {
    func updated(isLocked: Bool) -> SharedTrip {
        .init(duration: duration, fare: fare, bikeName: bikeName, isLocked: isLocked)
    }
}
