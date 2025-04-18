//
//  AxaLocksViewModel.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 16.03.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import Foundation
import AXALock
import Oval
import CoreLocation

final class AxaLocksViewModel: NSObject {
    var insert: ((IndexPath) -> ())? = nil
    var updade: (() -> ())? = nil
    var reload: ((IndexPath) -> ())? = nil
    var failure: ((Error) -> ())? = nil
    var empty: ((Bool) -> ())? = nil
    fileprivate(set) var filter: Lock.Filter
    fileprivate(set) var devices: [AxaDevice] = []
    fileprivate var locks = AxaBLE.Lock.all
    fileprivate var iotModules = [IoTModule]()
    fileprivate var bikes = [Bike]()
    fileprivate let handler = AxaBLE.Handler()
    fileprivate let network: IoTNetwork & BikeNetwork = Session.shared
    fileprivate let storage: FleetsStorage = CoreDataStack.shared
    fileprivate let locationManager = CLLocationManager()
    fileprivate var currentLocation: CLLocation?
    
    init(filter: Lock.Filter) {
        self.filter = filter
        super.init()
        handler.discovered = { [unowned self] lock in
            self.didFind(lock: lock)
        }
        AxaBLE.Lock.scan(with: handler)
        locationManager.delegate = self
        locationManager.startUpdatingLocation()
    }
    
    func onboard(device: AxaDevice) {
        devices.insert(device, at: 0)
        insert?(.init(row: 0, section: 0))
    }
    
    func change(filter: Lock.Filter) {
        guard filter != self.filter else { return }
        self.filter = filter
        calculate(filter: true)
    }
    
    func fetchModules() {
        network.fetch(query: .vendor(.AXA)) { [weak self] (result) in
            switch result {
            case .success(let mod):
                self?.iotModules = mod
                self?.fetchBikes()
            case .failure(let error):
                self?.failure?(error)
            }
        }
    }
    
    func fetchBikes() {
        guard let fleet = storage.currentFleet else { return print("Fuck off") }
        network.getBikes(for: fleet) { [weak self] (result) in
            switch result {
            case .success(let bikes):
                self?.bikes = bikes
                self?.calculate()
            case .failure(let error):
                self?.failure?(error)
            }
        }
    }
    
    func connect(lockAt indexPath: IndexPath, completion: @escaping (AxaDevice) -> ()) {
        let device = devices[indexPath.row]
        let lock = device.lock
        guard lock.connection != .paired else {
            completion(device)
            return
        }
        handler.connectionChanged = { [unowned self] l in
            guard l == lock else { return }
            switch l.connection {
            case .paired:
                 completion(device)
            default:
                break
            }
            self.reload?(indexPath)
        }
        lock.connect(with: handler)
    }
    
    func sendOutOfService(device: AxaDevice) {
        guard let bike = device.bike, let location = currentLocation else { return }
        network.updae(state: .outOfService, with: location.coordinate, for: bike.bikeId) { (result) in
            switch result {
            case .failure(let error):
                report(error: error)
            case .success:
                print("Bike \(bike.bikeId) is out of service")
            }
        }
    }
    
    fileprivate func didFind(lock: AxaBLE.Lock) {
        locks.append(lock)
        calculate()
    }
    
    fileprivate func calculate(filter: Bool = false) {
        if filter {
            devices.removeAll()
        }
        for module in iotModules {
            guard let lock = locks.first(where: \.id, isEqual: module.key) else { continue }
            let bike = bikes.first(where: {$0.bikeId == module.bikeId})
            let device = AxaDevice(lock: lock, module: module, bike: bike)
            switch self.filter {
            case .bike where bike == nil:
                continue
            case .noBike where bike != nil:
                continue
            default:
                print("Filter passed")
            }
            if let idx = devices.firstIndex(where: {$0.lock.id == lock.id}) {
                devices[idx] = device
                guard !filter else { continue }
                reload?(IndexPath(row: idx, section: 0))
            } else {
                devices.append(device)
                guard !filter else { continue }
                insert?(IndexPath(row: devices.count - 1, section: 0))
            }
        }
        if filter {
            updade?()
        }
        empty?(devices.isEmpty)
    }
}

extension AxaLocksViewModel: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        currentLocation = locations.first
    }
}
