//
//  NavigationNavigationInteractorProtocols.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 15/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreLocation

protocol NavigationInteractorInput {
    func start()
    func select(ellipse: Ellipse)
    func unselect()
    func getDirection()
    var userLocation: CLLocationCoordinate2D? {get set}
}

protocol NavigationInteractorOutput: InteractorOutput {
    func show(locks: [Ellipse])
    func show(ellipse: Ellipse)
    func show(direction: Direction)
    func addCloseButton()
}
