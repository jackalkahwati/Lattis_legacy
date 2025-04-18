//
//  DispatchDispatchInteractor.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import LattisSDK
import Oval
import CoreLocation

class DispatchInteractor: NSObject {
    weak var view: DispatchInteractorOutput!
    var router: DispatchRouter!
    var lock: Lock!
    var fromRoot: Bool = false
    fileprivate(set) var isLockLocked: Bool = false
    
    fileprivate var state: BikeState = .outOfService
    fileprivate let ble = EllipseManager.shared
    typealias Network = EllipseNetwork & BikeNetwork
    fileprivate let network: Network
    fileprivate let locationManager = CLLocationManager()
    fileprivate var coordinate = kCLLocationCoordinate2DInvalid
    init(network: Network = Session.shared) {
        self.network = network
        super.init()
        locationManager.requestWhenInUseAuthorization()
        locationManager.delegate = self
        locationManager.startUpdatingLocation()
    }
    
    deinit {
        locationManager.stopUpdatingLocation()
    }
}

extension DispatchInteractor: DispatchInteractorInput {
    func viewLoaded() {
        view.show(lock: lock)
        lock.peripheral?.subscribe(self)
        if fromRoot == false {
            view.showBack()
        }
    }
    
    func isCurrent(state: BikeState) -> Bool {
        switch state {
        case .outOfService: return true
        default: return false
        }
    }
    
    func select(state: BikeState) {
        guard CLLocationCoordinate2DIsValid(coordinate) else {
            return view.showAlert(title: nil, subtitle: "warning_dispatch_no_gps_text".localized())
        }
        self.state = state
        guard let bike = lock.lock?.bikeId else { return }
        view.startLoading(title: nil)
        network.updae(state: state, with: coordinate, for: bike) { [weak self] result in
            switch result {
            case .success:
                self?.finish()
            case .failure(let e):
                self?.view.show(error: e)
            }
        }
    }
    
    func set(lockState: LockSlider.LockState) {
        if lockState.isLocked {
            lock.peripheral?.lock()
        } else {
            lock.peripheral?.unlock()
        }
    }
    
    func finish() {
        view.stopLoading {}
        lock.peripheral?.disconnect()
        router.pop(root: true)
    }
}

extension DispatchInteractor: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.first {
            coordinate = location.coordinate
        }
    }
}

extension DispatchInteractor: EllipseDelegate {
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
        
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {
        isLockLocked = false
        switch security {
        case .invalid, .middle:
            view.showAlert(title: "lock_jamming_title".localized(), subtitle: "lock_jamming_text".localized())
            network.send(metadata: .ellipse(lock, true)) { _ in }
        case .locked:
            isLockLocked = true
            fallthrough
        default:
            view.update(state: security.slider)
        }
    }
}

extension Peripheral.Security {
    var slider: LockSlider.LockState {
        switch self {
        case .locked:
            return .locked
        default:
            return .unlocked
        }
    }
}
