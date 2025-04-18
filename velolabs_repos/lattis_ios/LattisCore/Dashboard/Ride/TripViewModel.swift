//
//  TripViewModel.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 02/09/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import CoreLocation
import EllipseLock
import AXALock
import Model

extension Trip {
    struct Info {
        let price: String?
        let duration: String?
        var lockState: LockState
        
        func updated(duration: String?) -> Info {
            .init(price: price, duration: duration, lockState: lockState)
        }
    }
}

enum EllipseState {
    case disconnected
    case searching
    case connecting
    case connected
}

final class TripViewModel {
    
    let bike: Bike
    var onError: (Error) -> () = {_ in}
    var onLockState: (EllipseState) -> () = {_ in}
    var bleHandler: (Bool) -> () = {_ in} { didSet { bleHandler(bleEnabled) } }
    var onAxaHint: (Bool) -> () = {_ in}
    var onIoTwithEllipseLock: () -> Void = {}
    fileprivate(set) var trip: Trip?
    var onUpdate: (Trip.Info) -> () = {_ in} {
        didSet {
            guard let info = info else { return }
            onUpdate(info)
        }
    }
    var iotStateUpdate: (Bool) -> Void = {_ in}
    var jamming: (Bool) -> Void = {_ in}
    var bleEnabled: Bool { ellipseManager.isOn }
    var bleIsReguired: Bool { !ellipseManager.isOn && (isEllipse || isAxa) }
    var isEllipse: Bool { bike.macId != nil }
    var isAxa: Bool { axa != nil }
    var hasIot: Bool { iotModule != nil }
    
    var location: CLLocation? {
        didSet {
            guard let old = oldValue,
                let location = location,
                old.distance(from: location) > trackingDistance else { return }
            save(step: .init(location.coordinate))
        }
    }
    fileprivate var lockState: LockState = .disconnected {
        didSet {
            if iotModule != nil && isIotOn == nil { return }
            info?.lockState = lockState
        }
    }
    fileprivate var isIotOn: Bool? {
        didSet {
            guard let on = isIotOn else { return }
            iotStateUpdate(on)
        }
    }
    fileprivate var iotStatus: IoTModule.Status? {
        didSet {
            guard let status = iotStatus else { return }
            isIotOn = !status.locked
        }
    }
    fileprivate var isGPSTrackingEnabled = true
    fileprivate let updateInterval: TimeInterval = 10
    fileprivate let trackingDistance: CLLocationDistance = 1
    fileprivate var timer: Timer?
    fileprivate let timeFormatter = DateComponentsFormatter()
    fileprivate let network: TripAPI & BikeAPI = AppRouter.shared.api()
    fileprivate let ellipseManager = EllipseManager.shared
    fileprivate var stepsStorage: JSONStorage<[Trip.Step]>?
    fileprivate var ellipse: Ellipse?
    fileprivate var iotModule: IoTModule?
    fileprivate var axa: IoTModule?
    fileprivate var axaLock: AxaBLE.Lock?
    fileprivate let axaHandler: AxaBLE.Handler = .init()
    fileprivate var startHandler: ((Error?) -> ())?
    fileprivate var duration: TimeInterval = 0
    fileprivate var steps: [Trip.Step] = [] {
        didSet {
            stepsStorage?.save(steps)
        }
    }
    fileprivate var info: Trip.Info? {
        didSet {
            guard let info = info else { return }
            onUpdate(info)
        }
    }
    
    init(_ bike: Bike, trip: Trip? = nil) {
        self.bike = bike
        self.trip = trip
        checkVendors()
        tripStarted()
    }
    
    init(_ trip: Trip) {
        self.bike = trip.bike!
        self.trip = trip
        checkVendors()
        tripStarted()
    }
    
    func startTrip(completion: @escaping (Error?) -> ()) {
        func performAction() {
            network.startTrip(with: bike) { [weak self] (result) in
                switch result {
                case .success(let trip):
                    self?.trip = trip
                    self?.tripStarted()
                    completion(nil)
                case .failure(let error):
                    completion(error)
                }
            }
        }
        if iotModule != nil {
            toggleIoT(lock: false) { err in
                if let error = err {
                    completion(error)
                } else {
                    performAction()
                }
            }
        } else {
            performAction()
        }
        
    }
    
    func unlockAndStartTrip(completion: @escaping (Error?) -> ()) {
        checkVendors()
        startHandler = completion
        if iotModule != nil && !isEllipse {
            return startTrip(completion: completion)
        }
        connectLock()
    }
    
    func endTrip(isTheft: Bool = false, completion: @escaping (Error?, Trip.End?, Trip?) -> ()) {
        guard let trip = trip else {
            completion(Trip.End.Fail.noTrip, nil, nil)
            return
        }
        func performAction() {
            do {
                let endInfo = try trip.end(location: location)
                guard isTheft || bike.shortEndRide || !trip.isStarted else {
                    completion(nil, endInfo, nil)
                    return
                }
                network.end(trip: endInfo) { [weak self] (result) in
                    switch result {
                    case .success(let trip):
                        AppRouter.shared.lockInfo = nil
                        AppRouter.shared.macId = nil
                        self?.didEnd(trip: trip)
                        completion(nil, nil, trip)
                    case .failure(let error):
                        completion(error, nil, nil)
                    }
                }
            } catch {
                completion(error, nil, nil)
            }
        }
        if iotModule != nil {
            toggleIoT(lock: true) { error in
                if let e = error {
                    completion(e, nil, nil)
                } else {
                    performAction()
                }
            }
        } else {
            performAction()
        }
    }
    
    func cancelBooking(with reason: Bike.Unbooking.Reason = .none, completion: @escaping (Error?) -> ()) {
        network.cancelBooking(with: bike.unbook(with: reason)) { [weak self] (result) in
            switch result {
            case .success:
                self?.disconnectLock()
                completion(nil)
            case .failure(let error):
                completion(error)
            }
        }
    }
    
    func connectLock() {
        checkVendors()
        if axa != nil {
            return connectAxa()
        }
        if iotModule != nil {
            if !isEllipse { onLockState(.connecting) }
            network.iotStatus(bikeId: bike.bikeId) { [weak self] (result) in
                switch result {
                case .failure(let error):
                    Analytics.report(error)
                    self?.onError(error)
                case .success(let status):
                    self?.iotStatus =  status
                    if let check = self?.isEllipse, !check {
                        self?.onLockState(.connected)
                        self?.toggleIoT(lock: false)
                    } else if let state = self?.lockState {
                        self?.info?.lockState = state
                        if let handler = self?.startHandler, let c = self?.ellipse?.connection, case .ready = c {
                            self?.startTrip(completion: handler)
                        }
                    }
                }
            }
        }
        if let ellipse = ellipse {
            ellipse.connect(handler: self, bike: bike)
        } else if let ellipse = ellipseManager.locks.first(where: {$0.macId == bike.macId}) {
            self.ellipse = ellipse
            ellipse.connect(handler: self, bike: bike)
        } else if bike.macId != nil {
            onLockState(.searching)
            ellipseManager.scan(with: self)
        } else {
            return
        }
        ellipseManager.subscribe(handler: self)
    }
    
    fileprivate func connectAxa() {
        axaHandler.discovered = { [unowned self] lock in
            guard let remote = self.axa, remote.key == lock.id else { return }
            self.axaLock = lock
            lock.connect(with: self.axaHandler)
        }
        axaHandler.connectionChanged = { [unowned self] lock in
            if let handler = self.startHandler, case .paired = lock.connection {
                self.startTrip(completion: handler)
            }
            self.onLockState(lock.connection.ellipe)
        }
        axaHandler.statusChanged = { [unowned self] lock in
            switch lock.status {
            case .open:
                self.lockState = .unlocked
                self.onAxaHint(false)
            case .strongClosed:
                self.lockState = .locked
                self.onAxaHint(false)
            case .weakClosed:
                self.onAxaHint(true)
            default:
                break
            }
        }
        if let lock = axaLock {
            lock.connect(with: axaHandler)
        } else if let key = axa?.key, let lock = AxaBLE.Lock.all.first(where: \.id, isEqual: key) {
            self.axaLock = lock
            lock.connect(with: axaHandler)
        } else {
            AxaBLE.Lock.scan(with: axaHandler)
        }
    }
    
    func disconnectLock() {
        ellipse?.disconnect()
        ellipse?.unsubscribe(self)
        axaLock?.disconnect()
    }
    
    func toggleLock() -> LockState {
        let old = lockState
        switch lockState {
        case .locked:
            if iotModule != nil {
                toggleIoT(lock: false)
            }
            ellipse?.unlock()
            axaLock?.unlock()
        case .unlocked:
            if iotModule != nil && ellipse == nil {
                toggleIoT(lock: true)
            }
            ellipse?.lock()
            axaLock?.lock()
            if axaLock != nil {
                onAxaHint(true)
            }
        case .disconnected:
            return .disconnected
        case .progress:
            break
        }
        lockState = .progress
        return old
    }

    func didEnd(trip: Trip) {
        ellipse?.disconnect()
        axaLock?.disconnect()
        axaLock = nil
        ellipse = nil
        calculate(invoice: trip.invoice)
        timer?.invalidate()
        toggleIoT(lock: true)
    }
    
    func fetchTrip(by tripId: Int, completion: @escaping (Error?) -> ()) {
        network.getTrip(by: tripId) { [weak self] (result) in
            switch result {
            case .failure(let error):
                completion(error)
            case .success(let trip):
                self?.trip = trip
                self?.tripStarted()
                completion(nil)
            }
        }
    }
    
    func toggleIoT(lock: Bool, completion: @escaping (Error?) -> Void = {_ in}) {
        if lock {
            guard let flag = isIotOn else { return completion(nil) }
            if !flag { return completion(nil) }
            network.lock(bikeId: bike.bikeId) { [weak self] (result) in
                switch result {
                case .success:
                    self?.isIotOn = false
                    if let check = self?.isEllipse, !check {
                        self?.lockState = .locked
                    }
                    completion(nil)
                case .failure(let error):
                    Analytics.report(error)
                    self?.onError(error)
                    completion(error)
                }
            }
        } else {
            guard let flag = isIotOn else { return completion(nil) }
            if flag { return completion(nil) }
            network.unlock(bikeId: bike.bikeId) { [weak self] (result) in
                switch result {
                case .success:
                    self?.isIotOn = true
                    if let check = self?.isEllipse, !check {
                        self?.lockState = .unlocked
                    }
                    completion(nil)
                case .failure(let error):
                    Analytics.report(error)
                    self?.onError(error)
                    completion(error)
                }
            }
        }
    }
    
    fileprivate func checkVendors() {
        if let iot = bike.iot(key: \.deviceType, isEqualTo: .iot) {
            self.iotModule = iot
        }
        if let axa = bike.iot(key: \.vendor, isEqualTo: "AXA") {
            self.axa = axa
        }
    }
    
    fileprivate func save(step: Trip.Step) {
        guard isGPSTrackingEnabled,
            let trip = trip,
            trip.isStarted else { return }
        steps.append(step)
    }
    
    fileprivate func tripStarted() {
        guard let trip = trip else { return }
        if startHandler != nil {
            startHandler = nil
            ellipse?.unlock()
            axaLock?.unlock()
        }
        stepsStorage = .init("\(trip.tripId)_steps.json")
        calculate(invoice: trip.invoice)
        startTimer()
        Analytics.log(.tripStarted(trip))
    }
    
    fileprivate func calculate(invoice: Trip.Invoice) {
        isGPSTrackingEnabled = invoice.doNotTrackTrip == false
        duration = invoice.duration
        
        let price: String?
        if let trip = trip, let type = trip.fleetType, type.isFree {
            price = nil
        } else {
            price = invoice.price
        }
        info = .init(price: price, duration: convert(duration: duration), lockState: lockState)
    }
    
    fileprivate func convert(duration: TimeInterval) -> String? {
        if duration < .minute {
            timeFormatter.allowedUnits = [.second]
            timeFormatter.unitsStyle = .short
        } else if duration < .hour {
            timeFormatter.allowedUnits = [.minute]
            timeFormatter.unitsStyle = .short
        } else {
            timeFormatter.allowedUnits = [.day, .hour, .minute]
            timeFormatter.unitsStyle = .positional
        }
        
        return timeFormatter.string(from: duration)
    }
    
    fileprivate func increment() {
        duration += 1
        if Int(duration)%Int(updateInterval) == 0 {
            updateTrip()
        }
        info = info?.updated(duration: convert(duration: duration))
    }
    
    fileprivate func startTimer() {
        timer?.invalidate()
        timer = .scheduledTimer(withTimeInterval: 1, repeats: true, block: { [weak self] (_) in
            self?.increment()
        })
    }
    
    fileprivate func updateTrip() {
        guard let trip = trip else { return }
        let cache = steps
        steps.removeAll()
        network.update(trip: trip.upload(cache)) { [weak self] (result) in
            switch result {
            case .success(let invoice):
                self?.calculate(invoice: invoice)
            case .failure(let error):
                self?.recover(steps: cache)
                self?.onError(error)
            }
        }
    }
    
    fileprivate func recover(steps: [Trip.Step]) {
        self.steps = steps + self.steps
    }
    
    deinit {
        ellipseManager.stopScan()
        stepsStorage?.destroy()
        timer?.invalidate()
    }
}

extension TripViewModel: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool) {
        bleHandler(connected)
    }
    
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Ellipse], delete: [Ellipse]) {
        guard let ellipse = lockManager.locks.first(where: {$0.macId == bike.macId}) else { return }
        self.ellipse = ellipse
        onLockState(.connecting)
        ellipse.connect(handler: self, bike: bike)
        lockManager.stopScan()
    }
}

extension TripViewModel: EllipseDelegate {
    func ellipse(_ ellipse: Ellipse, didUpdate security: Ellipse.Security) {
        switch security {
        case .locked:
            self.lockState = .locked
            if iotModule != nil, let on = isIotOn, on {
                onIoTwithEllipseLock()
            }
            jamming(false)
        case .unlocked:
            self.lockState = .unlocked
        case .invalid, .middle:
            if lockState == .progress {
                lockState = .unlocked
            }
            jamming(true)
            network.send(metadata: .jamming(bike), completion: {_ in})
        default:
            return
        }
        guard let c = self.location?.coordinate else { return }
        self.save(step: .init(c, lockState: .track(self.lockState == .locked)))
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate connection: Ellipse.Connection) {
        switch connection {
        case .paired:
            onLockState(.connected)
        case .ready:
            if let handler = startHandler {
                if iotModule != nil && isIotOn == nil { return }
                startTrip(completion: handler)
            }
        case .connecting, .unpaired, .reconnecting:
            self.lockState = .disconnected
        case .failed(let error):
            startHandler?(error)
            startHandler = nil
        default:
            break
        }
    }
}

extension AxaBLE.Lock.Connection {
    var ellipe: EllipseState {
        switch self {
        case .connected:
            return .connecting
        case .disconnected:
            return .disconnected
        case .paired:
            return .connected
        case .security:
            return .connecting
        }
    }
}
