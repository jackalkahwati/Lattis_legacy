//
//  Bike.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 17/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import CoreLocation
import Model

public struct Bike: Codable {
    
    public let bikeId: Int
    public let macId: String?
    public let lockId: Int?
    public let fleetKey: String?
    public let name: String
    public let fleetName: String?
    public let fleetLogo: URL?
    public let picture: URL?
    public let skipParkingImage: Bool?
    public let requirePhoneNumber: Bool?
    let latitude: Double?
    let longitude: Double?
    public let kind: Kind
    public let priceAmount: Double?
    public let parkingPriceAmount: Double?
    public let fleetType: Model.Fleet.ChargeType?
    public let terms: String?
    public let fleetId: Int
    public let details: String
    public let make: String
    public let model: String
    public let priceDuration: Int?
    public let pricePeriod: String?
    public let currency: String?
    public let excessUsageFees: Double?
    public let excessUsageDuration: Int?
    public let excessUsagePeriod: String?
    public let excessUsageAfterPeriod: String?
    public let excessUsageAfterDuration: Int?
    public let unlockFee: Double?
    public let controllers: [Thing]?
    public let bikeBatteryLevel: Int?
    let reservationSettings: Reservation.Settings?
    let reservation: Reservation.Pending?
    let paymentGateway: Payment.Gateway?
    let qrCodeId: Int?
    let qrCode: String?
    let contactEmail: String?
    let customerName: String?
    let bikeUuid: String?
    let pricingOptions: [Pricing]?
    let preauthAmount: Double?
    let enablePreauth: Bool?
    public let promotions: [Promotion]?
    public let promotion: Promotion?
}

public extension Bike {
    var isPayment: Bool {
        guard let fType = fleetType else { return false }
        return fType == .privatePay || fType == .publicPay
    }
    
    var isFree: Bool {
        if let fType = fleetType, fType == .privateFree || fType == .publicFree { return true }
        if let price = priceAmount, price > 0 { return false }
        if let unlock = unlockFee, unlock > 0 { return false }
        if let parking = parkingPriceAmount, parking > 0 { return false }
        return true
    }
    
    var noCardNeeded: Bool {
        guard let tp = fleetType else { return false }
        return tp == .publicFree || tp == .privateFree
    }
    
    var price: String? {
        guard let fType = fleetType,
            !(fType == .privateFree || fType == .publicFree),
            let amount = priceAmount,
            amount > 0,
            let value = amount.price(for: currency),
            let duratoin = priceDuration,
            let period = pricePeriod else { return nil }
        return "membership_pricing_template".localizedFormat(value, period.lowercased().localizedFormat(duratoin))
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
           let duration = excessUsageAfterDuration,
           let period = excessUsageAfterPeriod {
            // Display only if we dont have a base fare
            if price == nil {
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
        guard let amount = parkingPriceAmount,
            amount > 0,
            let p = amount.price(for: currency) else { return nil }
        return p
    }
    
    var surchargePrice: String? {
        guard let fee = excessUsageFees,
            fee > 0,
            let price = fee.price(for: currency),
            let duration = excessUsageDuration,
            let period = excessUsagePeriod else { return nil }
        return "membership_pricing_template".localizedFormat(price, period.lowercased().localizedFormat(duration))
    }
    
    var preauthPrice: String? {
        guard let preauth = enablePreauth,
              preauth,
              let amount = preauthAmount,
              amount > 0 else { return nil }
        return amount.price(for: currency)
    }
    
    var surchargeDescription: String? {
        guard let excessAfterPeriod = excessUsageAfterPeriod,
            let excessAfterDuration = excessUsageAfterDuration else { return nil }
        return "surcharge_description".localizedFormat(excessAfterPeriod.lowercased().localizedFormat(excessAfterDuration))
    }
    
    var reservationRemaining: String? {
        guard let reservation = reservation else { return nil }
        guard let reservationStart = reservation.reservationStart else { return nil }
        let calendar = NSCalendar(calendarIdentifier: .gregorian)
        let formatter = DateFormatter()
//        formatter.dateFormat = "yyyy-dd-MM'T'HH:mm:ss.SSS'Z'"
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        if let startDate = formatter.date(from: reservationStart) {
            let components = calendar?.components(
                [.day, .hour, .minute], from: Date(), to: startDate, options: .matchFirst)
            let day = components!.day
            let hour = components!.hour
            let mins = components!.minute
            if day != nil && day! <= 0 {
                return "\(day!)day, \(hour!)h and \(mins!)min"
            }
            if hour != nil && hour! <= 0 {
                return "\(hour!)h and \(mins!)min"
            }
            if mins != nil && mins! <= 0 {
                return "\(hour!)h and \(mins!)min"
            }
            return nil
        }
        return nil
    }
    
    var reservationDescription: String? {
        guard let time = reservationRemaining else { return nil }
        let text = "rental_time_limit_desc".localizedFormat(time)
        return text
    }
    
    var unlockPrice: String? {
        guard let fee = unlockFee, fee > 0 else { return nil }
        return fee.price(for: currency)
    }
    
    var coordinate: CLLocationCoordinate2D {
        guard let lat = latitude, let lon = longitude else { return kCLLocationCoordinate2DInvalid }
        return .init(latitude: lat, longitude: lon)
    }
    var shortEndRide: Bool { skipParkingImage ?? false }
    
    var kuhmuteUuid: String? {
        guard let _ = controllers?.first(where: {Thing.Vendor.compare(rawValue: $0.vendor, to: .Kuhmute)}) else { return nil }
        return bikeUuid
    }
    
    var adapterId: String? {
        guard let _ = controllers?.first(where: {Thing.DeviceType.compare(rawValue: $0.deviceType, to: .adapter)}) else { return nil }
        return bikeUuid
    }
    
    enum CodingKeys: String, CodingKey {
        case bikeId
        case macId
        case lockId
        case fleetKey
        case name = "bikeName"
        case bikeBatteryLevel
        case fleetName
        case fleetLogo
        case picture = "pic"
        case skipParkingImage
        case latitude
        case longitude
        case kind = "type"
        case priceAmount = "priceForMembership"
        case priceDuration = "priceTypeValue"
        case pricePeriod = "priceType"
        case currency
        case parkingPriceAmount = "priceForPenaltyOutsideParking"
        case fleetType
        case terms = "fleetTAndC"
        case fleetId
        case details = "description"
        case make
        case model
        case excessUsageFees
        case excessUsageDuration = "excessUsageTypeValue"
        case excessUsagePeriod = "excessUsageType"
        case excessUsageAfterPeriod = "excessUsageTypeAfterType"
        case excessUsageAfterDuration = "excessUsageTypeAfterValue"
        case unlockFee = "priceForBikeUnlock"
        case controllers
        case reservationSettings
        case reservation
        case requirePhoneNumber
        case qrCodeId
        case qrCode
        case contactEmail
        case customerName
        case bikeUuid
        case promotions
        case promotion
        case paymentGateway
        case pricingOptions
        case preauthAmount
        case enablePreauth
    }
    
    enum Kind: String, Codable {
        case regular
        case electric
        case kickScooter = "Kick Scooter"
        case locker
        case cart
        case kayak
        case moped
        
        static var random: Kind {
            let r = Int.random(in: 0...2)
            switch r {
            case 0:
                return regular
            case 1:
                return electric
            default:
                return kickScooter
            }
        }
    }
    
    enum Search {
        case nearest([Bike]) // Within 800 m
        case available([Bike]) // Within 5 km
        case busy
        case noService
    }
}

extension Bike {
    var fleet: Model.Fleet {
        .init(fleetId: fleetId, name: fleetName, email: contactEmail, customer: customerName, logo: fleetLogo, legal: terms, requirePhoneNumber: requirePhoneNumber, type: fleetType!, reservationSettings: reservationSettings, paymentSettings: .init(priceForPenaltyOutsideParking: parkingPriceAmount ?? 0, currency: currency ?? "USD"))
    }
}

extension Bike: Equatable {
    public static func == (lhs: Bike, rhs: Bike) -> Bool {
        return lhs.bikeId == rhs.bikeId
    }
}

extension Bike: MapPoint {
    public var batteryLevel: Int? {
        return bikeBatteryLevel
    }

    public var title: String? { name }
    
    public var subtitle: String? { fleetName }
    
    public var color: UIColor { .accent }
    
    public var bage: Int? { nil }
    
    public func isEqual(to: MapPoint) -> Bool {
        guard let bike = to as? Bike else { return false }
        return self == bike
    }
    
    public var identifier: String {
        switch kind {
        case .kickScooter:
            return "annotation_bike_kick_scooter"
        case .locker:
            return "annotation_locker"
        case .cart:
            return "annotation_cart"
        case .kayak:
            return "annotation_kayak"
        case .moped:
            return "annotation_moped"
        default:
            return "annotation_bike_regular"
        }
    }
    
    var localizedKindTitle: String {
        switch kind {
        case .kickScooter:
            return "kick_scooter".localized()
        case .moped:
            return "moped".localized()
        case .kayak:
            return kind.rawValue.localized()
        default:
            return (kind.rawValue + "_bike").localized()
        }
    }
}

extension Bike {
    struct Booking: Decodable {
        let supportPhone: String
        let onCallOperator: String?
        let bookedOn: Date
        let expiresIn: TimeInterval
        
        var deadline: Date { bookedOn.addingTimeInterval(expiresIn) }
    }
}

