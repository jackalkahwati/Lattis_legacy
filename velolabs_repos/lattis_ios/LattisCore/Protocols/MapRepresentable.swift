//
//  MapRepresentable.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import CoreLocation

public protocol OverMap: UIViewController {
    var mapController: MapRepresentable? { get set }
    func didLayoutSubviews()
}

public protocol MapPoint {
    var coordinate: CLLocationCoordinate2D { get }
    var identifier: String { get }
    var title: String? { get }
    var subtitle: String? { get }
    var color: UIColor { get }
    var bage: Int? { get }
    var batteryLevel: Int? { get }
    func isEqual(to: MapPoint) -> Bool
}

public enum MapPolygonType {
    case parking
    case geofence
}

public protocol MapShape {
    var coordinates: [CLLocationCoordinate2D] { get }
    var polygonType: MapPolygonType { get }
}

public protocol MapRepresentable: UIViewController {
    init(_ rootController: OverMap)
    var location: CLLocation? { get }
    var mapView: UIView { get }
    var rootController: OverMap { get }
    var topController: MapTopViewController? { get set }
    func add(points: [MapPoint], selected: MapPoint?)
    func add(shapes: [MapShape])
    func select(point: MapPoint)
    func deselectPoint()
    func removeAllPoints()
    func centerOnUserLocation()
    func focus(on coordinate: CLLocationCoordinate2D)
    func focus(on coordinates: [CLLocationCoordinate2D])
    func update(contentInset: UIEdgeInsets)
    func coordinate(for point: CGPoint, in view: UIView) -> CLLocationCoordinate2D
}

