//
//  RouteToBikeRouteToBikeInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import CoreLocation
import SwiftyTimer
import Oval
import LattisSDK
import AXALock

final class RouteToBikeInteractor: NSObject {
    var bike: Bike! {
        didSet {
            bike.status = .booked
//            lock = Lock(ellipse: Ellipse(macId: bike.macId))
            lock = Lock(bike: bike)
//            lock = Lock(ellipse: Ellipse(macId: "F16CDF328973"))
        }
    }
    var router: RouteToBikeRouter!
    weak var view: RouteToBikeInteractorOutput!
    var isLockConnected: Bool = false
    var location: CLLocation = .init(latitude: -180, longitude: -180) {
        didSet {
            tripService?.location = location
        }
    }
    fileprivate var limit = 10.seconds
    fileprivate var left = 0.seconds
    fileprivate let ble = EllipseManager.shared
    fileprivate let network: BikeNetwork
    fileprivate var isRestored = false
    fileprivate var expireTime: Date?
    fileprivate var timer: Timer?
    fileprivate var metadataSent = false
    fileprivate var tripService: TripService?
    fileprivate var tripTimer: Timer?
    fileprivate var rideDuration: TimeInterval = 0
    fileprivate var rideFare: Double = 0
    fileprivate var connectionFailed: Bool = false
    fileprivate let axaHandler = AxaBLE.Handler()
    
    fileprivate var lock: Lock!
    init(network: BikeNetwork = Session.shared) {
        self.network = network
        super.init()
        
        NotificationCenter.default.addObserver(self, selector: #selector(appDidEnterForeground), name: UIApplication.didBecomeActiveNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(appDidEnterBackground), name: UIApplication.willResignActiveNotification, object: nil)
        
        axaHandler.discovered = { [unowned self] lock in
            guard let id = self.lock?.axaModule?.key, id == lock.id else { return }
            self.lock.axaLock = lock
            lock.connect(with: self.axaHandler)
        }
        axaHandler.connectionChanged = { [unowned self] lock in
            guard self.lock.axaLock == lock else { return }
            var isConnected = false
            switch lock.connection {
            case .paired:
                isConnected = true
                self.view.connected()
            case .disconnected:
                self.view.disconnected()
            default:
                break
            }
            self.isLockConnected = isConnected
        }
    }
    
    deinit {
        tripTimer?.invalidate()
        ble.stopScan()
        NotificationCenter.default.removeObserver(self)
    }
    
    func restore(with bike: Bike, limit: TimeInterval, left: TimeInterval) {
        self.bike = bike
        self.limit = limit
        self.left = left
        isRestored = true
    }
    
    func restore(with bike: Bike, trip: Trip) {
        self.bike = bike
        rideDuration = abs(trip.startedAt?.timeIntervalSinceNow ?? trip.duration)
        if let price = trip.price {
            rideFare = price
        }
        self.startRide(auto: true)
        isRestored = true
    }
}

extension RouteToBikeInteractor: RouteToBikeInteractorInput {
    func checkBLE() {
        if !ble.isOn {
            view.showBLEWarning()
        }
    }
    
    func startBooking() {
        checkBLE()
        AppRouter.shared.cancelBooking = { [unowned self] force in
            if force {
                self.performCancel()
            } else {
                self.cancelTrip()
            }
        }
        view.buildRoute(to: bike)
        guard isRestored == false else {
            searchLock()
            startCount()
            return
        }
        
        network.book(bike: bike, coordinate: nil) { [weak self] (result) in
            switch result {
            case .success(let limit, let phone):
                AppRouter.shared.fleetSupportPhone = phone
                self?.bike.status = .booked
                self?.searchLock()
                self?.startCount(with: limit)
            case .failure(let error):
                if let e = error as? SessionError, case .conflict = e.code {
                    let alert = ActionAlertView.alert(title: "mandatory_phone_title".localized(), subtitle: "mandatory_phone_text".localized())
                    alert.action = .init(title: "mandatory_phone_action".localized()) {
                        self?.router.addPhoneNumber()
                    }
                    alert.cancel = .init(title: "cancel".localized()) {
                        self?.router.pop()
                    }
                    alert.show()
                    return
                }
                self?.view.show(error: error, file: #file, line: #line)
                self?.view.hideSpinner()
            }
        }
    }
    
    func beginTrip() {
        if let service = tripService {
            router.openRide(with: service, lock: lock, in: location.coordinate)
            return
        }
        view.startLoading(with: "route_to_bike_start".localized())
        router.openRide(with: bike, in: location.coordinate, onStart: { [weak self] interactor in
            if let lock = self?.lock {
                interactor.lock = lock
            }
            self?.view.stopLoading(completion: nil)
            }, onFail: { [weak self] error in
                self?.view.show(error: error, file: #file, line: #line) {
                    self?.router.pop()
                    AppRouter.shared.postCancelBooking?()
                }
        })
    }
    
    func trackUnconnectedBegin() {
    }
    
    func openMenu() {
        router.openMenu() { $0.bike = self.bike }
    }
    
    func cancelTrip() {
        view.showCancelWarning(tripStarted: tripService != nil)
    }
    
    func performCancel() {
        if let service = tripService {
            service.keepLocation()
            service.onEnd = { [unowned self, unowned service] in
                self.view.stopLoading(completion: nil)
                var trip = service.trip
                trip.isCanceled = true
                self.router.openSummary(trip: trip, delegate: self)
            }
            view.startLoading(with: "active_ride_ending_trip".localized())
            service.end(with: nil)
            return
        }
        AppRouter.shared.cancelBooking = nil
        disconttectLock()
        view.startLoading(with: "bike_booking_cancelling_book_loader".localized())
        network.unbook(bike: bike, damageReported: AppRouter.shared.damage, connectionFailed: connectionFailed) { [weak self] (result) in
            switch result {
            case .success:
                self?.view.stopLoading(completion: nil)
                self?.router.pop()
                AppRouter.shared.postCancelBooking?()
            case .failure(let error):
                if let err = error as? SessionError, case .resourceNotFound = err.code {
                    self?.view.stopLoading(completion: nil)
                    self?.router.pop()
                    AppRouter.shared.postCancelBooking?()
                } else {
                    self?.view.show(error: error, file: #file, line: #line)
                }
            }
        }
        connectionFailed = false
        AppRouter.shared.damage = false
    }
    
    func suspend() {
        stopCount()
    }
    
    func openInfo() {
        router.openInfo(bike: bike)
    }
}

extension RouteToBikeInteractor: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool) {
        if connected == false && view != nil {
            view.showBLEWarning()
        } else {
            view.hideWarnings()
            connectLock()
        }
    }
    
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [LattisSDK.Ellipse], delete: [LattisSDK.Ellipse]) {
        guard lock.peripheral == nil else { return }
        lock.peripheral = lockManager.locks.filter({ $0.macId == self.lock.ellipse?.macId }).first
        connectLock()
    }
}

extension RouteToBikeInteractor: EllipseDelegate {
    func ellipse(_ ellipse: LattisSDK.Ellipse, didUpdate connection: LattisSDK.Ellipse.Connection) {
        var isConnected = false
        switch connection {
        case .paired:
            isConnected = true
            view.connected()
        case .failed(let error):
            if let err = error as? EllipseError, case .accessDenided = err {
                view.warning(with: "ellipse_access_denided_title".localized(), subtitle: "ellipse_access_denided_text".localized(), action: performCancel)
            } else {
                view.show(error: error, file: #file, line: #line, action: performCancel)
            }
            connectionFailed = true
            view.disconnected()
        case .unpaired:
            view.disconnected()
        case .connecting:
            view.connecting()
        default:
            break
        }
        isLockConnected = isConnected
    }
    
    func ellipse(_ ellipse: LattisSDK.Ellipse, didUpdate security: LattisSDK.Ellipse.Security) {
        
    }
    
    func ellipse(_ ellipse: LattisSDK.Ellipse, didUpdate value: LattisSDK.Ellipse.Value) {
        switch value {
        case .firmwareVersion(let version):
            network.send(metadata: .firmware(version, bike), completion: {_ in})
        case .metadata(let metadata) where metadataSent == false:
            metadataSent = true
            network.send(metadata: .lockBattery(metadata.batteryLevel, bike), completion: {_ in})
        default:
            break
        }
    }
}

extension RouteToBikeInteractor: EndRideInteractorDelegate {
    func endTrip(with image: UIImage?, completion: @escaping (Error?, Trip?) -> ()) {
        
    }
    
    func didEndTrip(with rating: Int?) {
        lock.peripheral?.disconnect()
        lock.axaLock?.disconnect()
        router.pop()
    }
}

private extension RouteToBikeInteractor {
    func startRide(auto: Bool) {
        tripService = TripService(bike)
        tripService?.onFail = { [unowned self] error in
            self.view.show(error: error, file: #file, line: #line)
        }
        view.startLoading(with: "route_to_bike_start".localized())
        tripService?.onStart = { [unowned self] in
            self.view.stopLoading(completion: nil)
            self.startRideTimer()
        }
        tripService?.onTripUpdate = { [unowned self] update in
            self.rideDuration = update.duration
            self.rideFare = update.price ?? 0
            self.updateRideTime(increment: false)
            if let trip = update.trip {
                self.dashboardDidFinish(trip: trip)
            }
        }
        let location: CLLocationCoordinate2D? = auto ? nil : self.location.coordinate
        tripService?.start(with: location)
    }
    
    func startRideTimer() {
        tripTimer?.invalidate()
        tripTimer = Timer.every(1.second, { [weak self] in
            self?.updateRideTime()
        })
    }
    
    func dashboardDidFinish(trip: Trip) {
        tripTimer?.invalidate()
        router.openSummary(trip: trip, delegate: self)
    }
    
    func updateRideTime(increment: Bool = true) {
        view.update(tripTime: rideDuration.time, fare: rideFare.priceValue(bike.currency))
        if increment {
            rideDuration += 1
        }
    }
    
    func startCount(with limit: TimeInterval? = nil) {
        if let limit = limit {
            expireTime = Date(timeIntervalSinceNow: limit)
            self.limit = limit
            left = limit
        } else if let time = expireTime {
            left = time.timeIntervalSinceNow
        }
        timer = Timer.every(1.second, { [weak self] in
            self?.calculate()
        })
        view.hideSpinner()
    }
    
    func stopCount() {
        timer?.invalidate()
        timer = nil
    }
    
    func calculate() {
        let time: TimeInterval = left >= 0 ? left : 0
        view.update(time: time.time, for: bike)
        left -= 1
        
        if left < 0 {
            stopCount()
            if bike.fleetType == .privateFree || bike.fleetType == .publicFree {
                router.pop()
            } else {
                startRide(auto: true)
            }
        }
    }
    
    func searchLock() {
        if lock.needEllipse  {
            ble.scan(with: self)
        } else if lock.needAxa {
            if let module = lock.axaModule, let axa = AxaBLE.Lock.all.first(where: {$0.id == module.key}) {
                self.lock.axaLock = axa
                axa.connect(with: axaHandler)
            } else {
                AxaBLE.Lock.scan(with: axaHandler)
            }
        }
    }
    
    func connectLock() {
        if let peripheral = lock.peripheral {
            peripheral.connect(handler: self, bike: bike)
        } else if lock.needEllipse {
            ble.scan(with: self)
        } else if let axa = lock.axaLock {
            axa.connect(with: axaHandler)
        } else if lock.needAxa {
            if let module = lock.axaModule, let axa = AxaBLE.Lock.all.first(where: {$0.id == module.key}) {
                self.lock.axaLock = axa
                axa.connect(with: axaHandler)
            } else {
                AxaBLE.Lock.scan(with: axaHandler)
            }
        }
    }
    
    func disconttectLock() {
        lock.peripheral?.disconnect()
//        ble.clean()
        ble.stopScan()
        lock.axaLock?.disconnect()
    }
    
    func handle(error: Error, file: String = #file, line: Int = #line) {
        view.show(error: error, file: file, line: line)
    }
    
    @objc func appDidEnterForeground() {
        startCount()
    }
    
    @objc func appDidEnterBackground() {
        stopCount()
    }
}
