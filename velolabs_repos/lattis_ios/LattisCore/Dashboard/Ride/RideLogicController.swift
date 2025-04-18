//
//  RideLogicController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 05.05.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import CoreLocation
import OvalAPI
import Model
import Wrappers

enum RideState {
    case actions(ActionButton.Action?, ActionButton.Action)
    case hint(String, UIColor)
    case segwayHint(String)
    case hideHint
    case security(Device.Security)
    case price(String?)
    case duration(String?)
    case batteryLevel(Int)
    case parking(Parking)
    case parkingAlert(Parking.Fee)
    case showParkings
    case processing(String?, (() -> ())?)
    case loading(Bool)
    case failure(Error)
    case summary(Trip)
    case endTrip(Trip.End, TripManager)
    case ccRequired
    case axaAlert(Bool)
    case linkaAlert(Bool, Bool)
    case geofences([Geofence])
    case jamming(Bool)
    case iotAlert(String)
    case scanQrCode(() -> Void)
    case vehicleDocked
    case showReservationAlert
}

final class RideLogicController {
    var isParkingPresent = false
    var bike: Bike { manager.bike }
    var shouldHideLockControll: Bool {
        guard !isParkingPresent else { return true }
        return !manager.trip.isStarted || manager.deviceManager.state == .hub
    }
    var manualCode: String? {
        guard let lock = bike.controllers?.first(where: {Thing.Vendor.compare(rawValue: $0.vendor, to: .manual)}) else { return nil }
        return lock.key
    }
    let manager: TripManager
    fileprivate let api: ParkingAPI & GeofenceAPI & HubsAPI & TripAPI = AppRouter.shared.api()
    fileprivate var endingRide = false
    fileprivate var stateHandler: (RideState) -> Void = {_ in}
    @UserDefaultsBacked(key: "tripIdToHandleDocking")
    fileprivate var tripIdToHandleDocking: Int?
    @UserDefaultsBacked(key: "tripIdToUnlock")
    fileprivate var tripIdToUnlock: Int?
    @UserDefaultsBacked(key: "tripIdToEndRide")
    fileprivate var tripIdToEndRide: Int?
    @UserDefaultsBacked(key: "tripIdToShowSummary")
    fileprivate var tripIdToShowSummary: Int?
    
    init(_ manager: TripManager) {
        self.manager = manager
        NotificationCenter.default.addObserver(self, selector: #selector(handleTrip(notification:)), name: .tripUpdated, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(handleDocking(notification:)), name: .vehicleDocked, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(handleDockingUnlock(notification:)), name: .dockingUnlock, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(handleDockingEndTrip(notification:)), name: .dockingEndRide, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(handleSmartDocking(notification:)), name: .smartDocking, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(handleLocking(notification:)), name: .vehicleLocked, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(checkTripStatus), name: UIApplication.didBecomeActiveNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(handleEndingSoon(notification:)), name: .reservationEndingSoon, object: nil)
    }
    
    func fetchState(completion: @escaping (RideState) -> Void) {
        stateHandler = completion
        completion(.price(manager.priceString))
        completion(.duration(manager.duarationString))
        if !manager.trip.isStarted {
            completion(.hint("booking_timer_expired_label".localized(), .warning))
        }
        fetchGeofences(completion: completion)
        refreshActions(with: .updateSecurity(manager.deviceManager.security))
        refreshActions(with: .updateConnection(manager.deviceManager.connection))
        completion(.security(manager.deviceManager.security))
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) { [unowned self] in
            self.checkTripStatus()
            self.handleIot(state: self.manager.security)
        }
    }
    
    @objc
    fileprivate func checkTripStatus() {
        if let id = self.tripIdToEndRide, self.manager.trip.tripId == id {
            manager.smartDock { trip in
                self.stateHandler(.summary(trip))
            }
        } else if let id = self.tripIdToShowSummary, self.manager.trip.tripId == id {
            if manager.trip.endedAt == nil {
                self.manager.updateTrip()
            } else {
                manager.smartDock { trip in
                    self.stateHandler(.summary(trip))
                }
            }
        } else if let id = self.tripIdToUnlock, self.manager.trip.tripId == id {
            self.undock()
        } else if let id = self.tripIdToHandleDocking, self.manager.trip.tripId == id {
            stateHandler(.vehicleDocked)
        }
    }
    
    @objc
    fileprivate func handleDocking(notification: Notification) {
        guard let trip = notification.userInfo?["trip_id"] as? String,
              let tripId = Int(trip), manager.trip.tripId == tripId else { return }
        stateHandler(.vehicleDocked)
        tripIdToHandleDocking = tripId
        manager.deviceManager.refreshStatus()
    }
    
    @objc
    fileprivate func handleDockingUnlock(notification: Notification) {
        guard let trip = notification.userInfo?["trip_id"] as? String,
              let tripId = Int(trip), manager.trip.tripId == tripId else { return }
        undock()
    }
    
    @objc
    fileprivate func handleDockingEndTrip(notification: Notification) {
        guard let trip = notification.userInfo?["trip_id"] as? String,
              let tripId = Int(trip), manager.trip.tripId == tripId else { return }
        endRide(parking: false)
    }
    
    @objc
    fileprivate func handleSmartDocking(notification: Notification) {
        guard let trip = notification.userInfo?["trip_id"] as? String,
              let tripId = Int(trip), manager.trip.tripId == tripId else { return }
        manager.smartDock { trip in
            self.stateHandler(.summary(trip))
        }
    }
    
    @objc
    fileprivate func handleLocking(notification: Notification) {
        guard let trip = notification.userInfo?["trip_id"] as? String,
              let tripId = Int(trip), manager.trip.tripId == tripId else { return }
        stateHandler(.axaAlert(false))
        if let device = manager.deviceManager.list.first(where: {$0.kind == .omni}) {
            device.didSet(security: .locked)
        }
    }
    
    @objc
    fileprivate func handleEndingSoon(notification: Notification) {
        guard let tripId = notification.userInfo?["trip_id"] as? Int, manager.trip.tripId == tripId else { return }
        stateHandler(.showReservationAlert)
    }
    
    @objc
    fileprivate func handleTrip(notification: Notification) {
        guard let status = notification.object as? TripManager.Status else { return }
        switch status {
        case .updatePrice(let price):
            stateHandler(.price(price))
        case .updateDuration(let duration):
            stateHandler(.duration(duration))
        case .updateSecurity(let security):
            stateHandler(.security(security))
            handleIot(state: security)
            refreshActions(with: status)
        case .failure(let error):
            stateHandler(.failure(error))
        case .finished(let trip, let byUser):
            tripIdToHandleDocking = nil
            tripIdToShowSummary = nil
            tripIdToEndRide = nil
            tripIdToUnlock = nil
//            if !bike.shortEndRide && byUser {
//                return
//            }
            stateHandler(.processing(nil, {
                self.stateHandler(.summary(trip))
            }))
        case .requestLock(let request):
            stateHandler(.iotAlert(request))
        case .jammingCondition(let active):
            stateHandler(.jamming(active))
        case .axaCloseAlert(let happend):
            stateHandler(.axaAlert(happend))
        case .linkaOperationAlert(let locking, let happened):
            stateHandler(.linkaAlert(locking, happened))
        case .bleEnabled, .updateConnection:
            refreshActions(with: status)
        case .tripStarted:
            refreshActions(with: status)
            stateHandler(.processing(nil, nil))
        case .parking(let fee):
            endingRide = false
            Analytics.log(.outOfParking(fleetId: manager.trip.fleetId))
            stateHandler(.parkingAlert(fee))
        case .noLocationFound:
            Analytics.report(ErrorMessage("No location found to end ride \(manager.trip.tripId)"))
        case .needImage(let info):
            stateHandler(.processing(nil, {
                self.endingRide = false
                self.stateHandler(.endTrip(info, self.manager))
            }))
        case .updateBatteryLevel(let level):
            stateHandler(.batteryLevel(level))
        }
    }
    
    fileprivate func refreshActions(with status: TripManager.Status) {
        let left: ActionButton.Action? = manager.trip.isStarted ? nil : .plain(title: "cancel".localized(), style: .plain, handler: { [unowned self] in
            self.endRide()
        })
        let right: ActionButton.Action
        if manager.deviceManager.bleRestricted {
            stateHandler(.hint("bluetooth_access_alert_message".localized(), .azureRadiance))
            right = .plain(title: "connect_to_lock".localized(), style: .inactiveSecondary, handler: { [unowned self] in
                #if targetEnvironment(simulator)
                self.endRide()
                #else
                self.stateHandler(.hint("bluetooth_access_alert_message".localized(), .azureRadiance))
                #endif
            })
        } else if manager.deviceManager.state == .hub {
            right = .plain(title: "find_a_station".localized(), style: .inactiveSecondary) { [unowned self] in
                #if targetEnvironment(simulator)
                self.endRide()
                #else
                self.stateHandler(.showParkings)
                #endif
            }
        } else {
            if manager.trip.isStarted {
                switch status {
                case .tripStarted:
                    let security = manager.deviceManager.security
                    stateHandler(.security(security))
                    handleIot(state: security)
                    return refreshActions(with: .updateSecurity(security))
                case .updateSecurity(let state) where state == .locked:
                    right = .plain(title: "end_ride".localized(), handler: { [unowned self] in
                        self.endRide()
                    })
                    stateHandler(.loading(false))
                case .updateSecurity(let state) where state == .unlocked:
                    right = .plain(title: "end_ride_unselected".localized(), style: .inactiveSecondary) { [unowned self] in
                        #if targetEnvironment(simulator)
                        self.endRide()
                        #endif
                    }
                    stateHandler(.loading(false))
                case .updateConnection(let state) where state == .connected:
                    return refreshActions(with: .updateSecurity(manager.security))
                case .updateConnection(let state) where state == .connecting:
                    stateHandler(.hideHint)
                    right = .plain(title: "connecting_loader".localized(), style: .inactiveSecondary)
                    stateHandler(.loading(true))
                case .updateConnection(let state) where state == .search:
                    #if DEBUG
                    right = .plain(title: "walk_to_bike_label".localized(), style: .inactiveSecondary) {
                        self.endRide(parking: false)
                    }
//                    right = .plain(title: "walk_to_bike_label".localized(), style: .inactiveSecondary)
                    #else
                    right = .plain(title: "walk_to_bike_label".localized(), style: .inactiveSecondary)
                    #endif
                    stateHandler(.loading(false))
                case .updateConnection(let state) where state == .disconnected:
                    stateHandler(.loading(false))
                    if manager.deviceManager.state == .iot {
                        right = .plain(title: "connecting_loader".localized())
                    } else {
                        right = .plain(title: "connect_to_lock".localized(), handler: { [unowned self] in
                            #if targetEnvironment(simulator)
                            self.endRide()
                            #else
                            self.manager.deviceManager.connect()
                            #endif
                        })
                        stateHandler(.hint("connect_to_lock_popup".localized(), .azureRadiance))
                    }
                default:
                    return
                }
            } else {
                switch status {
                case .updateConnection(let state) where state == .connected:
                    right = .plain(title: "booking_begin_trip".localized(), handler: { [unowned self] in
                        if let hint = AppRouter.shared.hintMessage {
                            return self.stateHandler(.hint(hint, .azureRadiance))
                        }
                        self.stateHandler(.hideHint)
                        self.startTrip()
                    })
                    stateHandler(.loading(false))
                case .updateConnection(let state) where state == .connecting:
                    right = .plain(title: "connecting_loader".localized(), style: .inactiveSecondary)
                    stateHandler(.loading(true))
                case .updateConnection(let state) where state == .disconnected:
                    right = .plain(title: "connect_to_lock".localized(), handler: { [unowned self] in
                        self.manager.deviceManager.connect()
                    })
                    stateHandler(.loading(false))
                case .updateConnection(let state) where state == .search:
                    right = .plain(title: "walk_to_bike_label".localized(), style: .inactiveSecondary)
                    stateHandler(.loading(false))
                default:
                    return
                }
            }
        }
        stateHandler(.actions(left, right))
    }
    
    fileprivate func handleIot(state: Device.Security) {
        guard manager.trip.isStarted &&
                manager.deviceManager.state == .iot else { return }
        if state == .locked || state == .unlocked {
            stateHandler(.segwayHint("iot_hint_locked".localized()))
        }
    }
    
    fileprivate func startTrip() {
        if manager.deviceManager.state ~~ [.iot, .manualLock, .tapkey] {
            stateHandler(.scanQrCode({ [unowned self] in
                if self.manager.deviceManager.security != .unlocked {
                    self.stateHandler(.processing("starting_ride_loader".localized(), nil))
                    self.manager.deviceManager.unlock { [unowned self] in
                        self.manager.startTrip()
                    }
                } else {
                    self.stateHandler(.processing("starting_ride_loader".localized(), nil))
                    self.manager.startTrip()
                }
            }))
        } else {
            self.stateHandler(.processing("starting_ride_loader".localized(), nil))
            manager.startTrip()
        }
    }
    
    func toggleLock() {
        guard !endingRide else { return }
        if let hint = manager.toggleLock() {
            stateHandler(.hint(hint, .azureRadiance))
        }
    }
    
    func fetchGeofences(completion: @escaping (RideState) -> ()) {
        api.fetch(by: bike.fleetId) {[weak self] (result) in
            switch result {
            case .failure(let error):
                Analytics.report(error)
            case .success(let gf):
                completion(.geofences(gf))
                self?.manager.geofences = gf
                DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                    completion(.geofences(gf))
                }
            }
        }
    }
    
    func fetchParkings(completion: @escaping (RideState) -> ()) {
        api.getParkings(by: bike.fleetId, bikeId: bike.bikeId, coordinate: manager.location?.coordinate) { (result) in
            switch result {
            case .success(let parking):
                completion(.parking(parking))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    @objc
    func endRide(parking: Bool = true, force: Bool = false, damageReported: Bool = false) {
        if manager.endTrip(parking: parking, force: force, damageReported: damageReported) {
            tripIdToEndRide = nil
            endingRide = true
            stateHandler(.processing("end_ride_loader".localized(), nil))
        }
    }
    
    func undock() {
        guard let _ = bike.adapterId else { return }
        stateHandler(.processing("loading".localized(), nil))
        api.undock(vehicle: bike) { [weak self] (result) in
            self?.stateHandler(.processing(nil, {
                switch result {
                case .success:
                    self?.tripIdToHandleDocking = nil
                    self?.tripIdToUnlock = nil
                    self?.manager.deviceManager.unlock()
                case .failure(let error):
                    self?.stateHandler(.failure(error))
                }
            }))
        }
    }
}

