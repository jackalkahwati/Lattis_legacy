//
//  Trip.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 22/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import CoreLocation
import Model

extension Model.Fleet.ChargeType {
    var isFree: Bool {
        return self == .publicFree || self == .privateFree
    }
    
    var isPayment: Bool {
        return self == .publicPay || self == .privatePay
    }
}

struct Trip: Codable {
    let tripId: Int
    let bikeId: Int?
    let portId: Int?
    let hubId: Int?
    let fleetId: Int
    let startedDate: Double
    let endedDate: Double?
    let disableTracking: Bool?
    let fare: Double?
    let totalRefunded: Double?
    let parkingFee: Double?
    let surchargeFee: Double?
    let penaltyFees: Double?
    let totalPrice: Double?
    let currency: String?
    let fleetType: Model.Fleet.ChargeType?
    let isStarted: Bool
    let logo: URL?
    let fleetName: String?
    let promoCodeDiscount: Double?
    let startAddress: String?
    let endAddress: String?
    let unlockFee: Double?
    let discount: Double?
    let promotion: Promotion?
    var reservationEnd: String?
    var reservationStart: String?
    // Hack to filter out incorrect data in steps
    fileprivate let failableSteps: [Step.Failable]?
    var steps: [Step] { return failableSteps?.compactMap(Step.init) ?? [] }
    
    var bike: Bike? = nil
    var taxes: [Tax]? = nil
}


extension Trip {

    var startedAt: Date {
        let date = Date(timeIntervalSince1970: startedDate)
        return date
    }
    
    var endedAt: Date? {
        if endedDate != nil {
            return Date(timeIntervalSince1970: endedDate!)
        }
        return nil
    }
    
    // In seconds
    var duration: TimeInterval {
        let start = Date(timeIntervalSince1970: startedDate)
        var end = Date()
        if endedDate != nil {
            end = Date(timeIntervalSince1970: endedDate!)
        }
        return end.timeIntervalSince(start)
    }
    
    func price(for type: Price) -> String? {
        let p: Double
        switch type {
        case .total where totalPrice == 0:
            return "bike_detail_bike_cost_free".localized()
        case .total where totalPrice != nil:
            p = totalPrice!
        case .duration where fare != nil:
            p = fare!
        case .penalty where penaltyFees != nil:
            p = penaltyFees!
        case .surcharge where surchargeFee != nil && surchargeFee! > 0:
            p = surchargeFee!
        case .parking where parkingFee != nil && parkingFee! > 0:
            p = parkingFee!
        case .unlock where unlockFee != nil && unlockFee! > 0:
            p = unlockFee!
        case .refund where totalRefunded != nil && totalRefunded! > 0:
            p = totalRefunded!
        default:
            return nil
        }
        return p.price(for: currency)
    }

    enum Price: String {
        case duration = "Trip fare"
        case parking = "Parking fee"
        case surcharge = "Surcharge"
        case total = "Total"
        case unlock = "Unlock fee"
        case penalty
        case refund = "Refund"
    }
    
    enum CodingKeys: String, CodingKey {
        case tripId
        case bikeId
        case fleetId
        case startedDate = "dateCreated"
        case endedDate = "dateEndtrip"
        case disableTracking = "doNotTrackTrip"
        case fare = "chargeForDuration"
        case parkingFee = "priceForPenaltyOutsideParking"
        case surchargeFee = "overUsageFees"
        case penaltyFees
        case totalPrice = "total"
        case currency
        case fleetType = "type"
        case isStarted = "firstLockConnect"
        case logo
        case fleetName
        case startAddress
        case endAddress
        case failableSteps = "steps"
        case unlockFee = "priceForBikeUnlock"
        case discount = "membershipDiscount"
        case reservationEnd = "reservationEnd"
        case reservationStart = "reservationStart"
        case promoCodeDiscount
        case promotion
        case hubId
        case portId
        case taxes
        case totalRefunded = "totalRefunded"
    }
    
    public struct End: Encodable {
        public let tripId: Int
        public let latitude: Double
        public let longitude: Double
        public let accuracy: Double
        public var parkingImage: URL?
        public var bikeDamaged: Bool?
        public var chargeId: String?
    }
    
    struct Start: Encodable {
        let bikeId: Int
        let latitude: Double
        let longitude: Double
        let deviceToken: String?
        
        internal init(bikeId: Int, latitude: Double, longitude: Double, deviceToken: String? = AppRouter.shared.notificationToken) {
            self.bikeId = bikeId
            self.latitude = latitude
            self.longitude = longitude
            self.deviceToken = deviceToken
        }
    }
    
    public enum Fail: Error {
        case noBike
        case noLocation
        case noTrip
    }
    
    func end(location: CLLocation?, parkingImage: URL? = nil, bikeDamaged: Bool? = nil, chargeId: String? = nil) throws -> End {
        let latitude: CLLocationDegrees
        let longitude: CLLocationDegrees
        let accuracy: CLLocationAccuracy
        if isStarted {
            if let loc = location {
                latitude = loc.coordinate.latitude
                longitude = loc.coordinate.longitude
                accuracy = loc.horizontalAccuracy
            } else {
                throw Fail.noLocation
            }
        } else {
            if let b = bike {
                if let lat = b.latitude, let lon = b.longitude {
                    latitude = lat
                    longitude = lon
                } else if let loc = location {
                    latitude = loc.coordinate.latitude
                    longitude = loc.coordinate.longitude
                } else {
                    throw Fail.noLocation
                }
                accuracy = 0
            } else {
                throw Fail.noBike
            }
        }
        return .init(tripId: tripId, latitude: latitude, longitude: longitude, accuracy: accuracy, parkingImage: parkingImage, bikeDamaged: bikeDamaged, chargeId: chargeId)
    }
    
    public struct Rating: Encodable {
        public let tripId: Int
        public let rating: Int
    }
    
    public func rate(_ rating: Int) -> Rating {
        return .init(tripId: tripId, rating: rating)
    }
    
    public func discountString(_ amount: Double?)-> String? {
        guard let d = amount,
              d > 0,
              let p = d.price(for: currency) else { return nil }
        return "-\(p)"
    }
    
    public struct Invoice: Decodable {
        public let tripId: Int
        public let bikeId: Int?
        public let portId: Int?
        public let hubId: Int?
        public let chargeForDuration: Double
        public let currency: String?
        public let doNotTrackTrip: Bool
        public var duration: TimeInterval
        public let endedTrip: Trip?
        public let bikeBatteryLevel: Double?
        
        public var price: String? {
            guard chargeForDuration > 0 else { return nil }
            return chargeForDuration.price(for: currency)
        }
    }
    
    public struct Step: Codable {
        public let coordinate: CLLocationCoordinate2D
        public let time: Date
        public let lockState: LockState
        
        init(_ coordinate: CLLocationCoordinate2D, lockState: LockState = .none) {
            self.coordinate = coordinate
            self.lockState = lockState
            self.time = Date()
        }
        
        fileprivate init?(_ step: Failable) {
            guard let lat = step.latitude,
                let lon = step.longitude,
                let time = step.time else {
                    return nil
            }
            coordinate = .init(lat, lon)
            self.time = time
            if let lock = step.lockState {
                lockState = .track(lock == 1)
            } else {
                lockState = .none
            }
        }
        
        public init(from decoder: Decoder) throws {
            var container = try decoder.unkeyedContainer()
            let lat = try container.decode(Double.self)
            let lng = try container.decode(Double.self)
            let time = try container.decode(TimeInterval.self)
            self.coordinate = CLLocationCoordinate2D(latitude: lat, longitude: lng)
            self.time = Date(timeIntervalSince1970: time)
            if let lock = try container.decodeIfPresent(Int.self) {
                self.lockState = .track(lock == 1)
            } else {
                self.lockState = .none
            }
        }
        
        public func encode(to encoder: Encoder) throws {
            var container = encoder.unkeyedContainer()
            try container.encode(coordinate.latitude)
            try container.encode(coordinate.longitude)
            try container.encode(time.timeIntervalSince1970)
            if case let .track(state) = lockState {
                let intValue: Int = state ? 1 : 0
                try container.encode(intValue)
            }
        }

        public enum LockState {
            case none
            case track(Bool)
        }
        
        fileprivate struct Failable: Codable {
            let latitude: Double?
            let longitude: Double?
            let time: Date?
            let lockState: Int?
            
            public init(from decoder: Decoder) throws {
                var container = try decoder.unkeyedContainer()
                latitude = try container.decodeIfPresent(Double.self)
                longitude = try container.decodeIfPresent(Double.self)
                time = try container.decodeIfPresent(Date.self)
//                self.time = Date(timeIntervalSince1970: time)
                lockState = try container.decodeIfPresent(Int.self)
            }
            
            public func encode(to encoder: Encoder) throws {
                var container = encoder.unkeyedContainer()
                if let lat = latitude {
                    try container.encode(lat)
                }
                if let lon = longitude {
                    try container.encode(lon)
                }
                if let time = self.time {
                    try container.encode(time)
                }
                if let lock = lockState {
                    try container.encode(lock)
                }
            }
        }
    }
    
    public struct Update: Encodable {
        public let tripId: Int
        public let steps: [Step]
    }
    
    public struct Tax: Codable {
        public let name: String
        public let taxId: Int
        public let amount: Double
        public let percentage: Double
    }
    
    func upload(_ steps: [Step]) -> Update {
        return .init(tripId: tripId, steps: steps)
    }
    
    var invoice: Invoice {
        if let bikeId = bikeId {
            return .init(tripId: tripId, bikeId: bikeId, portId: nil, hubId: nil, chargeForDuration: totalPrice ?? 0, currency: currency ?? "USD", doNotTrackTrip: disableTracking ?? false, duration: duration, endedTrip: nil, bikeBatteryLevel: 0)
        }
        return .init(tripId: tripId, bikeId: 0, portId: nil, hubId: nil, chargeForDuration: totalPrice ?? 0, currency: currency ?? "USD", doNotTrackTrip: disableTracking ?? false, duration: duration, endedTrip: nil, bikeBatteryLevel: 0)
    }
    
    public init(tripId: Int, bikeId: Int, fleetId: Int, startedDate: Double, endedDate: Double?, disableTracking: Bool?, fare: Double?, totalRefunded: Double, parkingFee: Double?, surchargeFee: Double?, penaltyFees: Double?, totalPrice: Double?, currency: String?, isStarted: Bool, logo: URL?, fleetName: String?, startAddress: String?, endAddress: String?, unlockFee: Double?, bike: Bike? = nil) {
        self.tripId = tripId
        self.bikeId = bikeId
        self.portId = nil
        self.hubId = nil
        self.fleetId = fleetId
        self.startedDate = startedDate
        self.endedDate = endedDate
        self.disableTracking = disableTracking
        self.fare = fare
        self.parkingFee = parkingFee
        self.surchargeFee = surchargeFee
        self.penaltyFees = penaltyFees
        self.totalPrice = totalPrice
        self.currency = currency
        self.fleetType = .publicFree
        self.isStarted = isStarted
        self.logo = logo
        self.fleetName = fleetName
        self.startAddress = startAddress
        self.endAddress = endAddress
        self.unlockFee = unlockFee
        self.failableSteps = []
        self.bike = bike
        self.discount = nil
        self.promoCodeDiscount = nil
        self.promotion = nil
        self.totalRefunded = totalRefunded
    }
}


extension Trip.Step: Equatable {
    static func == (lhs: Trip.Step, rhs: Trip.Step) -> Bool {
        return lhs.coordinate == rhs.coordinate && lhs.time == rhs.time && lhs.lockState == rhs.lockState
    }
}

extension Trip.Step.LockState: Equatable {
    
}

extension Trip {
    struct Details: Codable {
        let trip: Trip
        let hub: Hub?
//        let bike: Bike?
    }
}


extension Trip {
    static let stab = Trip(tripId: 0, bikeId: 0, portId: nil, hubId: nil, fleetId: 0, startedDate: Date().timeIntervalSince1970, endedDate: Date().addingTimeInterval(8700).timeIntervalSince1970, disableTracking: nil, fare: 20.8, totalRefunded: 0.0, parkingFee: 10.4, surchargeFee: 0, penaltyFees: 0, totalPrice: 31.2, currency: "USD", fleetType: .privateFree, isStarted: true, logo: nil, fleetName: nil, promoCodeDiscount: nil, startAddress: nil, endAddress: nil, unlockFee: 1, discount: nil, promotion: nil, failableSteps: [])
}
