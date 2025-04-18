//
//  RouteToBikeRouteToBikeRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import CoreLocation
import Mapbox

final class RouteToBikeRouter: BaseRouter {
    class func push(in navigation: MapRepresenting, configure: (RouteToBikeInteractor) -> () = {_ in}) {
        let controller = RouteToBikeViewController(nibName: "RouteToBikeViewController", bundle: nil)
        let interactor = inject(controller: controller)
        interactor.router.mapNavigation = navigation
        configure(interactor)
        navigation.push(controller, animated: true, replace: false)
    }
    
    fileprivate weak var mapNavigation: MapRepresenting!
    
    func openRide(with bike: Bike, in location: CLLocationCoordinate2D, onStart: @escaping (RideInteractor) -> (), onFail: @escaping (Error) -> ()) {
        let service = TripService(bike)
        service.onStart = { [unowned self] in
            RideRouter.push(in: self.mapNavigation, replace: true) { interactor in
                onStart(interactor)
                interactor.tripService = service
            }
        }
        service.onFail = onFail
        service.start(with: location)
    }
    
    func openRide(with service: TripService, lock: Lock, in location: CLLocationCoordinate2D) {
        RideRouter.push(in: self.mapNavigation, replace: true) { interactor in
            interactor.lock = lock
            interactor.tripService = service
        }
        service.start(with: location)
    }
    
    func pop() {
        guard let coordinate = mapNavigation.mapView.userLocation?.coordinate else { return }
        let camera = MGLMapCamera(lookingAtCenter: coordinate, fromDistance: 4500, pitch: 15, heading: 0)
        mapNavigation.mapView.setCamera(camera, animated: true)
        mapNavigation.stopNavigation()
        _ = mapNavigation.pop(animated: true)
        AppRouter.shared.searchBike()
    }
    
    func openInfo(bike: Bike) {
        let info = BikeInfoRouter.navigation { $0.bike = bike }
        controller.present(info, animated: true, completion: nil)
    }
    
    func openSummary(trip: Trip, delegate: EndRideInteractorDelegate) {
        let summary = EndRideRouter.instantiate(type: .action, configure: {$0.delegate = delegate; $0.trip = trip})
        controller.present(summary, animated: true, completion: nil)
    }
    
    func dismiss() {
        controller.dismiss(animated: true, completion: nil)
    }
    
    func addPhoneNumber() {
        guard let coordinate = mapNavigation.mapView.userLocation?.coordinate else { return }
        let camera = MGLMapCamera(lookingAtCenter: coordinate, fromDistance: 4500, pitch: 15, heading: 0)
        mapNavigation.mapView.setCamera(camera, animated: true)
        mapNavigation.stopNavigation()
        _ = mapNavigation.pop(animated: true)
        AppRouter.shared.addPhoneNumber()
        AppRouter.shared.onStart(true)
    }
}

private func inject(controller: RouteToBikeViewController) -> RouteToBikeInteractor {
    let interactor = RouteToBikeInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = RouteToBikeRouter(controller)
    return interactor
}
