//
//  ParkingZonesParkingZonesViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/05/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import Mapbox
import MapKit

class ParkingZonesViewController: ViewController {
    var interactor: ParkingZonesInteractorInput!
    
    fileprivate var coordinates: [CLLocationCoordinate2D] = []
    fileprivate let mapView = MGLMapView(frame: .zero, styleURL: URL(string: "mapbox://styles/mapbox/light-v9"))
    
    override func loadView() {
        view = mapView
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        mapView.delegate = self
        interactor.viewLoaded()
        
        title = "bike_info_parking_zones_title".localized()
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close(_ :)))
    }
    
    @objc private func close(_ sender: Any) {
        navigationController?.dismiss(animated: true, completion: nil)
    }
}

extension ParkingZonesViewController: ParkingZonesInteractorOutput {
    func show(zones: [ParkingZone]) {
        zones.forEach{ add(zone: $0) }
        adjustZoom()
    }
    
    func show(spots: [Parking]) {
        mapView.addAnnotations(spots.map(MapAnnotation.init))
        
        adjustZoom(with: spots.map({ $0.coordinate }))
    }
}

extension ParkingZonesViewController: MGLMapViewDelegate {
    func mapView(_ mapView: MGLMapView, viewFor annotation: MGLAnnotation) -> MGLAnnotationView? {
        let reuseIdentifier = "Parking"
        var image = #imageLiteral(resourceName: "icon_parking_spot")
        if let ann = annotation as? MapAnnotation, let img = ann.image {
            image = img
        }
        var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: reuseIdentifier)
        if annotationView == nil {
            annotationView = MapAnnotationView(reuseIdentifier: reuseIdentifier, image: image)
        }
        
        return annotationView
    }
    
    func mapView(_ mapView: MGLMapView, fillColorForPolygonAnnotation annotation: MGLPolygon) -> UIColor {
        return UIColor.red.withAlphaComponent(0.06)
    }
    
    func mapView(_ mapView: MGLMapView, strokeColorForShapeAnnotation annotation: MGLShape) -> UIColor {
        return UIColor.red.withAlphaComponent(0.5)
    }
}

extension ParkingZonesViewController {
    func add(zone: ParkingZone) {
        func add(polygon: [CLLocationCoordinate2D]) {
            var bounds = polygon
            let shape = MGLPolygon(coordinates: &bounds, count: UInt(polygon.count))
            mapView.addAnnotation(shape)
            self.coordinates.append(contentsOf: polygon)
        }
        
        func add(circle: ParkingZone.Circle) {
            let polygon = circle.polygon()
            mapView.addAnnotation(polygon.0)
            self.coordinates.append(contentsOf: polygon.1)
        }
        
        switch zone.geometry {
        case .polygon(let coordinates), .rectangle(let coordinates):
            add(polygon: coordinates)
        case .circle(let circle):
            add(circle: circle)
        default:
            break
        }
    }
    
    func adjustZoom(with coordinates: [CLLocationCoordinate2D] = []) {
        self.coordinates += coordinates
        guard let bounds = MGLCoordinateBounds(coordinates: self.coordinates) else { return }
        let camera = mapView.cameraThatFitsCoordinateBounds(bounds, edgePadding: UIEdgeInsets(top: 50, left: 30, bottom: 10, right: 30))
        mapView.setCamera(camera, animated: true)
    }
}

extension ParkingZone.Circle {
    func polygon() -> (MGLPolygon, [CLLocationCoordinate2D]) {
        let degreesBetweenPoints = 8.0
        //45 sides
        let numberOfPoints = floor(360.0 / degreesBetweenPoints)
        let distRadians: Double = radius / 6371000.0
        // earth radius in meters
        let centerLatRadians: Double = center.latitude * Double.pi / 180
        let centerLonRadians: Double = center.longitude * Double.pi / 180
        var coordinates = [CLLocationCoordinate2D]()
        //array to hold all the points
        for index in 0 ..< Int(numberOfPoints) {
            let degrees: Double = Double(index) * Double(degreesBetweenPoints)
            let degreeRadians: Double = degrees * Double.pi / 180
            let pointLatRadians: Double = asin(sin(centerLatRadians) * cos(distRadians) + cos(centerLatRadians) * sin(distRadians) * cos(degreeRadians))
            let pointLonRadians: Double = centerLonRadians + atan2(sin(degreeRadians) * sin(distRadians) * cos(centerLatRadians), cos(distRadians) - sin(centerLatRadians) * sin(pointLatRadians))
            let pointLat: Double = pointLatRadians * 180 / Double.pi
            let pointLon: Double = pointLonRadians * 180 / Double.pi
            let point: CLLocationCoordinate2D = CLLocationCoordinate2DMake(pointLat, pointLon)
            coordinates.append(point)
        }
        let polygon = MGLPolygon(coordinates: &coordinates, count: UInt(coordinates.count))
        return (polygon, coordinates)
    }
}

extension MGLCoordinateBounds {
    init?(coordinates: [CLLocationCoordinate2D]) {
        let filtered = coordinates.filter({ CLLocationCoordinate2DIsValid($0) })
        guard filtered.isEmpty == false else { return nil }
        
        var sw = kCLLocationCoordinate2DInvalid
        var ne = kCLLocationCoordinate2DInvalid
        sw.latitude = filtered.map({ $0.latitude }).max()!//37.791905229074075
        sw.longitude = filtered.map({ $0.longitude }).min()! //-122.48287349939346
        ne.latitude = filtered.map({ $0.latitude }).min()! //37.74427534910713
        ne.longitude = filtered.map({ $0.longitude }).max()!//-122.39103466272354
        self = MGLCoordinateBounds(sw: sw, ne: ne)
    }
}
