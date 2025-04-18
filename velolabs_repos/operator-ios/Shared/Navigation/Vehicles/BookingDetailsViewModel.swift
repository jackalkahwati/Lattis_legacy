//
//  BookingDetailsViewModel.swift
//  Operator
//
//  Created by Ravil Khusainov on 14.06.2021.
//

import Foundation
import Combine

final class BookingDetailsViewModel: ObservableObject {
    let booking: Vehicle.Booking
    let vehicle: Vehicle
    var user: User { booking.user }
    var isfinished: Bool { booking.finishedAt != nil }
    fileprivate var storage: Set<AnyCancellable> = []
    
    init(_ booking: Vehicle.Booking, vehicle: Vehicle) {
        self.booking = booking
        self.vehicle = vehicle
    }
    
    func cancel(compleiton: @escaping () -> Void) {
        CircleAPI.cancel(booking: booking.id)
            .sink { result in
                switch result {
                case .finished:
                    compleiton()
                case .failure(let error):
                    print(error)
                }
            } receiveValue: {}
            .store(in: &storage)
    }
}
