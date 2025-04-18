//
//  RideHistoryDetailsRideHistoryDetailsInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 18/08/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import MapboxStatic

class RideHistoryDetailsInteractor {
    weak var view: RideHistoryDetailsInteractorOutput!
    var router: RideHistoryDetailsRouter!
    var trip: Trip!
}

extension RideHistoryDetailsInteractor: RideHistoryDetailsInteractorInput {
    func viewDidLoad() {
        view.show(trip: trip, snapshot: nil)
    }
    
    func requestSnapshot(size: CGSize) {
        guard let trip = self.trip, trip.steps.isEmpty == false else { return }
        let start = CustomMarker(coordinate: trip.steps.first!.location, url: URL(string: "https://s3-us-west-1.amazonaws.com/lattis.production/markers/icon_current_location.png")!)
        let end = CustomMarker(coordinate: trip.steps.last!.location, url: URL(string: "https://s3-us-west-1.amazonaws.com/lattis.production/markers/ride_summary_flag.png")!)
        let options = SnapshotOptions(styleURL: URL(string: "mapbox://styles/mapbox/light-v9")!, size: size)
        options.overlays = [start, end]
        let snapshot = Snapshot(options: options)
        _ = snapshot.image { [weak self] (image, error) in
            if let e = error {
                Analytics.report(e)
            }
            guard let img = image else { return }
            self?.view.present(snapshot: img)
        }
    }
}
