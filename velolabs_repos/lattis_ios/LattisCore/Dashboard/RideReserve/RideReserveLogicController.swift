//
//  RideReserveLogicController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 05.05.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import Wrappers
import CoreLocation
import Model

enum RideReserveState {
    case label(String)
    case time(String?)
    case qrCode
    case bleState(Bool)
    case connection(Device.Connection)
    case trip(TripManager)
    case cancel
    case loading(String?)
    case failure(Error)
}

final class RideReserveLogicController {
    typealias Handler = (RideReserveState) -> ()
    let manager: DeviceManager
    let booking: Bike.Booking
    let bike: Bike
    var location: CLLocation?
    
    var isKuhmuteEnabled: Bool { bike.adapterId != nil && manager.list.map(\.kind).contains(.adapter) }
    
    fileprivate let api: BikeAPI & TripAPI & UserAPI & HubsAPI = AppRouter.shared.api()
    fileprivate let timeFormatter = DateComponentsFormatter()
    fileprivate var timer: Timer?
    fileprivate var secondsLeft: TimeInterval = 0
    @UserDefaultsBacked(key: "isTripCanceled", defaultValue: false)
    fileprivate var isTripCanceled: Bool
    fileprivate var stateHandler: Handler = {_ in}
    fileprivate var stateString = "reserved_label".localized() {
        didSet { stateHandler(.label(stateString)) }
    }
    fileprivate var connection: Device.Connection = .disconnected {
        didSet {
            if connection != oldValue {
                stateHandler(.connection(connection))
            }
        }
    }
    
    init(_ booking: Bike.Booking, bike: Bike, manager: DeviceManager) {
        self.booking = booking
        self.bike = bike
        self.manager = manager
        timeFormatter.allowedUnits = [.minute, .second]
        timeFormatter.unitsStyle = .positional
        
        secondsLeft = booking.deadline.timeIntervalSinceNow
        
        NotificationCenter.default.addObserver(self, selector: #selector(stopCountdown), name: UIApplication.didEnterBackgroundNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(restartCountdown), name: UIApplication.willEnterForegroundNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(device(notification:)), name: .deviceStatusUpdated, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(cancel), name: .cancelBooking, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    func fetchState(completion: @escaping Handler) {
        stateHandler = completion
        stateHandler(.label(stateString))

        manager.connect { _ in
            completion(.connection(.connected))
        }
        completion(.connection(manager.connection))

        if secondsLeft > .hour {
            timeFormatter.allowedUnits = [.hour, .minute, .second]
        } else {
            timeFormatter.allowedUnits = [.minute, .second]
        }
        updateTimerUI()
        startCountdown()
    }
    
    @objc
    func stopCountdown() {
        timer?.invalidate()
    }
    
    @objc
    func cancel() {
        stateHandler(.loading("booking_cancel_loading".localized()))
        api.cancelBooking(with: bike.unbook()) { [weak self] result in
            switch result {
            case .success:
                self?.manager.disconnect()
                self?.stopCountdown()
                self?.stateHandler(.cancel)
            case .failure(let error):
                self?.stateHandler(.failure(error))
            }
        }
    }
    
    func startTrip() {
        guard let coordinate = location?.coordinate else { return stateHandler(.failure(Trip.Fail.noLocation)) }
        stateHandler(.loading("starting_ride_loader".localized()))
        func start() {
            TripManager.startTrip(booking, bike: bike, coordinate: coordinate, deviceManager: manager, completion: { [weak self] (reslut) in
                switch reslut {
                case .failure(let error):
                    self?.startCountdown()
                    self?.stateHandler(.failure(error))
                case .success(let manager):
                    self?.stateHandler(.trip(manager))
                }
            })
        }
        
        if let _ = bike.adapterId {
            api.undock(vehicle: bike) { [weak self] (result) in
                switch result {
                case .failure(let error):
                    self?.startCountdown()
                    self?.stateHandler(.failure(error))
                case .success:
                    if self?.manager.connection == .connected {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                            self?.manager.unlock()
                            start()
                        }
                    } else {
                        self?.manager.connect(completion: { error in
                            if error != nil {
                                self?.stateHandler(.failure(error!))
                            } else {
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                                    self?.manager.unlock()
                                    start()
                                }
                            }
                        })
                    }
                }
//                start()
            }
        } else {
            start()
        }
    }
    
    func startCountdown() {
        if let timer = timer, timer.isValid { return }
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] (_) in
            self?.decrement()
        }
    }
    
    fileprivate func decrement() {
        secondsLeft -= 1
        updateTimerUI()
        if secondsLeft <= 0 {
            timerExpired()
        }
    }
    
    fileprivate func timerExpired() {
        stopCountdown()
        if bike.isPayment {
            stateHandler(.loading("starting_ride_loader".localized()))
            
        } else {
            stateHandler(.loading("finding_rides".localized()))
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 3, execute: checkStatus)
    }
    
    fileprivate func checkStatus() {
        api.checkStatus { [weak self] (result) in
            switch result {
            case .failure(let error):
                print(error)
            case .success(let info):
                self?.handle(status: info)
            }
        }
    }
    
    fileprivate func handle(status: Status.Info) {
        if let tr = status.trip {
            api.getTrip(by: tr.tripId) { [weak self] (result) in
                switch result.unwrap(\.trip) {
                case .failure(let e):
                    self?.stateHandler(.failure(e))
                case .success(let trip):
                    guard let `self` = self else { return }
                    self.stateHandler(.loading(nil))
                    self.stateHandler(.trip(.init(trip, bike: self.bike, api: self.api, device: self.manager)))
                }
            }
        } else {
            stateHandler(.loading(nil))
            isTripCanceled = true
            stateHandler(.cancel)
        }
    }
    
    fileprivate func updateTimerUI() {
        stateHandler(.time(timeFormatter.string(from: secondsLeft)))
    }
    
    @objc
    fileprivate func restartCountdown() {
        secondsLeft = booking.deadline.timeIntervalSinceNow
        if secondsLeft <= 0 {
            secondsLeft = 0
            updateTimerUI()
            timerExpired()
        } else {
            startCountdown()
        }
    }
    
    @objc
    fileprivate func device(notification: Notification) {
        guard let _ = notification.object as? DeviceRepresenting else { return }
        connection = manager.connection
    }
}
