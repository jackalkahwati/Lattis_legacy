//
//  ParkingZonesViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 07/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import CoreLocation
import Cartography

class ParkingZonesViewController: MapTopViewController {
    
    fileprivate let fleetId: Int
    fileprivate let network: ParkingAPI = AppRouter.shared.api()
    
    fileprivate var parkingBottomToView: NSLayoutConstraint!
    fileprivate var parkingBottomToSafeArea: NSLayoutConstraint!
    fileprivate var parkingLeft: NSLayoutConstraint!
    fileprivate var parkingRight: NSLayoutConstraint!
    fileprivate var homeToParking: NSLayoutConstraint!
    fileprivate var homeToCard: NSLayoutConstraint!
    fileprivate let parkingView = ParkingView()
    
    init(_ fleetId: Int) {
        self.fleetId = fleetId
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        menuButton?.removeFromSuperview()
        
        addCloseButton()
        
        cardView.isHidden = true
        footerView.isHidden = true
        view.addSubview(parkingView)
        parkingView.closeButton.addTarget(self, action: #selector(unselectParking(_:)), for: .touchUpInside)
        
        constrain(parkingView, mapHomeButton, view) { parking, home, view in
            self.parkingLeft = parking.left == view.left + .margin/2
            self.parkingRight = parking.right == view.right - .margin/2
            self.parkingBottomToSafeArea = parking.bottom == view.safeAreaLayoutGuide.bottom ~ .defaultHigh
            self.parkingBottomToView = parking.bottom == view.bottom ~ .defaultLow
            self.homeToParking = home.bottom == parking.top - .margin/2 ~ .defaultLow
        }
        
        refresh()
    }
    
    fileprivate func refresh() {
        network.getParkings(by: fleetId, bikeId: nil, coordinate: nil) { [weak self] (result) in
            switch result {
            case .success(let parking):
                self?.show(parking: parking)
            case .failure(let error):
                Analytics.report(error)
            }
        }
    }
    
    fileprivate func show(parking: Parking) {
        func calculate() -> [CLLocationCoordinate2D] {
            var coordinates: [CLLocationCoordinate2D] = []
            for zone in parking.zones {
                switch zone.shape {
                case .polygon(let coord), .rectangle(let coord):
                    coordinates += coord
                case .circle(let circle):
                    coordinates += circle.coordinates
                }
            }
            coordinates += parking.spots.map(\.coordinate)
            if let user = mapController?.location {
                coordinates.append(user.coordinate)
            }
            return coordinates
        }
        
        mapController?.focus(on: calculate())
        
        mapController?.add(points: parking.spots, selected: nil)
        mapController?.add(shapes: parking.zones)
    }
    
    fileprivate func select(spot: Parking.Spot) {
        parkingView.parking = spot
        parkingBottomToView.priority = .defaultHigh
        parkingBottomToSafeArea.priority = .defaultLow
        parkingLeft.constant = 0
        parkingRight.constant = 0
        UIView.animate(withDuration: 0.3, animations: view.layoutIfNeeded)
    }
    
    @objc
    fileprivate func unselectParking(_ sender: UIResponder?) {
        parkingView.parking = nil
        parkingBottomToView.priority = .defaultLow
        parkingBottomToSafeArea.priority = .defaultHigh
        parkingLeft.constant = .margin/2
        parkingRight.constant = -.margin/2
        if sender != mapController {
            mapController?.deselectPoint()
        }
        UIView.animate(withDuration: 0.3, animations: view.layoutIfNeeded)
    }
    
    override func mapDidSelect(point: MapPoint) {
        guard let parking = point as? Parking.Spot else { return }
        select(spot: parking)
    }
    
    override func didTapOnMap() {
        unselectParking(mapController)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        navigationController?.setNavigationBarHidden(false, animated: animated)
    }
}
