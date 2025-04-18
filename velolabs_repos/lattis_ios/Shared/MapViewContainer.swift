//
//  MapViewContainer.swift
//  Lattis
//
//  Created by Ravil Khusainov on 18/04/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Mapbox
import Cartography
import LattisCore
import ClusterKit

class MapViewContainer: UIViewController {
    
    let rootController: OverMap
    weak var topController: MapTopViewController? {
        didSet {
            guard let location = map.userLocation?.location else { return }
            topController?.didUpdateUseer(location: location)
        }
    }
    weak var selectedAnnotationView: ZoomableAnnotationView?
    var selectedPoint: MapPoint?
    let map: MGLMapView = .init(frame: .zero, styleURL: URL(string: "mapbox://styles/mapbox/light-v9"))
    var mapView: UIView { return map }
    var visibleMarkers = [AnnotationView]()
    
    fileprivate var cameraInsets: UIEdgeInsets = .init(top: 94, left: 50, bottom: 120, right: 50)
    fileprivate var userFolowed = false
    fileprivate var deselectTapGesture: UITapGestureRecognizer?
    fileprivate let observer: MapObserver = .shared
    
    required init(_ rootController: OverMap) {
        self.rootController = rootController
        super.init(nibName: nil, bundle: nil)
        rootController.mapController = self
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }

        view.backgroundColor = .white
        map.clusterManager.algorithm = CKNonHierarchicalDistanceBasedAlgorithm()
        map.delegate = self
        map.showsUserLocation = true
//        map.userTrackingMode = .follow
        map.allowsTilting = false
        map.allowsRotating = false
        map.showsUserHeadingIndicator = true
        map.logoView.alpha = 0.0
        map.attributionButton.alpha = 0.0
        view.addSubview(map)
        constrain(map, view) { $0.edges == $1.edges }
        rootController.willMove(toParent: self)
        addChild(rootController)
        view.addSubview(rootController.view)
        rootController.didMove(toParent: self)
        constrain(rootController.view, view) { nav, view in
            nav.edges == view.edges
        }
        
        map.automaticallyAdjustsContentInset = false
        
        deselectTapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTap))
        deselectTapGesture?.delegate = self
    }
    
    func focus(on annotation: MGLAnnotation) {
        if let buffer = map.view(for: annotation) as? ZoomableAnnotationView {
            if let s = selectedAnnotationView, s === buffer { return }
            selectedAnnotationView?.zoomOut()
            selectedAnnotationView = buffer
            selectedAnnotationView?.zoomIn(animated: true)
            
        }
        mapView.addGestureRecognizer(deselectTapGesture!)
    }
    
    @objc
    fileprivate func handleTap() {
        deselectPoint()
        topController?.didTapOnMap()
    }
}

extension MapViewContainer: UIGestureRecognizerDelegate {
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
        true
    }
}

extension MapViewContainer: MapRepresentable {
    
    var location: CLLocation? { map.userLocation?.location }
    
    func focus(on coordinate: CLLocationCoordinate2D) {
        map.setCenter(coordinate, animated: true)
    }
    
    func add(shapes: [MapShape]) {
        let ann: [Polygon] = shapes.map { (shape) in
            let coordinates = shape.coordinates
            var c = coordinates
            let polygon: Polygon = .init(coordinates: &c, count: UInt(coordinates.count))
            polygon.shapeType = shape.polygonType
            return polygon
        }
        map.addAnnotations(ann)
    }
    
    func removeAllPoints() {
        DispatchQueue.main.async {
            self.selectedPoint = nil
            self.selectedAnnotationView = nil
            if let old = self.map.annotations, !old.isEmpty {
                self.map.removeAnnotations(old)
            }
            let old = self.map.clusterManager.annotations
            if !old.isEmpty {
                self.map.clusterManager.removeAnnotations(old)
            }
        }
    }
    
    func update(contentInset: UIEdgeInsets) {
        cameraInsets = contentInset
        map.contentInset = contentInset
//        guard map.contentInset != contentInset else { return }
//        map.setContentInset(contentInset, animated: true)
    }
    
    func centerOnUserLocation() {
        guard let center = map.userLocation?.coordinate else { return }
        let alt = MGLAltitudeForZoomLevel(17, map.camera.pitch, center.latitude, map.frame.size)
        let camera = MGLMapCamera(lookingAtCenter: center, altitude: alt, pitch: map.camera.pitch, heading: map.camera.heading)
        camera.centerCoordinate = center
        map.setCamera(camera, animated: true)
    }
    
    func add(points: [MapPoint], selected: MapPoint?) {
        selectedPoint = selected
        DispatchQueue.global().async {
            let oldAnnotations = self.map.clusterManager.annotations as? [Annotation] ?? []
            var toRemove: [Annotation] = []
            let annotations = points.filter({ point in
                if let old = oldAnnotations.first(where: {$0.value.isEqual(to: point)}) {
                    if old.coordinate == point.coordinate {
                        return false
                    } else {
                        toRemove.append(old)
                        return true
                    }
                } else {
                    return true
                }
            }).map(Annotation.init)
            DispatchQueue.main.async {
                self.map.clusterManager.removeAnnotations(toRemove)
                self.map.clusterManager.addAnnotations(annotations)
            }
        }
        selectedAnnotationView?.zoomIn(animated: true)
    }
    
    func select(point: MapPoint) {
        guard let annotation = map.annotations?.filter({ ann in
            if let cluster = ann as? CKCluster {
                return cluster.annotations.contains(where: {$0.coordinate == point.coordinate})
            }
            return ann.coordinate == point.coordinate
        }).first else { return }
        focus(on: annotation)
    }
    
    func coordinate(for point: CGPoint, in view: UIView) -> CLLocationCoordinate2D {
        return map.convert(point, toCoordinateFrom: view)
    }
    
    func deselectPoint() {
        selectedAnnotationView?.zoomOut()
        selectedAnnotationView = nil
        mapView.removeGestureRecognizer(deselectTapGesture!)
    }
    
    func focus(on coordinates: [CLLocationCoordinate2D]) {
        guard let bounds = MGLCoordinateBounds(coordinates: coordinates) else { return }
        let camera = map.cameraThatFitsCoordinateBounds(bounds, edgePadding: cameraInsets)
        map.setCamera(camera, animated: true)
    }
}

extension MapViewContainer: MGLMapViewDelegate {
    
    func mapView(_ mapView: MGLMapView, didSelect annotation: MGLAnnotation) {
        guard let cluster = annotation as? CKCluster else { return }
        guard let ann = cluster.annotations.first as? Annotation else { return }
        observer.selected.send(ann.value)
        
        guard let top = topController, top.canSelectPoint() else { return }
        focus(on: annotation)
        if cluster.count > 1 {
            let camera = mapView.cameraThatFitsCluster(cluster, edgePadding: .init(top: 94, left: 60, bottom: 120, right: 60))
            mapView.setCamera(camera, animated: true)
            return
        }
        topController?.mapDidSelect(point: ann.value)
    }
    
    func mapView(_ mapView: MGLMapView, viewFor annotation: MGLAnnotation) -> MGLAnnotationView? {
        if annotation is MGLUserLocation && mapView.userLocation != nil {
            var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: "userLocationAnnotationView") as? UserLocationView
            if annotationView == nil {
                annotationView = UserLocationView(reuseIdentifier: "userLocationAnnotationView")
            }
            return annotationView
        }
        guard let cluster = annotation as? CKCluster else { return nil }
        if cluster.annotations.count > 1 {
            visibleMarkers.removeAll()
            return ClusterView(cluster: cluster)
        }
        guard let an = cluster.annotations.first as? Annotation else { return nil }
        var view = mapView.dequeueReusableAnnotationView(withIdentifier: an.value.identifier) as? AnnotationView
        view = AnnotationView(annotation: an)
        // check if duplicate coordinates then offset the marker
        if visibleMarkers.contains(where: { $0.currentAnnotation.coordinate == view!.currentAnnotation.coordinate }) {
        let offset = CGVector(dx: 50, dy: 50)
            view?.centerOffset = offset
        } else {
            visibleMarkers.append(view!)
        }
        if let point = selectedPoint, an.value.isEqual(to: point) {
            selectedPoint = nil
            view?.zoomIn(animated: false)
            topController?.mapDidSelect(point: point)
            selectedAnnotationView = view
        }
        return view
    }
    
    func mapView(_ mapView: MGLMapView, regionWillChangeWith reason: MGLCameraChangeReason, animated: Bool) {
        let idGesture: Bool = reason != .programmatic
        topController?.mapWillMove(byGesture: idGesture)
    }
    
    func mapView(_ mapView: MGLMapView, regionDidChangeWith reason: MGLCameraChangeReason, animated: Bool) {
        let idGesture: Bool = reason != .programmatic
        map.clusterManager.updateClustersIfNeeded()
        topController?.mapDidMove(byGesture: idGesture)
    }
    
    func mapView(_ mapView: MGLMapView, didUpdate userLocation: MGLUserLocation?) {
        guard let location = userLocation?.location else { return }
        topController?.didUpdateUseer(location: location)
        if !userFolowed {
            userFolowed = true
            centerOnUserLocation()
        }
    }
    
    func mapView(_ mapView: MGLMapView, didFailToLocateUserWithError error: Error) {
        print(error)
    }
    
    func mapView(_ mapView: MGLMapView, fillColorForPolygonAnnotation annotation: MGLPolygon) -> UIColor {
        guard let poly = annotation as? Polygon else { return .clear }
        switch poly.shapeType {
        case .geofence:
            return UIColor.dodgerBlue.withAlphaComponent(0.2)
        default:
            return UIColor.red.withAlphaComponent(0.2)
        }
    }
    
    func mapView(_ mapView: MGLMapView, strokeColorForShapeAnnotation annotation: MGLShape) -> UIColor {
        guard let poly = annotation as? Polygon else { return .clear }
        switch poly.shapeType {
        case .geofence:
            return UIColor(white: 0, alpha: 0.8)
        default:
            return UIColor.red.withAlphaComponent(0.2)
        }
    }
    
    func mapView(_ mapView: MGLMapView, shouldChangeFrom oldCamera: MGLMapCamera, to newCamera: MGLMapCamera) -> Bool {
        mapView.camera = newCamera
        let bounds = mapView.visibleCoordinateBounds
        mapView.camera = oldCamera
        return bounds.ne.longitude <= 180 && bounds.sw.longitude >= -180
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

fileprivate class UserLocationView: MGLUserLocationAnnotationView {
    
    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        isUserInteractionEnabled = false
        isEnabled = false
        layer.zPosition = -1
        let imageView = UIImageView(image: UIImage(named: "icon_map_location"))
        addSubview(imageView)
        constrain(imageView, self) { image, view in
            image.edges == view.edges
            view.height == 22
            view.width == view.height
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class Polygon: MGLPolygon {
    var shapeType: MapPolygonType = .parking
}
