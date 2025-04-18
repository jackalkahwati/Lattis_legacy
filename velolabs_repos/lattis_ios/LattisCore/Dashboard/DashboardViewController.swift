//
//  DashboardViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 15/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import CoreLocation
import Model

protocol DashboardDelegate: AnyObject {
    func didChange(status: Status, info: Status.Info?, animated: Bool)
    func shouldCheckStatus()
    func update(state: AssetDashboardView.Card, asset: Asset)
}

public class DashboardViewController: UIViewController, OverMap {

    public weak var mapController: MapRepresentable?
    public weak var topController: MapTopViewController?
    fileprivate var navigation: UINavigationController!
    fileprivate var status: Status = .loading
    fileprivate var network: UserAPI & TripAPI & BikeAPI & ReservationsNetwork = AppRouter.shared.api()
    fileprivate let transition = AlphaTransition()
    fileprivate let notifierThreshhold: TimeInterval = 30.minutes
    
    public init() {
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .clear
        view.clipsToBounds = true
        let status = CurrentStateViewController()
        status.delegate = self
        navigation = UINavigationController(rootViewController: status)
        navigation.delegate = self
        navigation.willMove(toParent: self)
        addChild(navigation)
        view.addSubview(navigation.view)
        navigation.didMove(toParent: self)
        navigation.setNavigationBarHidden(true, animated: false)
        constrain(navigation.view, view) { nav, view in
            nav.edges == view.edges
        }
          
        AppRouter.shared.dashboard = self
        
        NotificationCenter.default.addObserver(self, selector: #selector(handleTrip(notification:)), name: .tripStarted, object: nil)
    }
    
    fileprivate func handle(status: Status, animated: Bool = true) {
        self.status = status
        switch status {
        case .booking(let booking, let bike):
            let controller = RideReserveViewController(booking, bike: bike)
            controller.delegate = self
            navigation.setViewControllers([controller], animated: animated)
        case .trip(let tripService):
            let controller = RideViewController(tripService)
            controller.delegate = self
            navigation.setViewControllers([controller], animated: animated)
        case .search:
            let search = RideSearchViewController()
            search.delegate = self
            navigation.setViewControllers([search], animated: animated)
            topController = search
            search.mapController = mapController
            mapController?.topController = search
            search.didLayoutSubviews()
        case .loading:
            let loading = CurrentStateViewController()
            loading.delegate = self
            navigation.setViewControllers([loading], animated: animated)
        case .modern:
            print("This case handled by SwiftUI")
        }
    }
    
    @objc
    fileprivate func handleTrip(notification: Notification) {
        guard let trip = notification.object as? Trip, let bike = trip.bike else { return }
        handle(status: .trip(.init(trip, bike: bike, unlock: true)))
        scheduleTripEndingNotifier(for: trip)
    }

    fileprivate func scheduleTripEndingNotifier(for trip: Trip) {

        guard let tripReservationEndDate = trip.reservationEnd
            else {return}

        // let diff = tripReservationEndDate.timeIntervalSinceNow
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        guard let formatted = formatter.date(from: tripReservationEndDate) else { return }
        let diff = formatted.timeIntervalSinceNow

        let content = UNMutableNotificationContent()
        content.title = "general_error_title".localized()
        content.subtitle = "reservation_ending_soon".localized()
        content.userInfo["trip_id"] = trip.tripId
        content.sound = UNNotificationSound.default

        if diff > notifierThreshhold {
            let trigger = UNTimeIntervalNotificationTrigger(timeInterval: diff - notifierThreshhold, repeats: false)
            let request = UNNotificationRequest(identifier: Notification.Name.reservationEndingSoon.rawValue, content: content, trigger: trigger)

            // add our notification request
            UNUserNotificationCenter.current().add(request)
        }
    }

    public func didLayoutSubviews() {}
}

extension DashboardViewController: DashboardDelegate {
    func didChange(status: Status, info: Status.Info?, animated: Bool) {
        guard self.status != status else { return }
        AppRouter.shared.onInfoUpdate(info, status)
        handle(status: status, animated: animated)
    }
    
    func shouldCheckStatus() {}
    
    func update(state: AssetDashboardView.Card, asset: Asset) {
        if let vc = navigation.viewControllers.first as? RideSearchViewController {
            vc.mapController = nil
        }
        let dash = AssetDashboardViewController.init(mapController, viewModel: .init(asset, delegate: self, map: mapController, card: state))
        navigation.setViewControllers([dash], animated: true)
        status = .modern
    }
}

extension DashboardViewController: UINavigationControllerDelegate {
    public func navigationController(_ navigationController: UINavigationController, animationControllerFor operation: UINavigationController.Operation, from fromVC: UIViewController, to toVC: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        return transition
    }
    
    public func navigationController(_ navigationController: UINavigationController, didShow viewController: UIViewController, animated: Bool) {
        if let top = viewController as? MapTopViewController {
            topController = top
            top.mapController = mapController
            mapController?.topController = top
            top.didLayoutSubviews()
        }
    }
}

extension Status: Equatable {
    static func == (lhs: Status, rhs: Status) -> Bool {
        switch (lhs, rhs) {
        case (.search, .search):
            return true
        case (.booking, .booking):
            return true
        case (.trip, .trip):
            return true
        default:
            return false
        }
    }
}
