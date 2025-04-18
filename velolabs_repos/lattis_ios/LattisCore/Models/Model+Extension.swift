//
//  Model+Extension.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 27.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import CoreLocation
import Model

extension Model.Bike {
    public var coordinate: CLLocationCoordinate2D {
        guard let lat = latitude, let lon = longitude else { return kCLLocationCoordinate2DInvalid }
        return .init(lat, lon)
    }
}

extension Model.Bike.BikeType {
    var localizedTitle: String {
        switch self {
        case .kickScooter:
            return "kick_scooter".localized()
        default:
            return (rawValue + "_bike").localized()
        }
    }
}

extension Model.Bike: Equatable {
    public static func == (lhs: Model.Bike, rhs: Model.Bike) -> Bool {
        return lhs.bikeId == rhs.bikeId &&
            lhs.coordinate == rhs.coordinate
    }
}

extension Model.Fleet {
    var isPayment: Bool {
        type == .privatePay || type == .publicPay
    }
    
    var isFree: Bool {
        type == .publicFree || type == .privateFree
    }
}

extension Membership {
    var priceString: String? {
        price.price(for: currency)
    }
}

extension Membership.Frequency {
    var cycle: String { rawValue.localized() }
    var priceCycle: String {
        switch self {
        case .monthly:
            return "month".localized()
        case .weekly:
            return "week".localized()
        case .yearly:
            return "year".localized()
        }
    }
}

extension Reservation {
    var notificationId: String { "reservation_\(reservationId)" }
    var canStartTrip: Bool { reservationStart < Date() }
    var inAsingleDay: Bool { Calendar.current.isDate(reservationStart, inSameDayAs: reservationEnd) }
}


extension Reservation.Estimate {
    var price: String? {
        if amount == 0 {
            return "bike_detail_bike_cost_free".localized()
        }
        return amount.price(for: currency)
    }
}

extension Payment.Settings {
    var price: String? {
//        guard let fType = fleetType,
//            !(fType == .privateFree || fType == .publicFree),
//            let amount = priceAmount,
//            amount > 0,
//            let value = amount.price(for: currency),
//            let duratoin = priceDuration,
//            let period = pricePeriod else { return nil }
//        return "membership_pricing_template".localizedFormat(value, period.lowercased().localizedFormat(duratoin))
        if priceForMembership == 0 {
            return "bike_detail_bike_cost_free".localized()
        }
        guard let price = priceForMembership.price(for: currency) else { return nil }
        return "membership_pricing_template".localizedFormat(price, priceType.lowercased().localizedFormat(priceTypeValue))
//        return priceForMembership.price(for: currency)
    }
    
    var fullPrice: String {
        var result = ""
        if let unlock = unlockPrice {
            result = "unlock_template".localizedFormat(unlock)
        }
        if let price = price {
            if result.isEmpty {
                result = price
            } else {
                result += " + " + price
            }
        }
        // This will be computed if we don't have an unlock fee
        if let fee = excessUsageFees,
           fee > 0, unlockPrice == nil,
           let duration = excessUsageTypeAfterValue,
           let period = excessUsageTypeAfterType {
            // Display only if we dont have a base fare
            if priceForMembership == 0 {
                // Accounts for surcharge fee only
                result = "free_for_duration".localizedFormat(period.lowercased().localizedFormat(duration))
            }
        }
        if result.isEmpty {
            result = "bike_detail_bike_cost_free".localized()
        }
        return result
    }
    
    var parkingPrice: String? {
        guard let amount = priceForPenaltyOutsideParking,
              amount > 0,
              let p = amount.price(for: currency) else { return nil }
        return p
    }
    
    var surchargePrice: String? {
        
        let duration = excessUsageTypeValue
        let period = excessUsageType
        guard let fee = excessUsageFees,
              fee > 0,
              let price = fee.price(for: currency) else { return nil }
        return "membership_pricing_template".localizedFormat(price, period.lowercased().localizedFormat(duration))
    }
    
    var surchargeDescription: String? {
        guard let value = excessUsageTypeAfterValue,
              value > 0,
              let t = excessUsageTypeAfterType else { return nil }
        return "surcharge_description".localizedFormat(t.lowercased().localizedFormat(value))
    }
    
    var unlockPrice: String? {
        guard let fee = priceForBikeUnlock, fee > 0 else { return nil }
        return fee.price(for: currency)
    }
}

extension Reservation.BikesRequest {
    var string: String {
        let formatter = ISO8601DateFormatter()
        let start = formatter.string(from: reservationStart).addingPercentEncoding(withAllowedCharacters: .urlHostAllowed)!
        let end = formatter.string(from: reservationEnd).addingPercentEncoding(withAllowedCharacters: .urlHostAllowed)!
        return "?fleet_id=\(fleetId)&reservation_start=\(start)&reservation_end=\(end)"
    }
}

extension Bike {
    func iot<T: Equatable>(key: KeyPath<Thing, T>, isEqualTo value: T) -> Thing? {
        controllers?.first(where: {$0[keyPath: key] == value})
    }
}

extension Model.Bike {
    func iot<T: Equatable>(key: KeyPath<Thing, T>, isEqualTo value: T) -> Thing? {
        controllers?.first(where: {$0[keyPath: key] == value})
    }
}

extension Payment {
    var title: String {
        switch self {
        case .applePay:
            return "Apple Pay"
        case .card(let card):
            return card.title
        case .addNew:
            return "add_credit_card".localized()
        }
    }
    
    var icon: UIImage? {
        switch self {
        case .applePay:
            return .named("icon_payment_applepay")
        case .card(let card):
            return card.icon
        case .addNew:
            return .named("icon_credit_card")
        }
    }
}

extension Payment.Card {
    var title: String {
        return "XXXX-" + String(number.suffix(4))
    }
    
    var date: String {
        return .init(format: "%02d/%02d", month, year)
    }
    
    var icon: UIImage? {
        if let s = Payment.System(rawValue: system) {
            return s.icon
        }
        return .named("icon_credit_card")
    }
}

extension Payment.Settings {
    var parkingFee: String? {
        guard let price = priceForPenaltyOutsideParking, price > 0 else { return nil }
        return price.price(for: currency)
    }
    
    var preauthPrice: String? {
        guard
            let preauth = enablePreauth,
              preauth,
              let amount = preauthAmount,
              amount > 0 else { return nil }
        return amount.price(for: currency)
    }
}

extension Model.Fleet.Address {
    func copmose(_ keys: [KeyPath<Self, String>], separator: String = ", ") -> String {
        keys.map({ self[keyPath: $0] }).joined(separator: separator)
    }
}


