//
//  RideRideInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 22/02/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Mapbox

protocol RideInteractorInput {
    var needShowHint: Bool {get}
    var isLockLocked: Bool {get}
    var isBluetoothEnabled: Bool {get}
    var shouldFollowUser: Bool { get }
    var canForceEndRide: Bool { get set }
    var location: CLLocation { get set }
    func searchParkings()
    func selectParking(with annotation: MapAnnotation)
    func routeToSelectedParking()
    func unselectParking()
    func openMenu()
    func viewDidLoad()
    func startCount()
    func openDirections() 
    func stopCount()
    func endRide(forced: Bool)
    func openDamage()
    func openTheft()
    func suspend()
    func set(lockState: LockButton.LockState) -> Bool
    func connectLock()
    func openBikeDetails()
    func openPayments()
    func stopSearchParkings()
}

protocol RideInteractorOutput: BaseInteractorOutput {
    func show(_ annotations: [MapAnnotation])
    func show(zones: [ParkingZone])
    func buildRoute(to annotation: MapAnnotation)
    func update(_ time: String)
    func show(lockState: LockButton.LockState)
    func showHint(text: String)
    func show(parkingCheck: Parking.Check)
    func show(update: Trip.Update)
    func show(bike: Bike)
    func handleJamming()
}
