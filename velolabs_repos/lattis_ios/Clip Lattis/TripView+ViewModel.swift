//
//  TripView+ViewModel.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 08.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import SwiftUI
import CoreLocation


extension TripView {
    final class ViewModel: NSObject, ObservableObject {
        
        @Published var vehicle: Vehicle?
        @Published var duration: String = "Duration: --"
        @Published var secure: Bool = true
        @AppStorage("vehicle-id")
        fileprivate var vehicleId: Int?
        @AppStorage("trip-id")
        fileprivate var tripId: Int?
        fileprivate let manager = CLLocationManager()
        fileprivate var location: CLLocation?
        fileprivate(set) var currentStatus: CurrentStatus = .loading
        fileprivate let onEnd: () -> Void
        
        var vehicleName: String {
            vehicle?.name ?? "No name"
        }
        
        var fleetName: String {
            vehicle?.fleet.name ?? "No name"
        }
        
        init(_ completion: @escaping () -> Void) {
            self.onEnd = completion
            super.init()
            manager.delegate = self
            if manager.authorizationStatus == .notDetermined {
                manager.requestWhenInUseAuthorization()
            } else {
                manager.startUpdatingLocation()
            }
            fetchVehicle()
        }
        
        func startTrip() {
            Task {
                do {
                    if let id = tripId {
                        let trip = try await OvalAPI.trip(id: id)
                        vehicleId = trip.bike_id
                        fetchVehicle()
                        self.currentStatus = .started
                    } else if let id = vehicleId, let coordinate = location?.coordinate {
                        let trip = try await OvalAPI.startTrip(info: .init(bike_id: id, latitude: coordinate.latitude, longitude: coordinate.longitude))
                        self.tripId = trip.trip_id
                        self.currentStatus = .started
                    }
                } catch {
                    print(error)
                }
            }
        }
        
        func endTrip() {
            guard let coordinate = location?.coordinate, let accuracy = location?.horizontalAccuracy, let id = tripId else { return }
            Task {
                do {
                    try await OvalAPI.endTrip(info: .init(trip_id: id, latitude: coordinate.latitude, longitude: coordinate.longitude, accuracy: accuracy))
                    tripId = nil
                    vehicleId = nil
                    onEnd()
                } catch {
                    print(error)
                }
            }
        }
        
        func fetchVehicle() {
            guard let id = vehicleId else { return }
            Task {
                do {
                    self.vehicle = try await CircleAPI.vehicle(id)
                    self.connect()
                } catch {
                    print(error)
                }
            }
        }
        
        func connect() {
            guard let id = vehicleId, let key = vehicle?.things?.first?.key else { return }
            Task {
                do {
                    let status = try await OvalAPI.iotStatus(id: id, controller: key)
                    self.secure = status.locked
                } catch {
                    print(error)
                }
            }
        }
        
        func toggleLock() {
            guard let id = vehicleId, let key = vehicle?.things?.first?.key else { return }
            Task {
                do {
                    if secure {
                        try await OvalAPI.unlock(id: id, controller: key)
                        self.secure = false
                    } else {
                        try await OvalAPI.lock(id: id, controller: key)
                        self.secure = true
                    }
                } catch {
                    print(error)
                }
            }
        }
    }
    
    enum CurrentStatus {
        case loading
        case started
    }
}

extension TripView.ViewModel: CLLocationManagerDelegate {
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        guard manager.authorizationStatus == .authorizedWhenInUse else { return }
        manager.startUpdatingLocation()
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let shouldStart = location == nil
        location = locations.last
        if shouldStart {
            startTrip()
        }
    }
}
