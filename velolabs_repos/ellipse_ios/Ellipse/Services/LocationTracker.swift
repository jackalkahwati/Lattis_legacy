//
//  LocationTracker.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/17/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import LattisSDK
import CoreLocation

class LocationTracker: NSObject {
    static let shared = LocationTracker()
    
    fileprivate let locationManager = CLLocationManager()
    fileprivate let storage: EllipseStorage = CoreDataStack.shared
    
    fileprivate var lockTimer: Timer?
    fileprivate var locationTimer: Timer?
    fileprivate var userLocation: CLLocation?
    fileprivate var state: Ellipse.LockState?
    fileprivate var date: Date?
    fileprivate var lock: Ellipse.Device?
    
    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.requestWhenInUseAuthorization()
    }
    
    func track(lock: Ellipse.Device) {
        lockTimer?.invalidate()
        date = nil
        state = nil
        lock.peripheral.subscribe(self)
        self.lock = lock
    }
    
    fileprivate func saveLocation() {
        locationManager.startUpdatingLocation()
        lockTimer?.invalidate()
        lockTimer = Timer.scheduledTimer(withTimeInterval: 5, repeats: false, block: { [weak self] (_) in
            self?.locationManager.stopUpdatingLocation()
            self?.write()
        })
    }
    
    func write() {
        guard let coordinane = userLocation?.coordinate, var ellipse = lock?.ellipse, let state = self.state else { return }
        ellipse.lockState = state
        ellipse.stateChangedAt = date ?? Date()
        ellipse.coordinate = coordinane
        storage.save(ellipse)
    }
    
    func location(completion: @escaping (CLLocation) -> ()) {
        locationManager.startUpdatingLocation()
        lockTimer = Timer.scheduledTimer(withTimeInterval: 5, repeats: false, block: { [unowned self] (_) in
            guard let location = self.userLocation else { return }
            completion(location)
            self.lockTimer?.invalidate()
            self.locationManager.stopUpdatingLocation()
        })
    }
}

extension LocationTracker: EllipseDelegate {
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {
        date = Date()
        switch security {
        case .locked where self.state != .locked:
            self.state = .locked
            saveLocation()
        case .unlocked where self.state != .unlocked:
            self.state = .unlocked
            saveLocation()
        default:
            break
        }
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
        
    }
}

extension LocationTracker: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        userLocation = locations.first
    }
}
