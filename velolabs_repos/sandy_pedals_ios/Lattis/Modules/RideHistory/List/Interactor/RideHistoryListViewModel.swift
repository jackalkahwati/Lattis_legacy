//
//  RideHistoryListViewModel.swift
//  Lattis
//
//  Created by Ravil Khusainov on 8/18/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Oval

final class RideHistoryListViewModel: NSObject {
    
    var refresh: (Bool) -> () = {_ in}
    var onError: (Error) -> () = {_ in}
    
    fileprivate let sortPredicate: (Trip, Trip) -> Bool = { (trip0, trip1) -> Bool in
        if trip0.finishedAt != nil && trip1.finishedAt == nil {
            return true
        } else if let date1 = trip1.finishedAt, let date0 = trip0.finishedAt {
            return date0 > date1
        }
        return false
    }
    
    fileprivate let filterPredicate: (Trip) -> Bool = { trip in
        return trip.finishedAt != nil
    }
    
    fileprivate let network: TripNetwork = Session.shared
    fileprivate let storage: TripsStorage = CoreDataStack.shared
    fileprivate var trips: [Trip] = []
    
    func start() {
        storage.subsribe(target: self) { [unowned self] (trips) in
            self.trips = trips.filter(self.filterPredicate).sorted(by: self.sortPredicate)
            self.refresh(self.trips.isEmpty)
        }
        getTripsFromNetwork()
    }
    
    func stop() {
        storage.unsubscribe(target: self)
    }
    
    var tripsCount: Int {
        return trips.count
    }
    
    func trip(for indexPath: IndexPath) -> Trip {
        return trips[indexPath.row]
    }
    
    fileprivate func getTripsFromNetwork() {
        network.getTrips { [weak self] (result) in
            switch result {
            case .success(let trips):
                self?.storage.update(trips: trips)
            case .failure(let error):
                self?.onError(error)
            }
        }
    }
}
