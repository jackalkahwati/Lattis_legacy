//
//  Bike+Mock.swift
//  CoreTests
//
//  Created by Ravil Khusainov on 02.10.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
@testable import LattisCore

extension Bike {
    static func mock() -> Bike {
        .init(bikeId: 1, macId: "test", lockId: 1, fleetKey: "test", name: "test", fleetName: "test", fleetLogo: nil, picture: nil, skipParkingImage: nil, requirePhoneNumber: nil, latitude: 0, longitude: 0, kind: .electric, priceAmount: 3, parkingPriceAmount: nil, fleetType: .privateFree, terms: nil, fleetId: 1, details: "", make: "", model: "", priceDuration: nil, pricePeriod: nil, currency: nil, excessUsageFees: nil, excessUsageDuration: nil, excessUsagePeriod: nil, excessUsageAfterPeriod: nil, excessUsageAfterDuration: nil, unlockFee: nil, controllers: nil, reservationSettings: nil, qrCodeId: nil, contactEmail: nil, customerName: nil)
    }
}

extension Bike.Booking {
    static func mock(bike: Bike = .mock()) -> Bike.Booking {
        .init(bike: bike, bookedOn: Date(), duration: 15)
    }
}
