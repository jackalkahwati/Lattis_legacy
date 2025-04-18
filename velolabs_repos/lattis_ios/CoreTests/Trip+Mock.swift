//
//  Trip+Mock.swift
//  CoreTests
//
//  Created by Ravil Khusainov on 02.10.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
@testable import LattisCore

extension Trip {
    static func mock() -> Trip {
        .init(tripId: 1, bikeId: 1, fleetId: 1, startedAt: Date(), endedAt: nil, disableTracking: nil, fare: nil, parkingFee: nil, surchargeFee: nil, penaltyFees: nil, totalPrice: nil, currency: nil, isStarted: false, logo: nil, fleetName: nil, startAddress: nil, endAddress: nil, unlockFee: nil, bike: nil)
    }
}
