//
//  VehicleStatusViewModel.swift
//  Operator
//
//  Created by Ravil Khusainov on 02.07.2021.
//

import Foundation
import Combine
import CoreLocation
import SwiftUI

protocol VehiclePatcher {
    var currentVehicle: Vehicle? { get }
    func updateVehicle(patch: Vehicle.Patch)
}

final class VehicleStatusViewModel: NSObject, ObservableObject {
    
    let patcher: VehiclePatcher
    var vehicle: Vehicle? { patcher.currentVehicle }
    @Published var status: Vehicle.Status? {
        didSet {
            usage = nil
        }
    }
    @Published var usage: Vehicle.Usage?
    
    let statusList: [Vehicle.Status] = [.active, .inactive, .suspended, .deleted]
    let usageList: [Vehicle.Status: [Vehicle.Usage]] = [
        .active: [.parked, .on_trip, .reserved, .collect],
        .inactive: [.lock_assigned, .lock_not_assigned, .balancing],
        .suspended: [.damaged, .under_maintenance, .reported_stolen, .transport],
        .deleted: [.total_loss, .stolen, .defleet]
    ]
    fileprivate let locationManager = CLLocationManager()
    fileprivate var location: CLLocation?
    
    init(_ patcher: VehiclePatcher) {
        self.patcher = patcher
        super.init()
        status = vehicle?.metadata.status
        usage = vehicle?.metadata.usage
        locationManager.delegate = self
        locationManager.startUpdatingLocation()
    }
    
    func valid() -> Bool {
        guard let vehicle = vehicle else { return true }
        guard let status = status, let usage = usage else { return false }
        return status != vehicle.metadata.status || usage != vehicle.metadata.usage
    }
    
    func disabled(usage: Vehicle.Usage) -> Bool {
        return usage.in([.on_trip, .lock_not_assigned, .controller_assigned, .reserved])
    }
    
    func save() {
        guard let status = status, var usage = usage else { return }
        var maintenance: Vehicle.Maintenance? = nil
        if usage == .lock_assigned && vehicle == nil {
            usage = .controller_assigned
        } else if usage == .lock_assigned && vehicle?.metadata.ellipse == nil && !vehicle!.things.isEmpty {
            usage = .controller_assigned
        }
        if status == .inactive || status == .suspended {
            maintenance = .shop_maintenance
        }
        var location: CLLocation?
        if status == .active {
            location = self.location
        }
        patcher.updateVehicle(patch: .init(status: status, usage: usage, maintenance: maintenance, coordinate: location?.coordinate))
    }
}

extension VehicleStatusViewModel: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        location = locations.last
    }
}

extension Hashable {
    func `in`(_ sequence: Array<Self>) -> Bool {
        sequence.contains(self)
    }
}

