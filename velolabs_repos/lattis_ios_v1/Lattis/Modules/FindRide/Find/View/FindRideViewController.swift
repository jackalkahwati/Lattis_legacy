//
//  FindRideViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 24/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit
import Mapbox
import Localize_Swift
import SwiftyTimer
import MapKit
import iCarousel
import EasyTipView

class FindRideViewController: ViewController {
    var interactor: FindRideInteractorInput!
    weak var mapNavigation: MapRepresenting?
    var mapContainer: MapContainer {
        return rideView
    }
    
    fileprivate let rideView = FindRideView.nib() as! FindRideView
    fileprivate var bikes: [Bike] = []
    fileprivate var card: CreditCard?
    
    override func loadView() {
        view = rideView
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        rideView.navigationView.findButton.addTarget(self, action: #selector(performSearch), for: .touchUpInside)
        
        rideView.carouselView.dataSource = self
        rideView.carouselView.delegate = self
                
        interactor.viewLoaded()
        NotificationCenter.default.addObserver(self, selector: #selector(currentCard(notification:)), name: currentCardChanged, object: nil)
        
        if AppRouter.shared.needQRTip() {
            var pref = EasyTipView.globalPreferences
            pref.positioning.bubbleHInset = 16
            pref.positioning.bubbleVInset = 10
            let view = EasyTipView(text: "find_ride_qr_tip".localized(), preferences: pref, delegate: nil)
            DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                view.show(animated: true, forView: self.rideView.navigationView.qrButton, withinSuperview: self.view)
                DispatchQueue.main.asyncAfter(deadline: .now() + 10) { [weak view] in
                    view?.dismiss()
                }
            }
        }
    }
    
    override var prefersStatusBarHidden: Bool {
        return false
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    private var isInitialSearch = true
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if let map = mapNavigation, map.isSelectMode == false {
            mapNavigation?.stopNavigation()
        }
        card = CoreDataStack.shared.currentCard
        rideView.carouselView.reloadData()
        
        if isInitialSearch && AppRouter.shared.isConnected {
            isInitialSearch = false
            startLoading(with: "find_ride_search".localized(), animated: false)
        } else if AppRouter.shared.isConnected == false {
            isInitialSearch = false
        }
    }
    
    @objc fileprivate func currentCard(notification: Notification) {
        rideView.carouselView.reloadData()
    }
    
    @objc private func performSearch() {
        mapNavigation?.clearSelection()
        rideView.hideCallout()
        rideView.hideWarning()
        interactor.search()
    }
    
    @IBAction func menuAction(_ sender: Any) {
        interactor.openMenu()
    }

    @IBAction func bookBike(_ sender: Any) {
        interactor.bookSelectedBike()
    }
    
    @IBAction func closeWarning(_ sender: Any) {
        rideView.hideWarning()
//        rideView.navigationView.state = .find
        mapNavigation?.clearSelection()
    }
    
    @IBAction func triggerSearch(_ sender: Any) {
        rideView.navigationView.state = .invert(rideView.navigationView.state)
    }
    
    @IBAction func hideSearch(_ sender: Any) {
        rideView.navigationView.state = .choose
    }
    
    @IBAction func choosePickUp(_ sender: Any) {
        mapNavigation?.clearSelection()
        rideView.hideWarning()
        rideView.hideCallout()
        interactor.choosePickUp()
    }
    
    @IBAction func scanQrCode(_ sender: Any) {
//        mapNavigation?.clearSelection()
        rideView.navigationView.state = .choose
        interactor.scanQRCode()
    }
}


extension FindRideViewController: MapContaining {
    func didSelect(annotation: MapAnnotation) {
        if let bike = annotation.model as? Bike {
            if let idx = bikes.firstIndex(where: { $0.bikeId == bike.bikeId }), rideView.carouselView.currentItemIndex != idx {
                rideView.carouselView.scrollToItem(at: idx, animated: true)
            }
            rideView.showCallout(with: bike)
        }
        interactor.selectBike(with: annotation)
        
    }
    
    func didUnselect(annotation: MapAnnotation?) {
        rideView.hideCallout()
        interactor.unselectBike()
    }
    
    func didUpdate(userLocation: CLLocation) {
        interactor.update(userCoordinate: userLocation.coordinate)
    }
    
    func mapView(_ mapView: MGLMapView, viewFor annotation: MGLAnnotation) -> MGLAnnotationView? {
        if let title = annotation.title, let tt = title, tt == "user" {
            let image = #imageLiteral(resourceName: "icon_user_pin")
            let view = MapAnnotationView(reuseIdentifier: "user", image: image, size: image.size)
            return view
        }
        guard let annotation = annotation as? MapAnnotation,
            let bike = annotation.model as? Bike else { return nil }
        
        let reuseIdentifier = bike.bikeType.rawValue
        var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: reuseIdentifier)
        if annotationView == nil {
            annotationView = MapAnnotationView(reuseIdentifier: reuseIdentifier, image: annotation.image)
        }
        return annotationView
    }
    
    var canSelectAnnotations: Bool {
        return rideView.isWarningShown == false
    }
    
    func validate(bikes: [Bike]) -> [Bike] {
        var valid: [Bike] = []
        var invalid: [Bike] = []
        for bike in bikes {
            if CLLocationCoordinate2DIsValid(bike.coordinate) {
                valid.append(bike)
            } else {
                invalid.append(bike)
            }
        }
        if !invalid.isEmpty {
            Analytics.report(Bike.Error.invalidCoordinates(invalid.map{$0.bikeId}))
        }
        return valid
    }
}

extension FindRideViewController: FindRideInteractorOutput {
    func show(annotations: [MapAnnotation]) {
        stopLoading()
        if annotations.isEmpty == false {
            rideView.navigationView.state = .choose
            mapNavigation?.mapView.addAnnotations(annotations)
        }
        bikes = annotations.compactMap{ $0.model as? Bike }
        rideView.carouselView.reloadData()
    }
    
    func show(result: Bike.Search, userLocation: Direction?) {
        var user: CLLocationCoordinate2D? = nil
        if let location = userLocation, CLLocationCoordinate2DIsValid(location.coordinate) {
            user = location.coordinate
            let point = MGLPointAnnotation()
            point.coordinate = location.coordinate
            point.title = "user"
            mapNavigation?.mapView.addAnnotation(point)
        } else if let coord = mapNavigation?.mapView.userLocation?.coordinate {
            user = coord
        }
        func busyCase() {
            if let loc = user {
                mapNavigation?.mapView.setCenter(loc, animated: true)
            }
            rideView.showWarning(with: "find_ride_warning_busy".localized())
        }
        switch result {
        case .nearest(let bikes):
            if bikes.count > 1 {
                mapNavigation?.coordinatesToShow = bikes.map{$0.coordinate} + (user == nil ? [] : [user!])
            } else {
                mapNavigation?.navigateToUserLocation()
            }
            let valid = validate(bikes: bikes)
            let annotations = valid.map(MapAnnotation.init)
            mapNavigation?.mapView.addAnnotations(annotations)
            self.bikes = valid
            if let bike = annotations.first, let map = mapNavigation as? MapViewController {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3, execute: {
                    map.state = .callout(bike)
                })
            }
            if valid.isEmpty {
                busyCase()
            }
        case .available(let bikes):
            let valid = validate(bikes: bikes)
            mapNavigation?.coordinatesToShow = valid.map{$0.coordinate} + (user == nil ? [] : [user!])
            mapNavigation?.mapView.addAnnotations(valid.map(MapAnnotation.init))
            self.bikes = valid
            if valid.isEmpty {
                busyCase()
            } else {
                rideView.showWarning(with: "find_ride_warning_available".localized())
            }
            
        case .busy:
            busyCase()
        case .noService:
            if let loc = user {
                mapNavigation?.mapView.setCenter(loc, animated: true)
            }
            let alert = ActionAlertView.alert(title: "find_ride_no_service_title".localized(), subtitle: "find_ride_no_service_subtitle".localized())
            alert.action = AlertAction(title: "find_ride_no_service_action".localized(), action: interactor.addPrivateNetwork)
            alert.cancel = AlertAction(title: "general_btn_ok".localized(), action: {})
            stopLoading {
                alert.show()
            }
            return
        }
        stopLoading()
        rideView.navigationView.state = .choose
        rideView.carouselView.reloadData()
    }
    
    func show(userLocationTitle: String) {
        rideView.navigationView.locationLabel.text = userLocationTitle
    }
    
    func closeSelection() {
        mapNavigation?.clearSelection()
        rideView.hideWarning()
    }
    
    func showQR(bike: Bike) {
        guard bikes.isEmpty == false  else {
            return show(result: .nearest([bike]), userLocation: nil)
        }
        
        if let idx = bikes.firstIndex(where: {$0.bikeId == bike.bikeId}) {
            rideView.carouselView.scrollToItem(at: idx, animated: true)
        } else {
            let freshBikes = [bike] + bikes
            show(result: .nearest(freshBikes), userLocation: nil)
        }
    }
}


extension FindRideViewController: iCarouselDelegate, iCarouselDataSource {
    func numberOfItems(in carousel: iCarousel) -> Int {
        return bikes.count
    }
    
    func carousel(_ carousel: iCarousel, viewForItemAt index: Int, reusing view: UIView?) -> UIView {
        let cell = (view as? BikeFindCell) ?? BikeFindCell.nib() as! BikeFindCell
        cell.bike = bikes[index]
        cell.card = card
        cell.openLink = interactor.openTerms(with:)
        return cell
    }
    
    func carouselItemWidth(_ carousel: iCarousel) -> CGFloat {
        return 290
    }
    
    func carousel(_ carousel: iCarousel, didSelectItemAt index: Int) {
        interactor.selectedBikeInfo()
    }
    
    func carouselCurrentItemIndexDidChange(_ carousel: iCarousel) {
        guard bikes.count > carousel.currentItemIndex && carousel.currentItemIndex >= 0 else { return }
        let bike = bikes[carousel.currentItemIndex]
        mapNavigation?.select(annotationWith: bike, filter: { (annotation) -> Bool in
            guard let bk = annotation.model as? Bike else { return false }
            return bk.bikeId == bike.bikeId
        })
    }
}
