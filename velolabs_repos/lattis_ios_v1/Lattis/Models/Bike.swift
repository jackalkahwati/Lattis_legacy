//
//  Bike.swift
//  Lattis
//
//  Created by Ravil Khusainov on 23/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import CoreLocation
import MapKit

public struct Bike: Codable {
    public let bikeId: Int
    public let fleetId: Int
    public let lockId: Int?
    public var coordinate: CLLocationCoordinate2D
    public let macId: String?
    public let operatorId: Int
    
    public var status: Status = .active
    public var currentStatus: CurrentStatus = .parked
    public var doNotTrackTrip: Bool = false
    public var requirePhoneNumber: Bool = false
    public var name: String?
    public var fleetName: String? = nil
    public var description: String? = nil
    public var make: String? = nil
    public var batteryLevel: Double? = nil
    public var bikeBatteryLevel: Double? = nil
    public var firmwareVersion: String? = nil
    public var model: String? = nil
    public var bikeType: BikeType = .regular
    public var key: String? = nil
    public var pic: URL? = nil
    public var fleetLogo: URL? = nil
    public var fleetKey: String? = nil
    public var network: PrivateNetwork? = nil
    public var termsLink: URL? = nil
    
    public var currency: String = "USD"
    public var priceForMembership: Double? = nil
    public var priceDuration: Int? = nil
    public var priceUnit: String? = nil
    public var depositPrice: Double? = nil
    public var excessUsageFees: Double? = nil
    public var excessUsageDuration: Int? = nil
    public var excessUsageUnit: String? = nil
    public var excessUsageAfterDuration: Int? = nil
    public var excessUsageAfterUnit: String? = nil
    public var refundCriteria: Int? = nil
    public var refundCriteriaUnit: String? = nil
    public var depositType: DepositType = .none
    public var fleetType: FleetType = .privateFree
    public var shortEndRide: Bool = true
    public var maxTripLength: Int? = nil
    public var iotModules: [IoTModule] = []
    
    public enum CodingKeys: String, CodingKey {
        case bikeId
        case macId
        case operatorId
        case fleetId
        case lockId
        case latitude
        case longitude
        case bikeName
        case description
        case model
        case make
        case type
        case key
        case batteryLevel
        case bikeBatteryLevel
        case pic
        case fleetName
        case fleetLogo
        case fleetKey
        case priceForMembership
        case priceTypeValue
        case priceType
        case priceValue
        case rideDeposit
        case priceForRideDeposit
        case priceForRideDepositType
        case usageSurcharge
        case excessUsageFees
        case excessUsageTypeValue
        case excessUsageType
        case excessUsageTypeAfterValue
        case excessUsageTypeAfterType
        case refundCriteria
        case refundCriteriaValue
        case fleetTAndC
        case fleetType
        case skipParkingImage
        case maxTripLength
        case status
        case currentStatus
        case currency
        case requirePhoneNumber
        case doNotTrackTrip
        case controllers
    }
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        bikeId = try container.decode(Int.self, forKey: .bikeId)
        lockId = try container.decodeIfPresent(Int.self, forKey: .lockId)
        macId = try container.decodeIfPresent(String.self, forKey: .macId)
        operatorId = try container.decode(Int.self, forKey: .operatorId)
        fleetId = try container.decode(Int.self, forKey: .fleetId)
        let lat = try container.decode(Double.self, forKey: .latitude)
        let lng = try container.decode(Double.self, forKey: .longitude)
        coordinate = CLLocationCoordinate2D(latitude: lat, longitude: lng)
        if let status = try container.decodeIfPresent(Status.self, forKey: .status) {
            self.status = status
        } else {
            self.status = .active
        }
        if let currentStatus = try container.decodeIfPresent(CurrentStatus.self, forKey: .currentStatus) {
            self.currentStatus = currentStatus
        } else {
            self.currentStatus = .parked
        }
        name = try container.decodeIfPresent(String.self, forKey: .bikeName)
        fleetName = try container.decodeIfPresent(String.self, forKey: .fleetName)
        description = try container.decodeIfPresent(String.self, forKey: .description)
        model = try container.decodeIfPresent(String.self, forKey: .model)
        make = try container.decodeIfPresent(String.self, forKey: .make)
        if let type = try container.decodeIfPresent(BikeType.self, forKey: .type) {
            self.bikeType = type
        } else {
            self.bikeType = .regular
        }
        key = try container.decodeIfPresent(String.self, forKey: .key)
        fleetKey = try container.decodeIfPresent(String.self, forKey: .fleetKey)
        if let level = try container.decodeIfPresent(Double.self, forKey: .batteryLevel) {
            self.batteryLevel = level/100
        }
        if let level = try container.decodeIfPresent(Double.self, forKey: .bikeBatteryLevel) {
            self.bikeBatteryLevel = level/100
        }
        pic = try container.decodeIfPresent(URL.self, forKey: .pic)
        fleetLogo = try container.decodeIfPresent(URL.self, forKey: .fleetLogo)
        priceForMembership = try container.decodeIfPresent(Double.self, forKey: .priceForMembership)
        priceDuration = try container.decodeIfPresent(Int.self, forKey: .priceTypeValue)
        priceUnit = try container.decodeIfPresent(String.self, forKey: .priceType)
        if let isDepo = try container.decodeIfPresent(String.self, forKey: .rideDeposit), isDepo == "Yes" {
            depositPrice = try container.decodeIfPresent(Double.self, forKey: .priceForRideDeposit)
            if let depositType = try container.decodeIfPresent(DepositType.self, forKey: .priceForRideDepositType) {
                self.depositType = depositType
            } else {
                self.depositType = .none
            }
        }
        if let isSur = try container.decodeIfPresent(String.self, forKey: .usageSurcharge), isSur == "Yes" {
            excessUsageFees = try container.decodeIfPresent(Double.self, forKey: .excessUsageFees)
        }
        excessUsageDuration = try container.decodeIfPresent(Int.self, forKey: .excessUsageTypeValue)
        excessUsageUnit = try container.decodeIfPresent(String.self, forKey: .excessUsageType)
        excessUsageAfterDuration = try container.decodeIfPresent(Int.self, forKey: .excessUsageTypeAfterValue)
        excessUsageAfterUnit = try container.decodeIfPresent(String.self, forKey: .excessUsageTypeAfterType)
        refundCriteria = try container.decodeIfPresent(Int.self, forKey: .refundCriteria)
        refundCriteriaUnit = try container.decodeIfPresent(String.self, forKey: .refundCriteriaValue)
        termsLink = try container.decodeIfPresent(URL.self, forKey: .fleetTAndC)
        if let type = try container.decodeIfPresent(FleetType.self, forKey: .fleetType) {
            self.fleetType = type
        } else {
            self.fleetType = .publicPay
        }
        maxTripLength = try container.decodeIfPresent(Int.self, forKey: .maxTripLength)
        if let skip = try container.decodeIfPresent(Bool.self, forKey: .skipParkingImage) {
            self.shortEndRide = skip
        } else {
            #if DEBUG
            self.shortEndRide = true
            #else
            self.shortEndRide = false
            #endif
        }
        if let cur = try container.decodeIfPresent(String.self, forKey: .currency) {
            self.currency = cur
        } else {
            self.currency = "USD"
        }
        if let phoneRequired = try container.decodeIfPresent(Bool.self, forKey: .requirePhoneNumber) {
            self.requirePhoneNumber = phoneRequired
        }
        if let notTrack = try container.decodeIfPresent(Bool.self, forKey: .doNotTrackTrip) {
            self.doNotTrackTrip = notTrack
        }
        if let modules = try container.decodeIfPresent([IoTModule].self, forKey: .controllers) {
            self.iotModules = modules
        }
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(bikeId, forKey: .bikeId)
        try container.encode(macId, forKey: .macId)
        try container.encode(operatorId, forKey: .operatorId)
        try container.encode(fleetId, forKey: .fleetId)
        try container.encode(lockId, forKey: .lockId)
        try container.encode(coordinate.latitude, forKey: .latitude)
        try container.encode(coordinate.longitude, forKey: .longitude)
        try container.encode(name, forKey: .bikeName)
        try container.encode(description, forKey: .description)
        try container.encode(model, forKey: .model)
        try container.encode(make, forKey: .make)
        try container.encode(bikeType, forKey: .type)
        try container.encode(key, forKey: .key)
        try container.encode(batteryLevel, forKey: .batteryLevel)
        try container.encode(bikeBatteryLevel, forKey: .bikeBatteryLevel)
        try container.encode(pic, forKey: .pic)
        try container.encode(fleetName, forKey: .fleetName)
        try container.encode(fleetLogo, forKey: .fleetLogo)
        try container.encode(fleetKey, forKey: .fleetKey)
        try container.encode(priceForMembership, forKey: .priceForMembership)
        try container.encode(status, forKey: .status)
        try container.encode(iotModules, forKey: .controllers)
    }
}

public extension Bike {
    enum BikeType: String, Codable {
        case regular
        case eBike = "electric"
        case kScooter = "Kick Scooter"
        public var image: UIImage? {
            switch self {
            case .eBike:
                return UIImage(named: "annotation_bike_electric")
            case .kScooter:
                return UIImage(named: "annotation_bike_kick_scooter")
            default:
                return UIImage(named: "annotation_bike_regular")
            }
        }
        public var name: String {
            switch self {
            case .eBike:
                return "bike_type_e_bike".localized()
            default:
                return "bike_type_regular".localized()
            }
        }
    }
    
    enum Status: String, Codable {
        case active, inactive, suspended, deleted, booked, onRide
    }
    
    enum CurrentStatus: String, Codable {
        case lockAssigned = "lock_assigned"
        case lockNotAssigned = "lock_not_assigned"
        case parked
        case onTrip = "on_trip"
        case damaged
        case stolen
        case underMaintenance = "under_maintenance"
        case totalLoss = "total_loss"
        case defleeted
    }
    
    enum DepositType: String, Codable {
        case none
        case oneTime = "OneTime"
        case perRide = "Per Ride"
    }
    
    enum FleetType: String, Codable {
        case privateFree = "private_no_payment"
        case publicPay = "public"
        case privatePay = "private"
        case publicFree = "public_no_payment"
        
        var isFree: Bool {
            return self == .publicFree || self == .privateFree
        }
    }
    
    init(_ bikeId: Int) {
        self.bikeId = bikeId
        self.lockId = 0
        self.fleetId = 0
        self.macId = ""
        self.operatorId = 0
        self.name = nil
        self.coordinate = kCLLocationCoordinate2DInvalid
    }
    
    init(bikeId: Int, coordinate: CLLocationCoordinate2D, name: String? = nil) {
        self.bikeId = bikeId
        self.name = name
        self.coordinate = coordinate
        self.fleetId = 0
        self.lockId = 0
        self.macId = ""
        self.operatorId = 0
    }
    
    enum Error: LocalizedError {
        case invalidCoordinates([Int])
        
        public var localizedDescription: String {
            return "\(self)"
        }
    }
}

extension Bike: AnnotationModel {
    var image: UIImage? {
        return bikeType.image
    }
}

extension Bike {
    public func distance(from: CLLocationCoordinate2D) -> Double {
        guard CLLocationCoordinate2DIsValid(coordinate) && CLLocationCoordinate2DIsValid(from) else { return 0 }
        let cur = MKMapPoint(coordinate)
        let dest = MKMapPoint(from)
        return cur.distance(to: dest)
    }
    
    public enum Search {
        case nearest([Bike]) // Within 800 m
        case available([Bike]) // Within 5 km
        case busy
        case noService
    }
    
    public var isFree: Bool {
        return fleetType == .publicFree || fleetType == .privateFree
    }
    
    var axaLock: IoTModule? {
        iotModules.first(where: {$0.vendor == .AXA})
    }
}
