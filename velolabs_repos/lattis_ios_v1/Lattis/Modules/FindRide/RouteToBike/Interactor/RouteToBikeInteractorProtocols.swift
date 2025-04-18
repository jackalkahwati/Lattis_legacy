//
//  RouteToBikeRouteToBikeInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import CoreLocation

protocol RouteToBikeInteractorInput {
    var isLockConnected: Bool {get}
    var location: CLLocation {get set}
    func startBooking()
    func beginTrip()
    func trackUnconnectedBegin()
    func cancelTrip()
    func performCancel()
    func openMenu()
    func suspend()
    func openInfo()
    func checkBLE()
}

protocol RouteToBikeInteractorOutput: BaseInteractorOutput {
    func update(time: String, for bike: Bike)
    func timeExpired()
    func buildRoute(to bike: Bike)
    func hideSpinner()
    func connecting()
    func connected()
    func disconnected()
    func update(tripTime: String?, fare: String?)
    func showCancelWarning(tripStarted: Bool)
    func showBLEWarning()
    func hideWarnings()
}
