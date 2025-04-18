//
//  AppRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 13/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Oval
import LattisSDK
import Reachability

fileprivate let tripIdKey = "activeTripId"

final class AppRouter {
    let bikeStorage: BikeStorage = BikeFileStorage()
    weak var map: MapViewController?
    weak var endDelegate: EndRideInteractorDelegate?
    weak var menuRouter: MenuRouter?
    var isTripStarted: Bool = false {
        didSet {
            if isTripStarted == false {
                tripEnded?()
            }
        }
    }
    // FIXME: Use deeplinking instead of closures
    var tripEnded: (() -> ())?
    var cancelBooking: ((Bool) -> ())?
    var postCancelBooking: (() -> ())?
    var currentState: Status = .none
    var onStart: (Bool) -> () = {_ in} {
        didSet {
            if case .none = currentState {} else {
                var shouldSearch = false
                if case .find = currentState { shouldSearch = true }
                onStart(shouldSearch)
            }
        }
    }
    var endTrip: (Bool) -> () = {_ in} {
        didSet {
            cancelBooking = nil
        }
    }
    var onInternetAlert: (LockButton) -> () = {_ in} {
        didSet {
            if let alert = connectionAlert {
                self.onInternetAlert(alert.lockButton)
            }
        }
    }
    var damage: Bool = false
    var privateNetwork: () -> () = {}
    var addPhoneNumber: () -> () = {}
    static let shared = AppRouter()
    fileprivate let network: BikeNetwork & UserNetwork & CardsNetwork & TripNetwork = Session.shared
    fileprivate let storage: UserStorage & CreditCardStorage & TripsStorage = CoreDataStack.shared
    fileprivate let reachability = try? Reachability()
    fileprivate weak var connectionAlert: StaticAlertView?
    fileprivate var needSearch = false
    
    init() {
        NotificationCenter.default.addObserver(self, selector: #selector(appDidEnterForeground), name: UIApplication.didBecomeActiveNotification, object: nil)
    }
    
    func handleInternetConnection() {
        reachability?.stopNotifier()
        reachability?.whenReachable = { _ in
            self.connectionAlert?.hide()
            if self.needSearch {
                self.needSearch = false
                self.onStart(true)
            }
        }
        reachability?.whenUnreachable = showNoInternet(reach: )
        try? reachability?.startNotifier()
    }
    
    var isConnected: Bool {
        return reachability?.connection != .unavailable
    }
    
    func checkConnection() -> Bool {
        if isConnected == false {
            needSearch = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.4, execute: {
                guard let reachability = self.reachability else { return }
                self.showNoInternet(reach: reachability)
            })
        }
        return isConnected
    }
    
    fileprivate func showNoInternet(reach: Reachability) {
        guard connectionAlert == nil else { return }
        let title = isTripStarted ? "active_ride_no_internet_text" : "general_no_internet_text"
        let alert = StaticAlertView.alert(with: title.localized())
        alert.show()
        self.connectionAlert = alert
        self.onInternetAlert(alert.lockButton)
    }
    
    static func setup(with map: MapViewController) {
        shared.map = map
    }
    
    func logout(with map: MapViewController) {
        self.map = map
    }
    
    func endTripOrCancelBooking(theft: Bool) {
        if isTripStarted {
            currentState = .none
            endTrip(theft)
        } else {
            cancelBooking?(true)
        }
    }
    
    fileprivate let fleetSupportPhoneKey = "fleetSupportPhoneKey"
    var supportPhone: String {
        return fleetSupportPhone ?? defaultSupportPhone!
    }
    
    func getSupportPhone(completion: @escaping (String) -> ()) {
        completion(supportPhone)
        network.getStatus { (result) in
            switch result {
            case .success(let status):
                self.fleetSupportPhone = status.operatorPhone
                self.defaultSupportPhone = status.supportPhone
                completion(self.supportPhone)
            case .failure(let error):
                print(error)
            }
        }
    }
    
    var fleetSupportPhone: String? {
        set {
            UserDefaults.standard.setValue(newValue, forKey: fleetSupportPhoneKey)
            UserDefaults.standard.synchronize()
        }
        get {
            return UserDefaults.standard.string(forKey: fleetSupportPhoneKey)
        }
    }
    
    fileprivate let defaultSupportPhoneKey = "defaultSupportPhoneKey"
    var defaultSupportPhone: String? {
        set {
            UserDefaults.standard.setValue(newValue, forKey: defaultSupportPhoneKey)
            UserDefaults.standard.synchronize()
        }
        get {
            return UserDefaults.standard.string(forKey: defaultSupportPhoneKey) ?? "415-503-9744"
        }
    }
    
    func checkCurrentStatus() {
        guard reachability?.connection != .unavailable else {
            return checkOfflineRide()
        }
        network.getStatus { (result) in
            switch result {
            case .success(let status):
                self.update(status: status)
            case .failure(let error):
                self.onStart(true)
            }
        }
        refreshUser()
    }
    
    func backToSearch() {
        guard let map = map else { return }
        map.followUser = false
        FindRideRouter.push(in: map, replace: true)
    }
    
    func addPrivateNetwork() {
        privateNetwork()
    }
    
    func refreshCards(completion: @escaping ([CreditCard]) -> () = {_ in}) {
        completion(storage.cards)
        network.getCards { (result) in
            switch result {
            case .success(let cards):
                self.storage.update(cards: cards)
                completion(cards)
            case .failure(let error):
                self.handle(error: error)
            }
        }
    }
    
    func didEndRide() {
        endDelegate?.didEndTrip(with: nil)
        tripId = nil
    }
    
    func save(trip: Trip, bike: Bike) {
        isTripStarted = true
        tripId = trip.tripId
        bikeStorage.save(bike)
    }
    
    func home() {
        menuRouter?.openHome()
    }
    
    func searchBike() {
        menuRouter?.openHome()
        onStart(true)
    }
    
    func needQRTip() -> Bool {
        let identifier = "needQRTip"
        var count = UserDefaults.standard.integer(forKey: identifier)
        count += 1
        if count <= 5 {
            UserDefaults.standard.set(count, forKey: identifier)
            UserDefaults.standard.synchronize()
        }
        return count <= 5
    }
    
    var tripId: Int? {
        get {
            let val = UserDefaults.standard.integer(forKey: tripIdKey)
            guard val > 0 else { return nil }
            return val
        }
        set {
            if let val = newValue {
                UserDefaults.standard.set(val, forKey: tripIdKey)
            } else {
                UserDefaults.standard.removeObject(forKey: tripIdKey)
                bikeStorage.deleteAll()
                
            }
            UserDefaults.standard.synchronize()
        }
    }
    
    @objc fileprivate func appDidEnterForeground() {
        if isConnected {
            connectionAlert?.hide()
        }
    }
}

private extension AppRouter {
    func checkOfflineRide() {
        guard let tripId = tripId,
            let trip = storage.trip(by: tripId) else { return }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) {
            self.restore(trip: trip)
        }
    }
    
    func handle(error: Error) {
        print(error)
        if let err = error as? SessionError, case .invalidToken = err.code {
            AppDelegate.shared.logout()
        }
    }
    
    func update(status: Status.Info) {
        currentState = status.status
        var shouldSearch = false
        switch currentState {
        case let .booking(bikeId, from, till):
            network.getBike(by: bikeId, qrCode: nil) { (result) in
                switch result {
                case .success(let bike):
                    self.restoreBooking(bike, from: from, till: till)
                case .failure(let error):
                    self.handle(error: error)
                }
            }
        case .trip(let tripId):
            network.getTrip(by: tripId) { (result) in
                switch result {
                case .success(let trip):
                    self.restore(trip: trip)
                case .failure(let error):
                    self.handle(error: error)
                }
            }
        default:
            currentState = .find
            shouldSearch = true
        }
        onStart(shouldSearch)
        fleetSupportPhone = status.operatorPhone
        defaultSupportPhone = status.supportPhone
    }
    
    func restore(trip: Trip) {
        guard let map = map else { return }
        let completion: (Bike) -> () = { bike in
            if trip.isStarted {
                RideRouter.push(in: map, replace: true) {
                    $0.tripService = TripService(trip, bike: bike)
                    $0.lock = Lock(bike: bike)
                }
            } else {
                RouteToBikeRouter.push(in: map) { $0.restore(with: bike, trip: trip)}
            }
        }
        if reachability?.connection == .unavailable {
            guard let bike = bikeStorage.bike(by: trip.bikeId) else { return }
            completion(bike)
        } else {
            network.getBike(by: trip.bikeId, qrCode: nil) { (result) in
                switch result {
                case .success(let bike):
                    completion(bike)
                case .failure(let error):
                    self.handle(error: error)
                }
            }
        }
    }
    
    func restoreBooking(_ bike: Bike, from: TimeInterval, till: TimeInterval) {
        guard let map = map else { return }
        let now = Date().timeIntervalSince1970
        let limit = till - from
        let left = till - now
        RouteToBikeRouter.push(in: map) { $0.restore(with: bike, limit: limit, left: left)}
    }
    
    func refreshUser() {
        network.getUser { (result) in
            switch result {
            case .success(let user):
                self.storage.save(user)
            case .failure(let error):
                self.handle(error: error)
            }
        }
        refreshCards()
    }
}
