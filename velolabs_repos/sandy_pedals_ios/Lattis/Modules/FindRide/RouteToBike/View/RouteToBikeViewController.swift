//
//  RouteToBikeRouteToBikeViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import Mapbox
//import MapboxCoreNavigation
import Oval

class RouteToBikeViewController: ViewController {
    
    @IBOutlet var rideView: RouteToBikeView!
    var interactor: RouteToBikeInteractorInput!
    weak var mapNavigation: MapRepresenting?
    var mapContainer: MapContainer {
        return rideView
    }
    
    deinit {
        interactor.suspend()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        interactor.startBooking()
        rideView.showSpinner()
        rideView.timeTextView.delegate = self
    }
    
    fileprivate var viewIsLoaded = false
    fileprivate var bleAlert: ActionAlertView?
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        viewIsLoaded = true
        let camera = mapContainer.mapView.camera
        camera.heading = 0
        mapContainer.mapView.setCamera(camera, animated: true)
    }
    
    @IBAction func cancelAction(_ sender: Any) {
        interactor.cancelTrip()
    }
    
    @IBAction func menuAction(_ sender: Any) {
        interactor.openMenu()
    }
    
    @IBAction func startRide(_ sender: ProgressButton) {
        if sender.progress == 1 {
            interactor.beginTrip()
        } else {
            interactor.trackUnconnectedBegin()
            warning(with: "bike_booking_begin_trip_not_connected_title".localized(), subtitle: "bike_booking_begin_trip_not_connected_text".localized())
        }
    }
    
    @IBAction func infoAction(_ sender: Any) {
        interactor.openInfo()
    }
    
    fileprivate func expireAlert() {
        let alert = ErrorAlertView.alert(title: "booking_expired_title".localized(), subtitle: "booking_expired_text".localized(), button: "booking_expired_cancel".localized())
        alert.action = interactor.performCancel
        alert.show(parrent: rideView)
    }
    
    override func show(error: Error, file: String, line: Int) {
        if let err = error as? SessionError {
            Analytics.report(error, file: file, line: line)
            if err.check(.lengthRequired) {
                warning(with: "public_fleet_no_stripe_title".localized(), subtitle: "public_fleet_no_stripe_text".localized())
            } else if case .unauthorized = err.code {
                let alert = ErrorAlertView.alert(title: "route_to_bike_booked_alert_title".localized(), subtitle: "route_to_bike_booked_alert_text".localized())
                alert.action = interactor.performCancel
                stopLoading {
                    alert.show(parrent: self.view)
                }
            } else {
                super.show(error: error, file: file, line: line)
            }
        } else {
            super.show(error: error, file: file, line: line)
        }
    }
}

extension RouteToBikeViewController: RouteToBikeInteractorOutput {
    func update(time: String, for bike: Bike) {
        rideView.update(time: time, for: bike)
    }
    
    func timeExpired() {
        expireAlert()
    }
    
    func buildRoute(to bike: Bike) {
        let annotation = MapAnnotation(model: bike)
        mapNavigation?.mapView.addAnnotation(annotation)
        
        guard AppDelegate.fake == false else { return }
        rideView.bikeNameLabel.text = bike.name
    }
    
    func hideSpinner() {
        // Hacky way. Better to solve it differently
        guard viewIsLoaded else { return DispatchQueue.main.asyncAfter(deadline: .now() + 0.3, execute: rideView.hideSpinner) }
        rideView.hideSpinner()
    }
    
    func connected() {
        rideView.startButton.endAnimation()
    }
    
    func connecting() {
        rideView.startButton.beginAnimation()
    }
    
    func disconnected() {
        rideView.startButton.endAnimation(progress: 0)
    }
    
    func update(tripTime: String?, fare: String?) {
        if rideView.activeView.alpha == 0 {
            UIView.animate(withDuration: .defaultAnimation, animations: { 
                self.rideView.activeView.alpha = 1
                self.rideView.separatorView.alpha = 1
                self.rideView.infoButton.alpha = 1
                self.rideView.titleLabel.alpha = 1
                self.rideView.bikeNameLabel.alpha = 1
                self.rideView.timeTextView.alpha = 0
                self.rideView.textHeight.constant = 64
                self.rideView.layoutIfNeeded()
            }, completion:{ _ in
                self.rideView.timeTextView.isHidden = true
            })
        }
        rideView.activeTimeLabel.text = tripTime
        rideView.fareLabel.text = fare
    }
    
    func showCancelWarning(tripStarted: Bool) {
        let title = tripStarted ? "bike_booking_end_ride_title".localized() :"route_to_bike_cancel_title".localized()
        let subtitle = tripStarted ? "bike_booking_end_ride_text".localized() : "route_to_bike_cancel_text".localized()
        let action = tripStarted ? "bike_booking_end_ride_action".localized() : "route_to_bike_cancel_submit".localized()
        let cancel = tripStarted ? "route_to_bike_cancel_cancel".localized() : "route_to_bike_cancel_cancel".localized()
        let alert = ActionAlertView.alert(title: title, subtitle: subtitle)
        alert.action = AlertAction(title: action, action: interactor.performCancel)
        alert.cancel = AlertAction(title: cancel, action: {AppRouter.shared.postCancelBooking = nil})
        alert.show(parrent: rideView)
    }
    
    func showBLEWarning() {
        rideView.startButton.progress = 0
        bleAlert = ActionAlertView.alert(title: "find_ride_no_bluettoth_title".localized(), subtitle: "find_ride_no_bluettoth_text".localized())
        bleAlert?.cancel = AlertAction(title: "damage_report_success_cancel_booking".localized(), action: interactor.performCancel)
        bleAlert?.show(parrent: rideView)
    }
    
    func hideWarnings() {
        bleAlert?.hide()
        bleAlert = nil
    }
}

extension RouteToBikeViewController: MapContaining {
    func didUpdate(userLocation: CLLocation) { interactor.location = userLocation }
}

extension RouteToBikeViewController: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange) -> Bool {
        interactor.openInfo()
        return false
    }
}
