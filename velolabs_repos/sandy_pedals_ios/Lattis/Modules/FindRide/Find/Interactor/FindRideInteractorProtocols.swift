//
//  FindRideInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 07/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import Mapbox
import QRCodeReader

typealias FindRideInteractorDelegate = QRCodeReaderViewControllerDelegate & FindQRViewDelegate

protocol FindRideInteractorInput {
    func viewLoaded()
    func update(userCoordinate: CLLocationCoordinate2D)
    func search()
    func bookSelectedBike()
    func selectedBikeInfo()
    func selectBike(with annotation: MapAnnotation)
    func unselectBike()
    func openMenu()
    func openTerms(with link: URL)
    func addPrivateNetwork()
    func scanQRCode()
    func choosePickUp()
}

protocol FindRideInteractorOutput: MapContaining {
    func show(annotations: [MapAnnotation])
    func show(result: Bike.Search, userLocation: Direction?)
    func show(userLocationTitle: String)
    func showQR(bike: Bike)
    func closeSelection()
}


