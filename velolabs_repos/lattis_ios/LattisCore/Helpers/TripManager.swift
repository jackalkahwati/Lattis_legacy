//
//  TripManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 24.09.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import Wrappers
import MapKit
import Stripe

final class TripManager: NSObject {
    typealias API = TripAPI & BikeAPI
    
    fileprivate(set) var trip: Trip
    let bike: Bike
    let api: API
    let deviceManager: DeviceManager
    @UserDefaultsBacked(key: "shouldShowManualUnlockPopUp", defaultValue: false)
    var shouldShowManualUnlockPopUp: Bool
    var geofences: [Geofence] = [] { didSet { geofencesToPolygons(geofences: geofences) }}
    weak var stripeContext: STPAuthenticationContext?
    fileprivate let locationManager = CLLocationManager()
    fileprivate var timer: Timer!
    fileprivate let updateInterval: TimeInterval = 10
    fileprivate let trackingDistance: CLLocationDistance = 1
    fileprivate let timeFormatter = DateComponentsFormatter()
    fileprivate var tripIsFinished = false
    @JSONStorageBacked
    fileprivate var steps: [Trip.Step]
    fileprivate var polygons: [MKPolygon] = []
    fileprivate var invoice: Trip.Invoice? {
        didSet {
            priceString = invoice?.price
            duration = invoice?.duration ?? 0
            batteryLevel = invoice?.bikeBatteryLevel
            if let trip = invoice?.endedTrip {
                didEnd(trip: trip, byUser: false)
            }
        }
    }
    fileprivate(set) var location: CLLocation? {
        didSet {
            guard let current = location, trip.isStarted else { return }
            // Location tracking is enabled
            if let noTrack = trip.disableTracking, noTrack { return }
            // Last step is <trackingDistance> away from current location
            if let step = steps.last,
               current.distance(from: .init(latitude: step.coordinate.latitude, longitude: step.coordinate.longitude)) < trackingDistance {
                return
            }
            steps.append(.init(current.coordinate))
        }
    }
    fileprivate var updateTime: TimeInterval = 5 {
        didSet {
            if updateTime >= updateInterval {
                updateTime = 0
                updateTrip()
            }
        }
    }
    fileprivate var duration: TimeInterval = 0 {
        didSet {
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
            duarationString = timeFormatter.string(from: duration)
        }
    }
    fileprivate(set) var duarationString: String? {
        didSet {
            if let duration = duarationString, duration != oldValue, invoice != nil {
                send(.updateDuration(duration))
            }
        }
    }
    fileprivate(set) var priceString: String? {
        didSet {
            if let price = priceString, price != oldValue {
                send(.updatePrice(price))
            }
        }
    }
    fileprivate(set) var security: Device.Security = .undefined {
        didSet {
            if security != oldValue {
                send(.updateSecurity(security))
                if let location = location, security == .locked || security == .unlocked {
                    steps.append(.init(location.coordinate, lockState: .track(security == .locked)))
                }
            }
        }
    }
    fileprivate(set) var connection: Device.Connection = .disconnected {
        didSet {
            guard connection != oldValue && !tripIsFinished else { return }
            send(.updateConnection(connection))
        }
    }
    fileprivate(set) var batteryLevel: Double? {
        didSet {
            guard let value = batteryLevel, value != oldValue else { return }
            send(.updateBatteryLevel(Int(value)))
        }
    }
    
    init(_ trip: Trip, bike: Bike, api: API = AppRouter.shared.api(), device: DeviceManager? = nil, unlock: Bool = false) {
        self.trip = trip
        self.bike = bike
        self.api = api
        self.deviceManager = device ?? bike.deviceManager()
        self._steps = .init(fileName: "\(trip.tripId)_steps.json", defaultValue: [])
        super.init()
        NotificationCenter.default.addObserver(self, selector: #selector(deviceState(notification:)), name: .deviceStatusUpdated, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(messageReceived(notification:)), name: .deviceMessage, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(internetConnection(notification:)), name: .internetConnection, object: nil)
        locationManager.delegate = self
        locationManager.startUpdatingLocation()
        locationManager.allowsBackgroundLocationUpdates = true
        beginUpdate()
        deviceManager.connect { [weak self] _ in
            if let `self` = self, unlock && self.deviceManager.state.in([.iot, .tapkey]) {
                self.deviceManager.unlock()
            }
        }
    }
    
    enum Status {
        case finished(Trip, Bool)
        case parking(Parking.Fee)
        case updateDuration(String)
        case updatePrice(String)
        case updateSecurity(Device.Security)
        case updateConnection(Device.Connection)
        case updateBatteryLevel(Int)
        case requestLock(String)
        case bleEnabled(Bool)
        case needImage(Trip.End)
        case noLocationFound
        case tripStarted
        case jammingCondition(Bool)
        case axaCloseAlert(Bool)
        case linkaOperationAlert(Bool, Bool)
        case failure(Error)
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}

extension Parking.Check {
    init(_ bike: Bike, location: CLLocation) {
        self.init(fleetId: bike.fleetId, latitude: location.coordinate.latitude, longitude: location.coordinate.longitude)
    }
}

// Public methods
extension TripManager {
    
    func beginUpdate() {
        timer = .scheduledTimer(withTimeInterval: 1, repeats: true, block: { [weak self] (_) in
            self?.increment()
        })
    }
    
    func toggleLock() -> String? {
        if deviceManager.bleRestricted { return "bluetooth_access_alert_message".localized() }
        if deviceManager.connection == .disconnected { return "connect_to_lock_popup".localized() }
        if deviceManager.security == .locked {
            deviceManager.unlock()
        } else if deviceManager.security == .unlocked {
            deviceManager.weakLock { [unowned self] (request) in
                self.send(.requestLock(request))
            }
        }
        return nil
    }
    
    func startTrip() {
        guard !trip.isStarted, let location = location else { return }
        api.startTrip(with: .init(bikeId: bike.bikeId, latitude: location.coordinate.latitude, longitude: location.coordinate.longitude)) { [weak self] (result) in
            switch result {
            case .failure(let error):
                self?.send(.failure(error))
            case .success(let trip):
                self?.trip = trip
                self?.send(.tripStarted)
            }
        }
    }
    
    func endTrip(_ endInfo: Trip.End? = nil, parking: Bool = true, force: Bool = false, damageReported: Bool? = nil, chargeId: String? = nil) -> Bool {
        guard deviceManager.security != .progress && invoice?.endedTrip == nil else { return false }
        let tripEnd: Trip.End
        if let e = endInfo {
            tripEnd = e
        } else if let e = try? trip.end(location: location, bikeDamaged: damageReported, chargeId: chargeId) {
            tripEnd = e
        } else {
            send(.noLocationFound)
            return false
        }
        func end() {
            deviceManager.strongLock { [unowned self] in
                self.endUpdate()
                self.api.end(trip: tripEnd, completion: { [weak self] (res) in
                    switch res {
                    case .failure(let error):
                        self?.handle(error: error)
                    case.success(let trip):
                        self?.didEnd(trip: trip)
                    }
                })
            }
        }
        func check() {
            guard let location = location else { return send(.noLocationFound) }
            api.checkParkingFee(check: .init(bike, location: location)) { [weak self] (result) in
                switch result {
                case .failure(let error):
                    self?.send(.failure(error))
                case .success(let fee):
                    switch fee {
                    case .allowed where self != nil:
                        _ = self?.endTrip(parking: false)
                    default:
                        self?.send(.parking(fee))
                    }
                }
            }
        }
        // Flow
        if force {
            end()
        } else if !trip.isStarted {
            endUpdate()
            api.end(trip: tripEnd, completion: { [weak self] (res) in
                switch res {
                case .failure(let error):
                    self?.handle(error: error)
                case.success(let trip):
                    self?.didEnd(trip: trip)
                }
            })
        } else if let damage = tripEnd.bikeDamaged, damage {
            end()
        } else if parking {
            check()
        } else if bike.shortEndRide {
            end()
        } else if tripEnd.parkingImage == nil {
            send(.needImage(tripEnd))
            return false
        } else {
            end()
        }
        return true
    }
    
    func smartDock(callBack: @escaping (Trip)->Void) {
        api.getTrip(by: trip.tripId) { [weak self] (result) in
            switch result.unwrap(\.trip) {
            case .failure(let error):
                self?.send(.failure(error))
            case .success(let trip):
                self?.trip = trip
                self?.didEnd(trip: trip)
                callBack(trip)
            }
        }
    }
    
    func send(_ status: Status) {
        if case .tripStarted = status, deviceManager.state == .manualLock {
            shouldShowManualUnlockPopUp = true
        }
        NotificationCenter.default.post(name: .tripUpdated, object: status)
    }
    
    func updateTrip() {
        guard trip.endedAt == nil else { return }
        if trip.isStarted {
            verifyGeofence()
        }
        let buffer = steps
        steps.removeAll()
        api.update(trip: .init(tripId: trip.tripId, steps: buffer)) { [weak self] result in
            switch result {
            case .failure(let error):
                Analytics.report(error)
                self?.steps = buffer + self!.steps
            case .success(let invoice):
                self?.invoice = invoice
            }
        }
    }
    
    fileprivate func geofencesToPolygons(geofences: [Geofence]) {
        polygons = geofences.map { fence in
            var coordinates = fence.coordinates
            return .init(coordinates: &coordinates, count: fence.coordinates.count)
        }
    }
}

extension TripManager: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        location = locations.last
    }
}

fileprivate extension TripManager {
    
    func increment() {
        duration += 1
        updateTime += 1
    }
    
    func endUpdate() {
        timer?.invalidate()
    }
    
    func didEnd(trip: Trip, byUser: Bool = true) {
        _steps.destroy()
        endUpdate()
        tripIsFinished = true
        deviceManager.disconnect()
        send(.finished(trip, byUser))
        cancelGeofenceNotifications()
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [Notification.Name.reservationEndingSoon.rawValue])
    }
    
    @objc
    func deviceState(notification: Notification) {
        guard let _ = notification.object as? DeviceRepresenting else { return }
        security = deviceManager.security
        connection = deviceManager.connection
    }
    
    @objc
    func messageReceived(notification: Notification) {
        guard let _ = notification.object as? DeviceRepresenting,
              let info = notification.userInfo as? [String: DeviceManager.Message],
              let message = info["message"] else { return }
        switch message {
        case .axaCloseAlert(let happend):
            send(.axaCloseAlert(happend))
        case .linkaOperationAlert(let locking, let happend):
            send(.linkaOperationAlert(locking, happend))
        case .ellipseJamming(let happend):
            send(.jammingCondition(happend))
            reportJamming()
        case .failure(let error):
            send(.failure(error))
        case .bleEnabled(let enabled):
            send(.bleEnabled(enabled))
        }
    }
    
    @objc
    func internetConnection(notification: Notification) {
        guard let connected = notification.object as? Bool else { return }
        for dev in deviceManager.list {
            if dev.kind == .iot {
                if connected {
                    dev.connect()
                } else {
                    dev.disconnect()
                }
            }
        }
    }
    
    func reportJamming() {
        api.send(metadata: .jamming(bike)) { [weak self] (result) in
            switch result {
            case .failure(let err):
                Analytics.report(err)
            case .success where self?.bike.lockId != nil:
                Analytics.log(.jamming(lockId: self!.bike.lockId!))
            case .success:
                print("Sent jamming for empty lock")
            }
        }
    }
}

// Static initializers
extension TripManager {
    static func startTrip(_ booking: Bike.Booking,
                          bike: Bike,
                          coordinate: CLLocationCoordinate2D,
                          api: API = AppRouter.shared.api(),
                          deviceManager: DeviceManager? = nil,
                          completion: @escaping (Result<TripManager, Error>) -> Void) {
        api.startTrip(with: .init(bikeId: bike.bikeId, latitude: coordinate.latitude, longitude: coordinate.longitude)) { (result) in
            switch result {
            case .failure(let error):
                completion(.failure(error))
            case .success(let trip):
                let manager = TripManager(trip, bike: bike, api: api, device: deviceManager)
                if let device = deviceManager {
                    if device.state == .manualLock {
                        manager.shouldShowManualUnlockPopUp = true
                    }
                    if device.state.in([.iot, .tapkey, .edge]) && device.security == .locked {
                        device.unlock()
                    }
                }
                completion(.success(manager))
            }
        }
    }
    
    static func startTrip(_ bike: Bike,
                          coordinate: CLLocationCoordinate2D,
                          pricing: Int?,
                          api: API = AppRouter.shared.api(),
                          deviceManager: DeviceManager? = nil,
                          completion: @escaping (Result<TripManager, Error>) -> Void) -> DeviceManager {
        let device = deviceManager ?? bike.deviceManager()
        device.connect { e in
            if let error = e {
                return completion(.failure(error))
            }
            api.book(bike: bike, pricingId: pricing) { (result) in
                switch result {
                case .failure(let error):
                    completion(.failure(error))
                case .success(let booking):
                    device.unlock()
                    startTrip(booking, bike: bike, coordinate: coordinate, api: api, deviceManager: device, completion: completion)
                }
            }
        }
        return device
    }
}

// MARk: - geofence verifications
fileprivate extension TripManager {
    func verifyGeofence() {
        guard let location = location, !polygons.isEmpty else { return }
        let check: [Bool] = polygons.compactMap { fence in
            let point = MKMapPoint(location.coordinate)
            let renderer = MKPolygonRenderer(polygon: fence)
            let pointRenderer = renderer.point(for: point)
            return renderer.path.contains(pointRenderer)
        }
        if !check.contains(true) {
            notifyGeofenceViolation()
        }
    }
    
    func notifyGeofenceViolation() {
        if UIApplication.shared.applicationState == .active {
            AppRouter.shared.showGeofenceAlert()
        } else {
            cancelGeofenceNotifications()
            
            let center = UNUserNotificationCenter.current()
            
            let postNotification = {
                let content = UNMutableNotificationContent()
                content.title = "notice".localized()
                content.subtitle = "geo_fence_warning".localized()
                content.sound = UNNotificationSound.default
                let request = UNNotificationRequest(identifier: Notification.geofenceViolationIdentifier, content: content, trigger: nil)
                
                center.add(request)
            }
            
            center.requestAuthorization(options: [.alert, .sound, .badge]) { (success, error) in
                if success {
                    postNotification()
                } else  if let e = error {
                    print(e)
                }
            }
        }
    }
    
    func cancelGeofenceNotifications() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [Notification.geofenceViolationIdentifier])
        UNUserNotificationCenter.current().removeDeliveredNotifications(withIdentifiers: [Notification.geofenceViolationIdentifier])
    }
}
