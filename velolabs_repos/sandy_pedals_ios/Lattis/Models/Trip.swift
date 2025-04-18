//
//  Trip.swift
//  Lattis
//
//  Created by Ravil Khusainov on 15/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import CoreLocation

public struct Trip: Codable {
    public var tripId: Int
    public var bikeId: Int
    public var steps: [Step]
    public var startedAt: Date?
    public var finishedAt: Date?
    public var fleetName: String? = nil
    public var fleetType: Bike.FleetType
    public var price: Double? = nil
    public var deposit: Double? = nil
    public var penaltyFees: Double? = nil
    public var excessUsageFees: Double? = nil
    public var total: Double? = nil
    internal var serverDuration: TimeInterval? = nil
    public var distance: Double? = nil
    public var refundCriteria: Int? = nil
    public var refundCriteriaUnit: String? = nil
    public var currency: String
    public var cardSystemId: String? = nil
    public var startAddress: String? = nil
    public var endAddress: String? = nil
    
    public var canSaveSteps: Bool = true
    public var location: CLLocationCoordinate2D = kCLLocationCoordinate2DInvalid
    public var isCanceled: Bool = false
    public var isStarted: Bool = false
    var card: CreditCard? = nil
    
    public init(_ tripId: Int = 0,
                bikeId: Int = 0,
                steps: [Step] = [],
                startedAt: Date? = nil,
                finishedAt: Date? = nil,
                fleetType: Bike.FleetType = .privateFree,
                currency: String = "USD") {
        self.tripId = tripId
        self.bikeId = bikeId
        self.steps = steps
        self.startedAt = startedAt
        self.finishedAt = finishedAt
        self.currency = currency
        self.fleetType = fleetType
    }
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.tripId = try container.decode(Int.self, forKey: .tripId)
        self.bikeId = try container.decode(Int.self, forKey: .bikeId)
        let steps = try container.decode([WeekStep].self, forKey: .steps)
        self.steps = steps.compactMap(Step.init)
        self.serverDuration = try container.decodeIfPresent(Double.self, forKey: .duration)
        self.startedAt = try container.decodeIfPresent(Date.self, forKey: .dateCreated)
        self.finishedAt = try container.decodeIfPresent(Date.self, forKey: .dateEndtrip)
        self.fleetName = try container.decodeIfPresent(String.self, forKey: .fleetName)
        self.total = try container.decodeIfPresent(Double.self, forKey: .total)
        self.distance = try container.decodeIfPresent(Double.self, forKey: .distance)
        self.price = try container.decodeIfPresent(Double.self, forKey: .priceForMembership)
        self.deposit = try container.decodeIfPresent(Double.self, forKey: .priceForRideDeposit)
        self.penaltyFees = try container.decodeIfPresent(Double.self, forKey: .penaltyFees)
        self.excessUsageFees = try container.decodeIfPresent(Double.self, forKey: .excessUsageFees)
        self.total = try container.decodeIfPresent(Double.self, forKey: .total)
        self.serverDuration = try container.decodeIfPresent(Double.self, forKey: .duration)
        self.distance = try container.decodeIfPresent(Double.self, forKey: .distance)
        self.refundCriteria = try container.decodeIfPresent(Int.self, forKey: .refundCriteria)
        self.refundCriteriaUnit = try container.decodeIfPresent(String.self, forKey: .refundCriteriaValue)
        self.endAddress = try container.decodeIfPresent(String.self, forKey: .endAddress)
        self.startAddress = try container.decodeIfPresent(String.self, forKey: .startAddress)
        let currency = try container.decodeIfPresent(String.self, forKey: .currency)
        self.currency = currency ?? "USD"
        self.cardSystemId = try container.decodeIfPresent(String.self, forKey: .cardId)
        let isStarted = try container.decodeIfPresent(Bool.self, forKey: .firstLockConnect)
        self.isStarted = isStarted ?? false
        self.fleetType = try container.decodeIfPresent(Bike.FleetType.self, forKey: .type) ?? .privateFree
        if let number = try container.decodeIfPresent(String.self, forKey: .ccNo) {
            let type = try container.decodeIfPresent(String.self, forKey: .ccType)
            self.card = CreditCard(number: number, typeString: type)
        }
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(tripId, forKey: .tripId)
        try container.encode(steps, forKey: .steps)
    }
    
    public enum CodingKeys: String, CodingKey {
        case tripId
        case bikeId
        case steps
        case duration
        case dateCreated
        case dateEndtrip
        case fleetName
        case total
        case distance
        case priceForMembership
        case priceForRideDeposit
        case penaltyFees
        case excessUsageFees
        case startAddress
        case endAddress
        case cardId
        case type
        case refundCriteria
        case refundCriteriaValue
        case ccNo
        case ccType
        case firstLockConnect
        case currency
    }
}

extension Trip {
    struct WeekStep: Decodable {
        let location: CLLocationCoordinate2D?
        let time: Date?
        
        public init(from decoder: Decoder) throws {
            var container = try decoder.unkeyedContainer()
            guard let lat = try container.decodeIfPresent(Double.self),
            let lng = try container.decodeIfPresent(Double.self),
            let time = try container.decodeIfPresent(Double.self)
                else {
                    self.location = nil
                    self.time = nil
                    return
            }
            self.location = CLLocationCoordinate2D(latitude: lat, longitude: lng)
            self.time = Date(timeIntervalSince1970: time)
        }
    }
    
    public struct Step: Codable {
        public let location: CLLocationCoordinate2D
        public let time: Date
        public let lockState: LockState
        
        public init(from decoder: Decoder) throws {
            var container = try decoder.unkeyedContainer()
            let lat = try container.decode(Double.self)
            let lng = try container.decode(Double.self)
            let time = try container.decode(Double.self)
            self.location = CLLocationCoordinate2D(latitude: lat, longitude: lng)
            self.time = Date(timeIntervalSince1970: time)
            self.lockState = .none
        }
        
        public func encode(to encoder: Encoder) throws {
            var container = encoder.unkeyedContainer()
            try container.encode(location.latitude)
            try container.encode(location.longitude)
            try container.encode(time.timeIntervalSince1970)
            if case let .track(state) = lockState {
                let intValue: Int = state ? 1 : 0
                try container.encode(intValue)
            }
        }
        
        init?(_ week: WeekStep) {
            guard let l = week.location, let t = week.time else {
                return nil
            }
            self.location = l
            self.time = t
            self.lockState = .none
        }
    }
    
    public struct Start: Encodable {
        public let bikeId: Int
        public let latitude: Double
        public let longitude: Double
    }
    
    public struct End: Encodable {
        public let tripId: Int
        public let latitude: Double
        public let longitude: Double
        public let accuracy: Double
        public var parkingImage: URL?
        public var bikeDamaged: Bool?
    }
    
    public struct Update: Decodable {
        public let duration: TimeInterval
        public let price: Double?
        public var currency: String
        public var trip: Trip?
        public var doNotTrackTrip: Bool
        
        init() {
            duration = 0
            price = 0
            currency = "USD"
            doNotTrackTrip = false
        }
        
        init?(_ trip: Trip) {
            guard trip.fleetType == .publicPay || trip.fleetType == .privatePay else { return nil }
            self.duration = abs(trip.startedAt?.timeIntervalSinceNow ?? trip.duration)
            self.price = trip.total ?? 0
            self.trip = trip
            self.currency = trip.currency
            self.doNotTrackTrip = !trip.canSaveSteps
        }
        
        public init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            duration = try container.decode(Double.self, forKey: .duration)
            price = try container.decodeIfPresent(Double.self, forKey: .price)
            doNotTrackTrip = try container.decode(Bool.self, forKey: .doNotTrackTrip)
            let curr = try container.decodeIfPresent(String.self, forKey: .currency)
            self.currency = curr ?? "USD"
        }
        
        enum CodingKeys: String, CodingKey {
            case duration
            case price = "chargeForDuration"
            case currency
            case doNotTrackTrip
        }
    }
    
    var start: Start {
        return .init(bikeId: bikeId, latitude: location.latitude, longitude: location.longitude)
    }
    
    func end(image: URL?, damage: Bool, location: CLLocation) -> End {
        let dmg: Bool? = damage ? damage : nil
        return .init(tripId: tripId, latitude: location.coordinate.latitude, longitude: location.coordinate.longitude, accuracy: location.horizontalAccuracy, parkingImage: image, bikeDamaged: dmg)
    }
    
    public var duration: TimeInterval {
        if let dur = serverDuration {
            return dur
        }
        guard let start = startedAt, let end = finishedAt else { return 0 }
        return end.timeIntervalSince(start)
    }
}

public extension Trip.Step {
    init(_ location: CLLocationCoordinate2D, state: LockState) {
        self.location = location
        self.time = Date()
        self.lockState = state
    }
    
    enum LockState {
        case none
        case track(Bool)
    }
}


