//
//  RideRideViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 22/02/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import Mapbox
import LattisSDK
import Oval
import EasyTipView

class ParkingManager {
    var zones: [ParkingZone]? {
        didSet {
            check()
        }
    }
    var spots: [MapAnnotation]? {
        didSet {
            check()
        }
    }
    
    private func check() {
        guard let zones = self.zones, let spots = self.spots else { return }
        showHint(zones.isEmpty == false || spots.isEmpty == false)
    }
    
    var showHint: (Bool) -> () = {_ in}
}

class RideViewController: ViewController {
    var mapContainer: MapContainer {
        return rideView
    }
    weak var mapNavigation: MapRepresenting?
    var interactor: RideInteractorInput!
    fileprivate var rideView = RideView.nib() as! RideView
    fileprivate var isRouteRequested: Bool = false
    fileprivate let parkinManager = ParkingManager()
    fileprivate var parkingHelper: ParkingHelper?
    fileprivate weak var lockButton: LockButton?
    fileprivate weak var tipView: EasyTipView?
    fileprivate var isHintShown: Bool = true
    fileprivate var hintText: String?
    
    override func loadView() {
        view = rideView
    }

    deinit {
        interactor.suspend()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        interactor.viewDidLoad()
        interactor.startCount()
        if interactor.shouldFollowUser {
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) { [weak self] in
                self?.mapNavigation?.followUser = true
            }
        }
        
        parkinManager.showHint = { [unowned self] zones in
            self.rideView.showParkings(zones: zones)
        }
        
        parkingHelper = ParkingHelper(mapNavigation!.mapView)
        AppRouter.shared.onInternetAlert = { [weak self] button in
            guard let `self` = self else { return }
            self.lockButton = button
            button.isHidden = false
            button.lockState = self.rideView.lockButton.lockState
            button.addTarget(self, action: #selector(self.lockAction(_:)), for: .touchUpInside)
        }
    }
    
    @IBAction func endRideAction(_ sender: Any) {
        interactor.endRide(forced: interactor.canForceEndRide)
    }
    
    @IBAction func parkingAction(_ sender: LoadingButton) {
        interactor.canForceEndRide = false
        if rideView.toggleParkings() {
            interactor.searchParkings()
            mapNavigation?.mapView.userTrackingMode = .none
            hideHint(save: true)
        } else {
            interactor.stopSearchParkings()
            mapNavigation?.clearSelection()
            mapNavigation?.mapView.userTrackingMode = .follow
            if let text = hintText {
                showHint(text: text)
            }
        }
    }
    
    @IBAction func profileAction(_ sender: Any) {
        interactor.openMenu()
    }
    
    @IBAction func getDirectionAction(_ sender: Any) {
        _ = rideView.toggleParkings()
        interactor.routeToSelectedParking()
        mapNavigation?.clearSelection()
    }
    
    @IBAction func closeCallout(_ sender: Any) {
        mapNavigation?.unselectAnnotation(showRest: true)
    }
    
    @IBAction func lockAction(_ sender: LockButton) {
        guard interactor.isBluetoothEnabled else { return }
        hideHint()
        var state = LockButton.LockState.locked
        switch sender.lockState {
        case .locked:
            state = .unlocked
        case .disconnected:
            interactor.connectLock()
            return
        default:
            break
        }
        guard interactor.set(lockState: state) else { return }
        rideView.endRideButton.isEnabled = false
        rideView.lockButton.lockState = .processing(state)
        lockButton?.lockState = .processing(state)
    }
    
    
    @IBAction func closeNavigation(_ sender: Any) {
        rideView.hideRouteMode()
        mapNavigation?.clearSelection()
        mapNavigation?.stopNavigation()
    }
    
    @IBAction func navigateHome(_ sender: Any) {
        mapNavigation?.navigateToUserLocation()
    }
    
    fileprivate func showHint(isLocked: Bool) {
        guard interactor.needShowHint else { return }
        let text = isLocked ? "active_ride_unlock_tip" : "active_ride_lock_tip"
        showHint(text: text.localized())
    }
    
    fileprivate func hideHint(save: Bool = false) {
        isHintShown = false
        tipView?.dismiss()
        if !save {
            hintText = nil
        }
    }
}

extension RideViewController: RideInteractorOutput {
    func show(_ annotations: [MapAnnotation]) {
        rideView.parkingButton.stopLoading()
//        if annotations.isEmpty == false {
            parkinManager.spots = annotations
            rideView.mapView.addAnnotations(annotations)
        parkingHelper?.annotations = annotations
//        } else {
//            rideView.parkingButton.isUserInteractionEnabled = false
//            rideView.showWarning(title: "active_ride_parkings_not_found_title".localized(), text: "active_ride_parkings_not_found_text".localized()) {
//                Timer.after(3.seconds, { [weak self] in
//                    self?.rideView.isParkingsSown = false
//                    self?.rideView.parkingButton.isUserInteractionEnabled = true
//                })
//            }
//        }
    }
    
    func show(zones: [ParkingZone]) {
        mapNavigation?.show(zones: zones)
        parkinManager.zones = zones
        parkingHelper?.zones = zones
    }
    
    func buildRoute(to annotation: MapAnnotation) {
        let action = UIAlertController(title: annotation.title, message: nil, preferredStyle: .actionSheet)
        action.addAction(.init(title: "Apple Maps", style: .default, handler: { (_) in
            var urlString = "http://maps.apple.com/?daddr=\(annotation.coordinate.latitude),\(annotation.coordinate.longitude)"
            if let title = annotation.title {
                urlString += "&q=\(title)"
            }
            let url = URL(string: urlString)!
            UIApplication.shared.open(url)
        }))
        if UIApplication.shared.canOpenURL(URL(string: "https://www.google.com/maps")!) {
            action.addAction(.init(title: "Google Maps", style: .default, handler: { (_) in
                var urlString = "https://www.google.com/maps/?daddr=\(annotation.coordinate.latitude),\(annotation.coordinate.longitude)"
                if let title = annotation.title {
                    urlString += "&q=\(title)"
                }
                let url = URL(string: urlString)!
                UIApplication.shared.open(url)
            }))
        }
        action.addAction(.init(title: "cancel".localized(), style: .cancel, handler: nil))
        present(action, animated: true, completion: nil)
    }
    
    func update(_ time: String) {
        rideView.timeLabel.text = time
    }
    
    func show(lockState: LockButton.LockState) {
        switch lockState {
        case .locked, .unlocked:
            showHint(isLocked: lockState.isLocked)
        default:
            hideHint()
        }
        rideView.lockButton.lockState = lockState
        rideView.stateLabel.text = lockState.text
        lockButton?.lockState = lockState
        switch lockState {
        case .processing:
            rideView.endRideButton.isEnabled = false
        default:
            rideView.endRideButton.isEnabled = true
        }
    }
    
    func showHint(text: String) {
        if isHintShown {
            hideHint()
        }
        var pref = EasyTipView.globalPreferences
        pref.positioning.bubbleHInset = 16
        pref.drawing.arrowPosition = .bottom
        let view = EasyTipView(text: text, preferences: pref, delegate: nil)
        view.show(animated: true, forView: self.rideView.lockButton, withinSuperview: self.rideView)
        self.isHintShown = true
        tipView = view
        hintText = text
    }
    
    func handleJamming() {
        let alert = ErrorAlertView.alert(title: "active_ride_jamming_title".localized(), subtitle: "active_ride_jamming_subtitle".localized())
        alert.show()
    }
    
    func show(parkingCheck: Parking.Check) {
        let alert = ActionAlertView.alert(check: parkingCheck)
        
        switch parkingCheck {
        case .restricted:
            alert.cancel = AlertAction(title: "btn_cancel".localized(), action: {})
            if !self.rideView.isParkingsSown {
                alert.action = AlertAction(title: "active_ride_out_of_zones_action".localized(), action: {
                    self.parkingAction(self.rideView.parkingButton)
                })
            }
        default:
            if rideView.isParkingsSown {
                alert.action = AlertAction(title: "damage_report_success_continue_ride".localized().uppercased(), action: {})
            } else {
                alert.action = AlertAction(title: "active_ride_out_of_zones_action".localized(), action: {
                    self.parkingAction(self.rideView.parkingButton)
                    self.interactor.canForceEndRide = true
                })
            }
            alert.cancel = AlertAction(title: "active_ride_out_of_zones_cancel".localized(), action: {
                self.interactor.endRide(forced: true)
            })
        }
        alert.show()
    }
    
    func show(update: Trip.Update) {
        rideView.priceLabel.isHidden = update.price != nil ? update.price! <= 0 : true
        rideView.priceTitleLabel.isHidden = rideView.priceLabel.isHidden
        rideView.priceLabel.text = update.price?.priceValue(update.currency)
    }
    
    func show(bike: Bike) {
        guard let name = bike.name,
            let data = "active_ride_in_ride_with".localizedFormat(bike.bikeId, name).data(using: .utf8),
            let attr = try? NSMutableAttributedString(data: data, options: [.documentType: NSAttributedString.DocumentType.html, .characterEncoding: String.Encoding.utf8.rawValue], documentAttributes: nil) else { return }
        let range = NSRange(location: 0, length: attr.string.count)
        let attributes: [NSAttributedString.Key: Any] = [.font: rideView.rideLabel.font!, .foregroundColor: rideView.rideLabel.textColor!, .underlineColor: rideView.rideLabel.textColor!]
        attr.addAttributes(attributes, range: range)
        rideView.rideLabel.attributedText = attr
        rideView.rideTextView.attributedText = attr
        rideView.rideTextView.linkTextAttributes = attributes
        rideView.rideTextView.delegate = self
    }
}

extension RideViewController: MapContaining {
    func currentLocation(isVisible: Bool) {
        rideView.isPositionButtonShown = !isVisible
    }
    
    func didUpdate(userLocation: CLLocation) {
        interactor.location = userLocation
    }
    
    func didSelect(annotation: MapAnnotation) {
        interactor.selectParking(with: annotation)
        guard let parking = annotation.model as? Parking else { return }
        rideView.showCallout(with: parking)
    }
    
    func didUnselect(annotation: MapAnnotation?) {
        rideView.hideCallout()
        interactor.unselectParking()
    }
    
    func mapView(_ mapView: MGLMapView, viewFor annotation: MGLAnnotation) -> MGLAnnotationView? {
        
        guard let annotation = annotation as? MapAnnotation else { return nil }
        
        let reuseIdentifier = "Parking"
        
        var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: reuseIdentifier) as? MapAnnotationView
        if annotationView == nil {
            annotationView = MapAnnotationView(reuseIdentifier: reuseIdentifier, image: annotation.image)
        } else {
            annotationView?.imageView?.image = annotation.image
        }
        
        return annotationView
    }
    
    var canSelectAnnotations: Bool {
        return rideView.isRouteMode == false
    }
    
    override func show(error: Error, file: String, line: Int) {
        var needReport = true
        if let err = error as? EllipseError {
            switch err {
            case .timeout:
                warning(with: "ellipse_connection_tmeout_title".localized(), subtitle: "ellipse_connection_tmeout_text".localized())
                show(lockState: .disconnected)
            default:
                super.show(error: err, file: file, line: line)
            }
        } else if let err = error as? SessionError {
            var title: String? = nil
            var subtitle: String? = nil
            switch err.code {
            case .lengthRequired:
                title = "public_fleet_no_stripe_title".localized()
                subtitle = "public_fleet_no_stripe_text".localized()
            case .resourceNotFound:
                title = "active_ride_no_parking_zones_title".localized()
                subtitle = "active_ride_no_parking_zones_text".localized()
            case .conflict:
                let alert = ActionAlertView.alert(title: "end_ride_stripe_error_title".localized(), subtitle: "end_ride_stripe_error_text".localized())
                alert.action = AlertAction(title: "end_ride_stripe_error_action".localized(), action: interactor.openPayments)
                alert.cancel = AlertAction(title: "end_ride_stripe_error_cancel".localized(), action: {alert.hide()})
                stopLoading {
                    alert.show()
                }
                return
            default:
                super.show(error: error, file: file, line: line)
            }
            if let tt = title {
                warning(with: tt, subtitle: subtitle)
            }
        } else if error is TripServiceError {
            warning(with: "privacy_location_alert_title".localized(), subtitle: "privacy_location_alert_text".localized())
        } else {
            needReport = false
            super.show(error: error, file: file, line: line)
        }
        if needReport {
            Analytics.report(error, file: file, line: line)
        }
    }
}

extension RideViewController: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange, interaction: UITextItemInteraction) -> Bool {
        interactor.openBikeDetails()
        return false
    }
}

extension Parking.Check {
    var alertTexts: (title: String, subTitle: String) {
        switch self {
        case .restricted:
            return ("notice".localized(), "parking_restricted_warning_message".localized())
        case .fee(let price, let currency):
            return ("active_ride_parking_out_of_bounds_title".localized(), "active_ride_parking_out_of_bounds_text".localizedFormat(price.priceValue(currency)!))
        default:
            return ("active_ride_out_of_zones_title".localized(), "active_ride_out_of_zones_text".localized())
        }
    }
}

extension ActionAlertView {
    class func alert(check: Parking.Check) -> ActionAlertView {
        let text = check.alertTexts
        return alert(title: text.title, subtitle: text.subTitle)
    }
}


private final class ParkingHelper {
    let mapView: MGLMapView
    
    init(_ mapView: MGLMapView) {
        self.mapView = mapView
    }
    
    var annotations: [MapAnnotation] = [] {didSet {calculate()}}
    var zones: [ParkingZone] = [] {didSet {calculate()}}
    
    func calculate() {
        var coordinates: [CLLocationCoordinate2D] = []
        for zone in zones {
            switch zone.geometry {
            case .polygon(let coord), .rectangle(let coord):
                coordinates += coord
            case .circle(let circle):
                coordinates += circle.polygon().1
            default:
                break
            }
        }
        coordinates += annotations.map({$0.coordinate})
        guard let bounds = MGLCoordinateBounds(coordinates: coordinates) else { return }
        let camera = mapView.cameraThatFitsCoordinateBounds(bounds, edgePadding: UIEdgeInsets(top: 94, left: 50, bottom: 120, right: 50))
        mapView.setCamera(camera, animated: true)
    }
}
