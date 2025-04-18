//
//  Direction.swift
//  Lattis
//
//  Created by Ravil Khusainov on 24/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import CoreLocation

struct Direction {
    var name: String?
    var address: String?
    var coordinate: CLLocationCoordinate2D = kCLLocationCoordinate2DInvalid
    var rating: Int32
    var placeId: String?
}

extension Direction: AnnotationModel {
    var image: UIImage? {
        return #imageLiteral(resourceName: "icon_parking")
    }
}
