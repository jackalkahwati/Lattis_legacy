//
//  MapViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 23/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Mapbox
//import MapboxCoreNavigation
import MapKit

protocol MapContaining: BaseInteractorOutput {
    var mapContainer: MapContainer {get}
    var mapNavigation: MapRepresenting? {get set}
    var canSelectAnnotations: Bool {get}
    func didSelect(annotation: MapAnnotation)
    func didUnselect(annotation: MapAnnotation?)
    func didUpdate(userLocation: CLLocation)
    func mapView(_ mapView: MGLMapView, viewFor annotation: MGLAnnotation) -> MGLAnnotationView?
    func navigationReached()
    func currentLocation(isVisible: Bool)
}

extension MapContaining {
    func didUpdate(userLocation: CLLocation) {}
    func didSelect(annotation anntation: MapAnnotation) {}
    func didUnselect(annotation anntation: MapAnnotation?) {}
    func mapView(_ mapView: MGLMapView, viewFor annotation: MGLAnnotation) -> MGLAnnotationView? { return nil }
    func navigationReached() {} //FIXME:
    var canSelectAnnotations: Bool {
        return false
    }
    func currentLocation(isVisible: Bool) {}
}

protocol MapRepresenting: class {
    var isSelectMode: Bool {get}
    var mapView: MGLMapView! {get set}
    var coordinatesToShow: [CLLocationCoordinate2D]? {get set}
    func push(_ controller: MapContaining, animated: Bool, replace: Bool)
    @discardableResult func pop(animated: Bool) -> MapContaining?
    func unselectAnnotation(showRest: Bool)
    func clearSelection()
    func navigate(to point: MapAnnotation)
    func stopNavigation()
    var followUser: Bool {get set}
    func select(annotationWith model: AnnotationModel, filter: (MapAnnotation) -> Bool)
    func navigateToUserLocation()
    func show(zones: [ParkingZone])
}

class MapViewController: UIViewController {
    @IBOutlet weak var mapView: MGLMapView!
    @IBOutlet weak var selectContainer: UIView!
    @IBOutlet weak var selectPin: UIImageView!
    @IBOutlet weak var shadowView: UIImageView!
    fileprivate let locationManager = CLLocationManager()
    
    var coordinatesToShow: [CLLocationCoordinate2D]? = nil
    var state: State = .map {
        didSet {
            switch state {
            case .callout(let annotation):
                select(annotation: annotation)
                isSelectMode = true
            default:
                isSelectMode = false
                break
            }
        }
    }
    
    var followUser: Bool = false {
        didSet {
            mapView.userTrackingMode = followUser ? .followWithCourse : .follow
        }
    }

    fileprivate(set) var isSelectMode: Bool = false
    fileprivate var controllers: [MapContaining] = []
    fileprivate var rootController: MapContaining
    fileprivate var selectedAnnotation: MapAnnotation?
    fileprivate var topController: MapContaining {
        return controllers.last ?? rootController
    }
//    fileprivate var navigation: RouteController? { didSet { resumeNotifications() }}
    init(_ rootController: MapContaining) {
        self.rootController = rootController
        super.init(nibName: "MapViewController", bundle: nil)
        add(child: rootController)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        locationManager.delegate = self
        mapView.compassView.isHidden = true
        mapView.delegate = self
        mapView.userTrackingMode = .follow
        topController.mapNavigation = self
        topController.mapContainer.mapView = mapView
        topController.mapContainer.touchTargetView = mapView
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(true, animated: true)
    }
    
    fileprivate func add(child: MapContaining) {
        guard let child = child as? UIViewController else { return }
        
        child.willMove(toParent: self)
        addChild(child)
        view.addSubview(child.view)
        child.view.translatesAutoresizingMaskIntoConstraints = false
        child.view.constrainEdges(to: view)
        child.didMove(toParent: self)
    }
    
    fileprivate func remove(child: MapContaining) {
        guard let child = child as? UIViewController else { return }
        
        child.view.removeFromSuperview()
        child.removeFromParent()
    }
    
    fileprivate func move(from: MapContaining, to: MapContaining, animated: Bool, forward: Bool) {
        var mutable = from
        guard let from = from as? UIViewController, let to = to as? UIViewController else {
            return
        }
        to.willMove(toParent: self)
        addChild(to)
        view.addSubview(to.view)
        to.view.alpha = 0
        to.view.frame = view.bounds
        to.didMove(toParent: self)
        
        
        let layouts = view.constraints.filter({ $0.firstItem as? UIView == from.view })
        view.removeConstraints(layouts)
        from.view.frame = view.bounds
        from.view.translatesAutoresizingMaskIntoConstraints = true
        if forward {
            view.bringSubviewToFront(from.view)
        }
        
        var toFrame = to.view.frame
        toFrame.origin.x = toFrame.width * (forward ? 1 : -0.3)
        to.view.frame = toFrame
        toFrame.origin.x = 0
        
        func finish() {
            mutable.mapNavigation = nil
            from.view.removeFromSuperview()
            from.removeFromParent()
            
            to.view.translatesAutoresizingMaskIntoConstraints = false
            to.view.constrainEdges(to: self.view)
        }
        
        func action() {
            from.view.alpha = 0
            to.view.alpha = 1
            to.view.frame = toFrame
            from.view.frame = {
                var frame = from.view.frame
                frame.origin.x = frame.width * (forward ? -0.3 : 1)
                return frame
            }()
        }
        
        if animated {
            UIView.animate(withDuration: 0.3, delay: 0, options: .curveEaseIn, animations: {
                action()
            }, completion: { _ in
                finish()
            })
        } else {
            action()
            finish()
        }
    }
    
    private func select(annotation: MapAnnotation) {
        guard let view = topController.mapView(mapView, viewFor: annotation) else { return }
        
        func action() {
            if let coord = coordinatesToShow?.filter({CLLocationCoordinate2DIsValid($0)}) {
                mapView.userTrackingMode = .none
                let camera = MGLMapCamera.camera(by: coord, in: mapView, with: UIEdgeInsets(top: 114, left: 30, bottom: 230, right: 30))
                if camera.isEqual(to: mapView.camera) {
                    camera.altitude += 0.00001
                }
                mapView.setCamera(camera, animated: true)
                self.selectedAnnotation = annotation
            }
            else {
                selectPin.image = annotation.image
                selectPin.frame = {
                    var frame = view.frame
                    frame.size.height /= 2
                    let point = mapView.convert(annotation.coordinate, toPointTo: selectContainer)
                    frame.origin.x = point.x - frame.width*0.5
                    frame.origin.y = point.y - frame.height
                    return frame
                }()
                self.shadowView.center = CGPoint(x: self.selectPin.frame.midX, y: self.selectPin.frame.maxY)
                selectPin.isHidden = false
                shadowView.isHidden = false
                mapView.removeAnnotation(annotation)
                mapView.setCenter(annotation.coordinate, animated: true)
                UIView.animate(withDuration: .defaultAnimation) {
                    self.selectPin.frame = {
                        var frame = self.selectPin.frame
                        frame.size.width *= 2
                        frame.size.height *= 2
                        frame.origin.x = self.selectContainer.bounds.midX - frame.width*0.5
                        frame.origin.y = self.selectContainer.bounds.midY - frame.height*0.75
                        return frame
                    }()
                    self.shadowView.center = CGPoint(x: self.selectPin.frame.midX, y: self.selectPin.frame.maxY)
                }
                self.selectedAnnotation = annotation
            }
           
            topController.didSelect(annotation: annotation)
        }
        
        if let selected = selectedAnnotation {
            mapView.addAnnotation(selected)
        }
        action()
    }
    

    @IBAction func unselect(_ sender: Any) {
        if let gesture = sender as? UITapGestureRecognizer, gesture.state != .ended  { return }
        unselectAnnotation(showRest: sender is UIGestureRecognizer)
    }
    
    fileprivate var navigationPoint: MapAnnotation?
}

extension MapViewController: MapRepresenting {
    func push(_ controller: MapContaining, animated: Bool, replace: Bool) {
        clearSelection()
        controller.mapNavigation = self
        move(from: topController, to: controller, animated: animated, forward: true)
        controller.mapContainer.touchTargetView = mapView
        controller.mapContainer.mapView = mapView
        if let location = mapView.userLocation?.location {
            controller.didUpdate(userLocation: location)
        }
        if replace {
            controllers.removeAll()
            rootController = controller
        } else {
            controllers.append(controller)
        }
    }
    
    @discardableResult func pop(animated: Bool) -> MapContaining? {
        guard let top = controllers.popLast() else { return nil }
        clearSelection()
        topController.mapNavigation = self
        move(from: top, to: topController, animated: animated, forward: false)
        return top
    }
    
    func unselectAnnotation(showRest: Bool) {
        topController.mapContainer.touchTargetView = mapView
        state = .map
        self.selectPin.frame = {
            var frame = self.selectPin.frame
            frame.size.width *= 0.5
            frame.size.height *= 0.5
            frame.origin.x += frame.width*0.5
            frame.origin.y += frame.height*0.75
            return frame
        }()
        self.shadowView.center = CGPoint(x: self.selectPin.frame.midX, y: self.selectPin.frame.maxY)
        self.selectPin.isHidden = true
        shadowView.isHidden = true
        if let annotation = self.selectedAnnotation, showRest {
            self.selectedAnnotation = nil
            self.mapView.addAnnotation(annotation)
        }
        topController.didUnselect(annotation: selectedAnnotation)
    }
    
    func clearSelection() {
        if case .callout(_) = state {
            state = .map
        }
        selectedAnnotation = nil
        unselect(self)
        if let annotations = mapView.annotations {
            mapView.removeAnnotations(annotations)
        }
    }
    
    
    func navigate(to point: MapAnnotation) {
        navigationPoint = point
//        buildRoute(annotation: point) { [weak self] (route) in
//            self?.navigation = RouteController(along: route, dataSource: <#RouterDataSource#>)
//        }
    }
    
    func stopNavigation() {
        navigationPoint = nil
        mapView.userTrackingMode = .follow
    }
    
    func select(annotationWith model: AnnotationModel, filter: (MapAnnotation) -> Bool) {
        guard case let .callout(a) = state else { return }
        guard let annotation = mapView.annotations?.compactMap({ $0 as? MapAnnotation }).first(where: filter),
            annotation != a else { return }
        state = .callout(annotation)
    }
    
    func navigateToUserLocation() {
        guard let location = mapView.userLocation else { return }
        mapView.setCenter(location.coordinate, zoomLevel: 15, animated: true)
    }
    
    func show(zones: [ParkingZone]) {
        zones.forEach{ add(zone: $0) }
    }
}

// MARK: - Navigation
private extension MapViewController {
    // Notification sent when the alert level changes.
    @objc func alertLevelDidChange(_ notification: NSNotification) {
//        let distance = notification.userInfo![RouteControllerAlertLevelDidChangeNotificationDistanceToEndOfManeuverKey] as! CLLocationDistance
//        if distance <= 1 {
//            topController.navigationReached()
//        }
    }
    
    // Notifications sent on all location updates
    @objc func progressDidChange(_ notification: NSNotification) {
//        let routeProgress = notification.userInfo![RouteControllerAlertLevelDidChangeNotificationRouteProgressKey] as! RouteProgress
//        addLine(for: routeProgress.route)
        mapView.userTrackingMode = .follow
    }
    
    // Notification sent when the user is determined to be off the current route
    @objc func rerouted(_ notification: NSNotification) {
//        guard let point = navigationPoint else { return }
    }
    
    func add(zone: ParkingZone) {
        func add(polygon: [CLLocationCoordinate2D]) {
            var bounds = polygon
            let shape = MGLPolygon(coordinates: &bounds, count: UInt(polygon.count))
            mapView.addAnnotation(shape)
        }
        
        func add(circle: ParkingZone.Circle) {
            mapView.addAnnotation(circle.polygon().0)
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
    
    func animateSelected(with annotation: MapAnnotation, and view: UIView) {
        coordinatesToShow = nil
        let point = mapView.convert(annotation.coordinate, toPointTo: selectContainer)
        selectPin.image = annotation.image
        selectPin.frame = {
            var frame = view.frame
            frame.size.height /= 2
            frame.origin.x = point.x - frame.width*0.5
            frame.origin.y = point.y - frame.height
            return frame
        }()
        self.shadowView.center = CGPoint(x: self.selectPin.frame.midX, y: self.selectPin.frame.maxY)
        selectPin.isHidden = false
        shadowView.isHidden = false
        mapView.removeAnnotation(annotation)
        mapView.setCenter(annotation.coordinate, animated: true)
        UIView.animate(withDuration: .defaultAnimation, animations: {
            self.selectPin.frame = {
                var frame = self.selectPin.frame
                frame.size.width *= 2
                frame.size.height *= 2
                frame.origin.x -= frame.width*0.25
                frame.origin.y -= frame.height*0.5
                return frame
            }()
            self.shadowView.center = CGPoint(x: self.selectPin.frame.midX, y: self.selectPin.frame.maxY)
        }, completion: { _ in
            self.selectedAnnotation = annotation
        })
    }
}

extension MapViewController: MGLMapViewDelegate {
    func mapView(_ mapView: MGLMapView, regionWillChangeAnimated animated: Bool) {
        guard selectedAnnotation != nil, coordinatesToShow == nil, animated == false else { return }
        unselectAnnotation(showRest: true)
    }
    
    func mapView(_ mapView: MGLMapView, regionDidChangeAnimated animated: Bool) {
        topController.currentLocation(isVisible: mapView.isUserLocationVisible)
        guard coordinatesToShow != nil, let annotation = selectedAnnotation, let view = topController.mapView(mapView, viewFor: annotation) else { return }
        animateSelected(with: annotation, and: view)
    }
    
    func mapView(_ mapView: MGLMapView, didUpdate userLocation: MGLUserLocation?) {
        guard let location = userLocation?.location else { return }
        
        topController.didUpdate(userLocation: location)
    }
    
    func mapView(_ mapView: MGLMapView, viewFor annotation: MGLAnnotation) -> MGLAnnotationView? {
        return topController.mapView(mapView, viewFor: annotation)
    }
    
    func mapView(_ mapView: MGLMapView, didSelect annotation: MGLAnnotation) {
        guard let annotation = annotation as? MapAnnotation,
            topController.canSelectAnnotations else { return }
        state = .callout(annotation)
    }
    
    func mapView(_ mapView: MGLMapView, fillColorForPolygonAnnotation annotation: MGLPolygon) -> UIColor {
        return UIColor.red.withAlphaComponent(0.06)
    }
    
    func mapView(_ mapView: MGLMapView, strokeColorForShapeAnnotation annotation: MGLShape) -> UIColor {
        return UIColor.red.withAlphaComponent(0.5)
    }
}

extension MapViewController: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        switch status {
        case .authorizedWhenInUse:
            mapView.userTrackingMode = .follow
        default:
            break
        }
    }
}

extension MapViewController {
    enum State {
        case map
        case callout(MapAnnotation)
    }
}


extension MKPolygon {
    func contains(_ coor: CLLocationCoordinate2D) -> Bool {
        let polygonRenderer = MKPolygonRenderer(polygon: self)
        let currentMapPoint: MKMapPoint = MKMapPoint(coor)
        let polygonViewPoint: CGPoint = polygonRenderer.point(for: currentMapPoint)
        return polygonRenderer.path.contains(polygonViewPoint)
    }
}

extension MGLMapCamera {
    class func camera(by coordinates: [CLLocationCoordinate2D], in mapView: MGLMapView, with padding: UIEdgeInsets) -> MGLMapCamera {
        guard coordinates.isEmpty == false else { return mapView.camera }
        var sw = kCLLocationCoordinate2DInvalid
        var ne = kCLLocationCoordinate2DInvalid
        sw.latitude = coordinates.map({ $0.latitude }).max()!//37.791905229074075
        sw.longitude = coordinates.map({ $0.longitude }).min()! //-122.48287349939346
        ne.latitude = coordinates.map({ $0.latitude }).min()! //37.74427534910713
        ne.longitude = coordinates.map({ $0.longitude }).max()!//-122.39103466272354
        let bounds = MGLCoordinateBounds(sw: sw, ne: ne)
        let camera = mapView.cameraThatFitsCoordinateBounds(bounds, edgePadding: padding)
        return camera
    }
}
