//
//  ReservationsListLogicController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 13.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import Cartography
import Model

enum ReservationsState {
    case update
    case failure(Error)
}

final class ReservationsListLogicController {
    
    let dateFormatter: DateFormatter
    let timeFormatter: DateFormatter
    fileprivate(set) var reservations: [Reservation]
    
    fileprivate let network: ReservationsNetwork
    
    init(_ reservations: [Reservation]) {
        self.reservations = reservations
        dateFormatter = .init()
        timeFormatter = .init()
        network = AppRouter.shared.api()
        dateFormatter.dateStyle = .medium
        timeFormatter.timeStyle = .short
        dateFormatter.doesRelativeDateFormatting = true
    }
    
    func fetchReservations(completion: @escaping (ReservationsState) -> Void) {
        network.fetchReservations { [weak self] (result) in
            switch result {
            case .success(let reservations):
                self?.reservations = reservations
                completion(.update)
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
}
