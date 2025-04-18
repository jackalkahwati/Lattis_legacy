//
//  NavigationNavigationViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 15/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Mapbox

class NavigationViewController: ViewController {
    @IBOutlet weak var directionLeftLayout: NSLayoutConstraint!
    @IBOutlet weak var endAddressLabel: UILabel!
    @IBOutlet weak var instructionsLabel: UILabel!
    @IBOutlet weak var startAddressLabel: UILabel!
    @IBOutlet weak var durationLabel: UILabel!
    @IBOutlet weak var distanceLabel: UILabel!
    @IBOutlet weak var directionNameLabel: UILabel!
    @IBOutlet weak var directionView: UIView!
    @IBOutlet weak var directionLayout: NSLayoutConstraint!
    @IBOutlet weak var calloutLayout: NSLayoutConstraint!
    @IBOutlet weak var lockTimeLabel: UILabel!
    @IBOutlet weak var lockNameLabel: UILabel!
    @IBOutlet weak var calloutView: UIView!
    @IBOutlet weak var emptyView: UIView!
    @IBOutlet weak var mapView: MGLMapView!
    
    var interactor: NavigationInteractorInput!
    
    fileprivate let dateFormatter = DateFormatter()
    fileprivate let timeFormatter = DateFormatter()
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .default
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        addMenuButton()
        title = "find_my_ellipse".localized()
        
        mapView.userTrackingMode = .follow
        mapView.delegate = self
        
        dateFormatter.dateStyle = .medium
        timeFormatter.timeStyle = .short
        
        interactor.start()
    }
    
    @IBAction func getDirections(_ sender: Any) {
        interactor.getDirection()
    }
    
    @IBAction func hideCallout(_ sender: Any) {
        interactor.unselect()
        navigationItem.rightBarButtonItem = nil
        calloutLayout.priority = .defaultLow
        directionLayout.priority = .defaultLow
        directionLeftLayout.priority = .defaultLow
        UIView.animate(withDuration: 0.35) {
            self.calloutView.alpha = 0
            self.directionView.alpha = 0
            self.view.layoutIfNeeded()
        }
    }
}

extension NavigationViewController: NavigationInteractorOutput {
    func show(ellipse: Ellipse) {
        if let date = ellipse.stateChangedAt {
            let timeText = ellipse.lockState == .unlocked ? "unlocked_at_ios" : "locked_at_ios"
            lockTimeLabel.text = timeText.localizedFormat(timeFormatter.string(from: date), dateFormatter.string(from: date))
        } else {
            lockTimeLabel.text = nil
        }
        directionNameLabel.text = ellipse.name
        mapView.deselect(ellipse: ellipse)
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "hide".localized(), style: .plain, target: self, action: #selector(hideCallout(_:)))
        lockNameLabel.text = ellipse.name
        guard calloutView.alpha != 1 else { return }
        self.calloutLayout.priority = .defaultHigh
        UIView.animate(withDuration: 0.35) {
            self.calloutView.alpha = 1
            self.view.layoutIfNeeded()
        }
        guard CLLocationCoordinate2DIsValid(ellipse.coordinate) else { return }
        mapView.setCenter(ellipse.coordinate, zoomLevel: 14, animated: true)
    }
    
    func show(locks: [Ellipse]) {
        let points = locks.compactMap(Annotation.init)
        if let annotations = mapView.annotations {
            annotations.forEach(mapView.removeAnnotation(_:))
        }
        emptyView.isHidden = points.isEmpty == false
        if emptyView.isHidden {
            mapView.addAnnotations(points)
        }
    }
    
    func show(direction: Direction) {
        distanceLabel.text = direction.distance
        durationLabel.text = direction.duration
        startAddressLabel.text = direction.startAddress
        endAddressLabel.text = direction.endAddress
        instructionsLabel.text = direction.instructions
        directionLayout.priority = .defaultHigh
        directionLeftLayout.priority = .defaultHigh
        calloutLayout.priority = .defaultLow
        UIView.animate(withDuration: 0.35) {
            self.view.layoutIfNeeded()
            self.directionView.alpha = 1
            self.calloutView.alpha = 0
        }
    }
}

extension NavigationViewController: MGLMapViewDelegate {
    func mapView(_ mapView: MGLMapView, didSelect annotation: MGLAnnotation) {
        guard let annotation = annotation as? Annotation else { return }
        interactor.select(ellipse: annotation.ellipse)
    }
    
    func mapView(_ mapView: MGLMapView, didDeselect annotation: MGLAnnotation) {
        
    }
    
    func mapView(_ mapView: MGLMapView, imageFor annotation: MGLAnnotation) -> MGLAnnotationImage? {
        var image = mapView.dequeueReusableAnnotationImage(withIdentifier: "image")
        if image == nil {
            image = MGLAnnotationImage(image: #imageLiteral(resourceName: "map_shared_to_me_bike_icon_large"), reuseIdentifier: "image")
        }
        return image
    }
    
    func mapView(_ mapView: MGLMapView, didUpdate userLocation: MGLUserLocation?) {
        interactor.userLocation = userLocation?.coordinate
    }
}

private extension NavigationViewController {
    
}

class Annotation: NSObject, MGLAnnotation {
    var coordinate: CLLocationCoordinate2D
    let ellipse: Ellipse
    
    init?(_ ellipse: Ellipse) {
        guard CLLocationCoordinate2DIsValid(ellipse.coordinate) else { return nil }
        self.coordinate = ellipse.coordinate
        self.ellipse = ellipse
    }
}

extension MGLMapView {
    func deselect(ellipse: Ellipse) {
        guard let annotations = self.annotations as? [Annotation] else { return }
        if let idx = annotations.index(where: {$0.ellipse.lockId == ellipse.lockId}) {
            let ann = self.annotations?[idx]
            deselectAnnotation(ann, animated: false)
        }
    }
}
