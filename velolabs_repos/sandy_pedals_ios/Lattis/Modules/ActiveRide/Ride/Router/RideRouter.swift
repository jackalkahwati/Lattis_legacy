//
//  RideRideRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 22/02/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import Cartography
import LGSideMenuController

final class RideRouter: BaseRouter {
    fileprivate let delegate = MenuDelegate()
    class func push(in navigation: MapRepresenting, replace: Bool, configure: (RideInteractor) -> () = {_ in}) {
        let controller = RideViewController()
        let interactor = inject(controller: controller)
        interactor.router.mapNavigation = navigation
        configure(interactor)
        navigation.push(controller, animated: true, replace: replace)
        controller.sideMenuController?.delegate = interactor.router.delegate
    }
    
    fileprivate weak var mapNavigation: MapRepresenting!
    
    func openEndRide(configure: (EndRideInteractor) -> ()) {
        let controller = EndRideRouter.navigation(configure: configure)
        self.controller.present(controller, animated: true, completion: nil)
    }
    
    func openRideSummary(trip: Trip, delegate: EndRideInteractorDelegate) {
        let summary = EndRideRouter.instantiate(type: .action) { (interactor) in
            interactor.trip = trip
            interactor.delegate = delegate
        }
        self.controller.present(summary, animated: true, completion: nil)
    }
    
    func openFind() {
        mapNavigation.clearSelection()
        mapNavigation.followUser = false
        FindRideRouter.push(in: mapNavigation, replace: true)
        AppRouter.shared.onStart(true)
    }
    
    func openDamage(configure: (DamageInteractor) -> () = { _ in }) {
        let damage = DamageRouter.navigation(configure: configure)
        controller.present(damage, animated: true, completion: nil)
    }
    
    func openTheft(for bike: Bike) {
        let theft = TheftRouter.navigation() { $0.bike = bike }
        controller.present(theft, animated: true, completion: nil)
    }
    
    func openDirections(with delegate: DirectionsInteractorDelegate) {
        let directions = DirectionsRouter.navigation(with: delegate, currentAllowed: false)
        controller.present(directions, animated: true, completion: nil)
    }
    
    func dismiss() {
        controller.dismiss(animated: true, completion: nil)
    }
    
    func showWalkthrough() {
        let walk = UIStoryboard(name: "WalkthroughViewController", bundle: nil).instantiateInitialViewController() as! WalkthroughViewController
        walk.willMove(toParent: controller)
        controller.addChild(walk)
        walk.view.alpha = 0
        controller.view.addSubview(walk.view)
        constrain(walk.view) { (view) in
            view.edges == view.superview!.edges
        }
        walk.didMove(toParent: controller)
        UIView.animate(withDuration: .defaultAnimation) { 
            walk.view.alpha = 1
        }
    }
    
    func openInfo(bike: Bike) {
        let info = BikeInfoRouter.navigation { $0.bike = bike }
        controller.present(info, animated: true, completion: nil)
    }
    
    func openPayments() {
        let billing = PaymentMethodRouter.navigation(accessory: .select)
        controller.present(billing, animated: true, completion: nil)
    }
}

private func inject(controller: RideViewController) -> RideInteractor {
    let interactor = RideInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = RideRouter(controller)
    return interactor
}
