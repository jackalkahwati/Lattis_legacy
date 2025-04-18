//
//  BikeLocationViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 02.09.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Model
import Cartography

class BikeLocationViewController: UIViewController, OverMap {
    func didLayoutSubviews() {}
    
    override func loadView() {
        view = PassthroughView()
    }
    
    weak public var mapController: MapRepresentable? {
        didSet {
            (view as? PassthroughView)?.targetView = mapController?.mapView
        }
    }
    
    fileprivate let bike: Model.Bike
    fileprivate let backButton = ActionButton()
    fileprivate let focusButton = UIButton.rounded()

    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.addSubview(backButton)
        focusButton.setImage(.named("icon_location_home"), for: .normal)
        focusButton.tintColor = .accent
        focusButton.addTarget(self, action: #selector(centerMap), for: .touchUpInside)
        view.addSubview(focusButton)
        
        constrain(backButton, focusButton, view) { back, focus, view in
            back.bottom == view.safeAreaLayoutGuide.bottom - .margin
            back.left == view.left + .margin
            back.right == view.right - .margin
            
            focus.right == view.right - .margin
            focus.bottom == back.top - .margin*4
        }
        
        backButton.action = .plain(title: "back".localized(), handler: close)
        mapController?.add(points: [bike], selected: nil)
        
//        focus()
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
            self?.focus()
        }
    }
    
    fileprivate func focus() {
        if let user = mapController?.location {
            mapController?.focus(on: [user.coordinate, bike.coordinate])
        } else {
            mapController?.focus(on: bike.coordinate)
        }
    }
    
    @objc
    fileprivate func centerMap() {
        mapController?.centerOnUserLocation()
    }
    
    init(_ bike: Model.Bike) {
        self.bike = bike
        super.init(nibName: nil, bundle: nil)
    }
    
    static func map(_ bike: Model.Bike) -> MapRepresentable {
        AppRouter.shared.map(BikeLocationViewController(bike))
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension Model.Bike: MapPoint {
    public var title: String? { bikeName }
    
    public var subtitle: String? { fleet.name }
    
    public var color: UIColor { .accent }
    
    public var bage: Int? { nil }
    
    public func isEqual(to: MapPoint) -> Bool {
        guard let bike = to as? Model.Bike else { return false }
        return self == bike
    }
    
    public var batteryLevel: Int? { nil }

    public var identifier: String {
        switch bikeGroup.type {
        case .electric:
            return "annotation_bike_electric"
        case .kickScooter:
            return "annotation_bike_kick_scooter"
        case .locker:
            return "annotation_locker"
        default:
            return "annotation_bike_regular"
        }
    }
}
