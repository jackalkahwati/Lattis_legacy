//
//  MapContainer.swift
//  Lattis
//
//  Created by Ravil Khusainov on 23/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Mapbox

class MapContainer: UIView {
    weak var mapView: MGLMapView!
    var touchTargetView: UIView?
    @IBOutlet weak var dropTouchView: UIView!
    
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        let view = super.hitTest(point, with: event)
        if view == dropTouchView {
            return touchTargetView
        }
        return view
    }
}
