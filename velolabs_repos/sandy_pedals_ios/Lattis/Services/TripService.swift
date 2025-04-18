//
//  TripService.swift
//  Lattis
//
//  Created by Ravil Khusainov on 15/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import CoreLocation
import SwiftyTimer
import Oval
import FirebaseCrashlytics
import KeychainSwift

final class TripService: NSObject {
    var isQrBike: Bool = false
    var canSaveSteps: Bool = false
    var bike: Bike
    var isLocked: Bool = false {
        didSet {
            if isLocked != oldValue {
                lockState = .track(isLocked)
                saveStep()
            }
        }
    }
    var isParkingsCheck: Bool = false
    var location = CLLocation(latitude: -180, longitude: -180) {
        didSet {
            guard CLLocationCoordinate2DIsValid(location.coordinate) else { return }
            trip.location = location.coordinate
        }
    }
    fileprivate var lockState: Trip.Step.LockState = .none
    fileprivate var checkedLocation = CLLocation(latitude: -180, longitude: -180)
    fileprivate var prevLocation = kCLLocationCoordinate2DInvalid
    fileprivate(set) var trip: Trip
    fileprivate var updateTimer: Timer?
    fileprivate let storage: TripsStorage
    typealias TripServiceNetwork = TripNetwork & ParkingNetwork & UserNetwork
    fileprivate let network: TripServiceNetwork
    fileprivate let locationManager: CLLocationManager = {
        let manager = CLLocationManager()
        manager.desiredAccuracy = kCLLocationAccuracyBestForNavigation
        manager.activityType = .otherNavigation
        manager.distanceFilter = 5
        manager.allowsBackgroundLocationUpdates = true
        return manager
    }()

    init(_ bike: Bike, storage: TripsStorage = CoreDataStack.shared, network: TripServiceNetwork = Session.shared) {
        self.bike = bike
        self.trip = Trip(bikeId: bike.bikeId)
        self.storage = storage
        self.network = network
        super.init()
        configure()
    }
    
    init(_ trip: Trip, bike: Bike, storage: TripsStorage = CoreDataStack.shared, network: TripServiceNetwork = Session.shared) {
        self.trip = trip
        self.bike = bike
        self.storage = storage
        self.network = network
        super.init()
        configure()
        didStart(trip: trip.tripId)
    }
    
    deinit {
        updateTimer?.invalidate()
        NotificationCenter.default.removeObserver(self)
    }
    
    var onStart: () -> () = {}
    var onEnd: () -> () = {}
    var onFail: (Error) -> () = {_ in}
    var onParkingCheck: (Parking.Check) -> () = {_ in}
    var onTripUpdate: (Trip.Update) -> () = {_ in}
    
    func keepLocation() {
        location = CLLocation(latitude: bike.coordinate.latitude, longitude: bike.coordinate.longitude)
        checkedLocation = location
    }
    
    func start(with location: CLLocationCoordinate2D? = nil) {
        guard let loc = location else {
            return checkStatus()
        }
        self.location = CLLocation(latitude: loc.latitude, longitude: loc.longitude)
        saveStep()
        network.start(trip: trip) { [weak self] (result) in
            switch result {
            case .success(let tripId, let doNotTrack):
                self?.trip.canSaveSteps = !doNotTrack
                self?.didStart(trip: tripId)
            case .failure(let error):
                self?.onFail(error)
            }
        }
    }
    
    func end(with image: UIImage?) {
        guard CLLocationCoordinate2DIsValid(trip.location) else { return onFail(TripServiceError.locationDisabled) }
        updateTimer?.invalidate()
        var data: Data? = nil
        if let image = image {
            let reduced = image.resize(to: CGSize(width: 1024, height: 1024))
            data = reduced.jpegData(compressionQuality: 0.5)
        }
        sendSteps()
        let time = Date()
        let loc: CLLocation
        if CLLocationCoordinate2DIsValid(checkedLocation.coordinate) {
            trip.location = checkedLocation.coordinate
            loc = checkedLocation
        } else {
            loc = CLLocation(latitude: trip.location.latitude, longitude: trip.location.longitude)
        }
        network.end(trip: trip, location: loc, with: data, damage: AppRouter.shared.damage) { [weak self] (result) in
            switch result {
            case .success(let trip):
                self?.trip = trip
                self?.trip.finishedAt = Date()
                self?.storage.save(self!.trip)
                self?.locationManager.stopUpdatingLocation()
                self?.onEnd()
                if let id = self?.bike.macId {
                    KeychainSwift.clean(for: id)
                }
                let diff = Date().timeIntervalSince(time)
            case .failure(let error):
                self?.onFail(error)
                let diff = Date().timeIntervalSince(time)
            }
        }
        AppRouter.shared.damage = false
    }
    
    func checkParking() {
        let location = self.location
        network.checkParking(on: location, fleet: bike.fleetId) { [weak self] (result) in
            switch result {
            case .success(let check):
                self?.checkedLocation = location
                self?.onParkingCheck(check)
            case .failure(let error):
                self?.onFail(error)
            }
        }
    }
    
    func rateTrip(with rating: Int?) {
        guard let rating = rating else { return }
        network.rate(trip: trip, with: rating, completion: {_ in})
        
    }
}


enum TripServiceError: Error {
    case locationDisabled
}

private extension TripService {
    func configure() {
        locationManager.delegate = self
        NotificationCenter.default.addObserver(self, selector: #selector(didBecomeActive), name: UIApplication.didBecomeActiveNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(didEnterBackground), name: UIApplication.willResignActiveNotification, object: nil)
    }
    
    @objc func didEnterBackground() {
        guard trip.canSaveSteps else { return }
        locationManager.startUpdatingLocation()
    }
    
    @objc func didBecomeActive() {
        locationManager.stopUpdatingLocation()
    }
    
    func saveStep() {
        var allowed = canSaveSteps
        if case .track = lockState, trip.canSaveSteps {
            allowed = true
        } else if allowed && prevLocation == location.coordinate {
            allowed = false
        }
        guard allowed && trip.canSaveSteps else { return }
        guard CLLocationCoordinate2DIsValid(location.coordinate) else { return }
//        guard prevLocation.isWithin(2, of: location) == false
//            && prevLocation.isOutOf(1000, from: location) == false else { return }
        prevLocation = location.coordinate
        trip.steps.append(Trip.Step(location.coordinate, state: lockState))
        lockState = .none
        storage.save(trip)
//        location = kCLLocationCoordinate2DInvalid
    }
    
    func didStart(trip tripId: Int){
        AppRouter.shared.currentState = .trip(tripId)
        trip.tripId = tripId
        trip.steps.removeAll()
        if trip.startedAt == nil {
            trip.startedAt = Date()
        }
        updateTimer = Timer.every(10.second) { [weak self] in
            if let ss = self {
                ss.sendSteps()
            }
        }
        trip.isStarted = true
        storage.save(trip)
        onStart()
    }
    
    func sendSteps() {
//        if bike.doNotTrackTrip, case .none = lockState {
//            guard bike.isFree == false else { return }
//            network.getTrip(by: trip.tripId, success: { [weak self] (trip) in
//                guard var update = Trip.Update(trip) else { return }
//                update.trip = nil
//                self?.onTripUpdate(update)
//            }, fail: { [weak self] error in
//                self?.onFail(error)
//            })
//            return
//        }
        saveStep()
//        guard trip.steps.isEmpty == false else { return }
        let buffer = trip.steps
        network.update(trip: trip) { [weak self] (result) in
            switch result {
            case .success(let update):
                guard let t = self?.bike, t.fleetType == .publicPay || t.fleetType == .privatePay else { return }
                self?.trip.canSaveSteps = !update.doNotTrackTrip
                self?.onTripUpdate(update)
            case .failure(let error):
                if let err = error as? SessionError, case .unexpectedResponse = err.code {
                    return
                }
                self?.onFail(error)
                self?.restore(steps: buffer)
            }
        }
        trip.steps.removeAll()
        storage.save(trip)
    }
    
    func restore(steps: [Trip.Step]) {
        trip.steps += steps
        storage.save(trip)
    }
    
    func checkStatus() {
        let getTripDetails: (Int) -> Void = { tripId in
            self.network.getTrip(by: tripId) { [weak self] (result) in
                switch result {
                case .success(let trip):
                    self?.trip = trip
                    self?.didStart(trip: trip.tripId)
                case .failure(let error):
                    self?.onFail(error)
                }
            }
        }
        network.getStatus { [weak self] (result) in
            switch result {
            case .success(let status):
                switch status.status {
                case .trip(let tripId):
                    getTripDetails(tripId)
                default: break
                }
            case .failure(let error):
                self?.onFail(error)
            }
        }
    }
}

extension TripService: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.last else { return }
        location = loc
    }
}

