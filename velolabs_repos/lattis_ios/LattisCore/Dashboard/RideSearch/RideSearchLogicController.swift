//
//  RideSearchLogicController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 29.04.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import Model
import Wrappers
import CoreLocation

class RideSearchLogicController {
    
    var selected: Bike? {
        didSet {
            guard selected == nil else { return }
            payPerUse = false
            pricing = nil
        }
    }
    var address: Address?
    var pricing: Pricing? {
        didSet {
            if pricing != nil {
                payPerUse = false
            }
        }
    }
    var payPerUse: Bool = false
    
    var hasPhonePumber: Bool {
        guard let phone = user?.phoneNumber else { return false }
        return !phone.isEmpty
    }
    
    var explicitConsent: Bool = false
    fileprivate(set) var subscriptions: [Subscription] = []
    fileprivate let api: BikeAPI & SubscriptionsAPI & HubsAPI & TripAPI = AppRouter.shared.api()
    fileprivate let cardsStorage = CardStorage()
    fileprivate var currentCard: Payment.Card?
    fileprivate let storage = UserStorage()
    fileprivate var user: User?
    fileprivate var dockedBikes: [Int: [Bike]] = [:]
    fileprivate var rentals: Rentals?
    @UserDefaultsBacked(key: "tripIdToShowSummary")
    fileprivate var tripIdToShowSummary: Int?
    fileprivate var fetchTask: URLSessionTask?
    
    init() {
        hanleCreditCard()
        fetchSubscriptions()
        NotificationCenter.default.addObserver(self, selector: #selector(fetchSubscriptions), name: .subscriptionsUpdated, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(hanleCreditCard), name: .creditCardAdded, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(hanleCreditCard), name: .creditCardUpdated, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(hanleCreditCard), name: .creditCardRemoved, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc
    fileprivate func hanleCreditCard() {
        cardsStorage.fetch { [unowned self] (cards) in
            self.currentCard = cards.first(where: {Payment.card($0).isCurrent})
        }
    }
    
    var consentText: [String] {
        guard !explicitConsent else { return [] }
        return UITheme.theme.strictTNC
    }
    
    var canRent: Bool {
        guard let bike = selected else { return false }
        if bike.noCardNeeded { return true }
        if let card = currentCard {
            return card.gateway == bike.paymentGateway
        }
        return false
    }
    
    var shouldSelectPricing: Bool {
        selected?.pricingOptions != nil && pricing == nil && !payPerUse
    }
    
    func fetchUser(completion: @escaping (User?) -> ()) {
        completion(nil)
        storage.current(needRefresh: true, completion: { [unowned self] user in
            self.user = user
            completion(user)
        })
    }
    
    func fetchRentals(for region: MapRegion, completion: @escaping ([MapPoint]) -> Void) {
        fetchTask?.cancel()
        fetchTask = api.fetchRentals(in: region) { [weak self] (result) in
            switch result {
            case .success(let rentals):
                self?.rentals = rentals
                guard let r = self?.map(rentals) else { return }
                completion(r)
            case .failure(let error):
                if error.asNSError.code == -999 {
                    return // Error is cancel
                }
                Analytics.report(error)
            }
        }
    }
    
    func checkForSummary(completion: @escaping (Trip) -> Void) {
        guard let tripId = tripIdToShowSummary else {return}
        api.getTrip(by: tripId) { [weak self] (result) in
            switch result.unwrap(\.trip) {
            case .failure(let error):
                Analytics.report(error)
            case .success(let trip):
                self?.tripIdToShowSummary = nil
                completion(trip)
            }
        }
    }
    
    func bikes(for hub: Hub) -> [Bike] {
        dockedBikes[hub.hubId] ?? []
    }
    
    fileprivate func map(_ rentals: Rentals) -> [MapPoint] {
        dockedBikes.removeAll()
        var result: [MapPoint] = rentals.hubs
        for hub in rentals.hubs {
            var hb: [Bike] = []
            let bikes = hub.bikes ?? []
            for b in bikes {
                guard let bike = rentals.bikes.first(where: {$0.bikeId == b.bikeId}) else { continue }
                hb.append(bike)
            }
            dockedBikes[hub.hubId] = hb
        }
        result += rentals.bikes.filter({ (bike) -> Bool in
            !rentals.hubs.compactMap(\.bikes).flatMap({$0}).contains(where: {$0.bikeId == bike.bikeId}) &&
                bike.coordinate != kCLLocationCoordinate2DInvalid
        })
        return result
    }
    
    func book(the bike: Bike, completion: @escaping (Result<Bike.Booking, Error>) -> ()) {
        api.book(bike: bike, pricingId: pricing?.pricingOptionId) { [weak self] (result) in
            switch result {
            case .success(let booking):
                self?.selected = nil
                completion(.success(booking))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    func perk(for fleetId: Int) -> Double? {
        subscriptions.first(where: {$0.membership.fleet.fleetId == fleetId})?.membership.incentive
    }
    
    @objc
    fileprivate func fetchSubscriptions() {
        api.fetchSubscriptions { [weak self] (result) in
            switch result {
            case .failure(let error):
                Analytics.report(error)
            case .success(let subs):
                self?.subscriptions = subs
            }
        }
    }
    
    fileprivate func hub(bike: Bike) -> Hub.Bike? {
        guard let ren = rentals, let b = ren.hubs.compactMap(\.bikes).flatMap({$0}).first(where: {$0.bikeId == bike.bikeId}) else { return nil }
        return b
    }
}
