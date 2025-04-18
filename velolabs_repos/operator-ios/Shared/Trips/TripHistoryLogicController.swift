//
//  TripHistoryLogicController.swift
//  Operator
//
//  Created by Ravil Khusainov on 04.04.2021.
//

import Foundation
import Combine


final class TripHistoryLogicController: ObservableObject {
    let vehicle: Vehicle
    @Published var trips: [Trip] = []
    fileprivate var cancellables: Set<AnyCancellable> = []
    
    init(_ vehicle: Vehicle) {
        self.vehicle = vehicle
        fetchTrips()
    }
    
    fileprivate func fetchTrips() {
        CircleAPI.trips(vehicleId: vehicle.id, active: false)
            .catch(fetchFailed)
            .assign(to: \.trips, on: self)
            .store(in: &cancellables)
    }
    
    fileprivate func fetchFailed(_ error: Error) -> Just<[Trip]> {
        print(error)
        return Just([])
    }
}
