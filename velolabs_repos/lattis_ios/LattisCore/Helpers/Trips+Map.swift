//
//  Trips+Map.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 13.11.2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import MapboxStatic
import CoreLocation

extension TripAPI {
    func fetchMap(start: CLLocationCoordinate2D, finish: CLLocationCoordinate2D, size: CGSize, completion: @escaping (Result<UIImage, Error>) -> ()) {
        let startMarker = CustomMarker(coordinate: start, url: URL(string: "https://s3-us-west-1.amazonaws.com/lattis.production/markers/icon_trip_start_location.png")!)
        let endMarker = CustomMarker(coordinate: finish, url: URL(string: "https://s3-us-west-1.amazonaws.com/lattis.production/markers/icon_trip_end_location.png")!)
        let options = SnapshotOptions(styleURL: URL(string: "mapbox://styles/mapbox/light-v9")!, size: size)
        options.overlays = [startMarker, endMarker]
        let snapshot = Snapshot(options: options)
        _ = snapshot.image { (image, error) in
            if let e = error {
                completion(.failure(e))
            } else if let img = image {
                completion(.success(img))
            }
        }
    }
}
