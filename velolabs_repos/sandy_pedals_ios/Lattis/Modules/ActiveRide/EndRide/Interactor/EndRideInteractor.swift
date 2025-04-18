//
//  EndRideEndRideInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 17/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import MapboxStatic

protocol EndRideInteractorDelegate: class {
    func endTrip(with image: UIImage?, completion: @escaping (Error?, Trip?) -> ())
    func didEndTrip(with rating: Int?)
}

class EndRideInteractor {
    weak var view: EndRideInteractorOutput!
    var router: EndRideRouter!
    weak var delegate: EndRideInteractorDelegate?
    var trip: Trip?
}

extension EndRideInteractor: EndRideInteractorInput {
    func submit(rating: Int) {
        router.dismiss()
        delegate?.didEndTrip(with: rating)
    }
    
    func didMake(picture: UIImage){
        view.startLoading(with: "active_ride_ending_trip".localized())
        delegate?.endTrip(with: picture) { [weak self] (error, trip) in
            self?.view.stopLoading() {
                if let error = error {
                    self?.handle(error: error)
                } else {
                    self?.router.pushController(with: .action, configure: { interactor in
                        interactor.delegate = self?.delegate
                        interactor.trip = trip
                    })
                }
            }
        }
    }
    
    func viewLoaded() {
        guard let trip = self.trip else { return }
        self.view.show(trip: trip)
    }
    
    func openPayments() {
        router.openPayments()
    }
    
    func dismiss() {
        router.dismiss()
    }
    
    func buildMap(size: CGSize) {
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
            self?.view.show(snapshot: img)
        }
    }
}

private extension EndRideInteractor {
    func handle(error: Error, file: String = #file, line: Int = #line) {
        view.show(error: error, file: file, line: line)
    }
}
