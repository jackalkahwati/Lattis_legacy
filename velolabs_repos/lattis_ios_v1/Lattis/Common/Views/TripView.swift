//
//  TripView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 8/24/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Mapbox

class TripView: UIView {
    
    fileprivate let mapView = MGLMapView(frame: .zero, styleURL: URL(string: "mapbox://styles/mapbox/light-v9"))
    fileprivate var camera: MGLMapCamera?
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        addSubview(mapView)
        mapView.delegate = self
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        mapView.frame = bounds
        if let camera = camera, mapView.camera != camera {
            mapView.setCamera(camera, animated: true)
        }
    }
    
    var trip: Trip? {
        didSet {
            if let coordinates = trip?.steps.map({$0.location})  {
                adjustMap(coordinates: coordinates)
            }
        }
    }
    
    fileprivate func adjustMap(coordinates: [CLLocationCoordinate2D]) {
        guard let bounds = MGLCoordinateBounds(coordinates: coordinates) else { return }
        let camera = mapView.cameraThatFitsCoordinateBounds(bounds, edgePadding: UIEdgeInsets(top: 30, left: 30, bottom: 30, right: 30))
        self.camera = camera
        mapView.setCamera(camera, animated: true)
        
        if let start = coordinates.first {
            let pin = MGLPointAnnotation()
            pin.title = "start"
            pin.coordinate = start
            mapView.addAnnotation(pin)
        }
        if let end = coordinates.last {
            let pin = MGLPointAnnotation()
            pin.title = "end"
            pin.coordinate = end
            mapView.addAnnotation(pin)
        }
        var cc = coordinates
        let line = MGLPolyline(coordinates: &cc, count: UInt(cc.count))
        mapView.addAnnotation(line)
    }
}

extension TripView: MGLMapViewDelegate {
    func mapView(_ mapView: MGLMapView, imageFor annotation: MGLAnnotation) -> MGLAnnotationImage? {
        if let title = annotation.title, let tt = title, tt == "start" {
            return MGLAnnotationImage(image: #imageLiteral(resourceName: "icon_map_start"), reuseIdentifier: tt)
        }
        var image = #imageLiteral(resourceName: "icon_map_end")
        image = image.withAlignmentRectInsets(UIEdgeInsets(top: 0, left: 20, bottom: 0, right: 0))
        return MGLAnnotationImage(image: image, reuseIdentifier: "end")
    }
    
    func mapView(_ mapView: MGLMapView, strokeColorForShapeAnnotation annotation: MGLShape) -> UIColor {
        return .lsTurquoiseBlue
    }
    
    func mapView(_ mapView: MGLMapView, alphaForShapeAnnotation annotation: MGLShape) -> CGFloat {
        return 0.57
    }
    
    func mapView(_ mapView: MGLMapView, lineWidthForPolylineAnnotation annotation: MGLPolyline) -> CGFloat {
        return 4
    }
}
