//
//  ReservationLogicController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 13.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import Model

final class ReservationLogicController {
    let reservation: Reservation
    
    var pickUpDate: String {
        formatter.string(from: reservation.reservationStart)
    }
    var returnDate: String {
        formatter.string(from: reservation.reservationEnd)
    }
    var totalPrice: String? {
        guard let billing = reservation.tripPaymentTransaction else { return nil }
        return billing.total.price(for: billing.currency)
    }
    var parkingFee: String? {
        reservation.bike.fleet.paymentSettings?.parkingFee
    }
    
    fileprivate let formatter = DateFormatter()
    fileprivate let network: ReservationsNetwork & BikeAPI = AppRouter.shared.api()
    fileprivate var timer: Timer?
    fileprivate let timeFormatter = DateComponentsFormatter()
    fileprivate let cardStorage = CardStorage()
    
    init(_ reservation: Reservation) {
        self.reservation = reservation
        formatter.dateStyle = .long
        formatter.timeStyle = .short
        formatter.doesRelativeDateFormatting = true
        
    }
    
    func fetchCard(completion: @escaping (Payment.Card) -> ()) {
        cardStorage.fetch { [weak self] (cards) in
            self?.handle(cards: cards, completion: completion)
        }
    }
    
    func countdown(completion: @escaping (String?) -> Void) -> Bool {
        if reservation.canStartTrip {
            completion(nil)
            timer?.invalidate()
            return true
        } else {
            timer = .scheduledTimer(withTimeInterval: 1, repeats: true, block: { [weak self] _ in
                guard let date = self?.reservation.reservationStart else { return }
                let duration = date.timeIntervalSinceNow
                if duration <= 0 {
                    completion(nil)
                    self?.timer?.invalidate()
                    return
                }
                completion(self?.convert(duration: duration))
            })
            let duration = reservation.reservationStart.timeIntervalSinceNow
            completion(convert(duration: duration))
            return false
        }
    }
    
    fileprivate func convert(duration: TimeInterval) -> String? {
        if duration < .day {
            timeFormatter.allowedUnits = [.hour, .minute, .second]
            timeFormatter.unitsStyle = .positional
        } else {
            timeFormatter.allowedUnits = [.day, .hour, .minute]
            timeFormatter.unitsStyle = .short
        }
        return timeFormatter.string(from: duration)
    }
    
    fileprivate func handle(cards: [Payment.Card], completion: @escaping (Payment.Card) -> ()) {
        guard let billing = reservation.tripPaymentTransaction, let card = cards.first(where: { $0.cardId == billing.cardId }) else { return }
        completion(card)
    }
    
    func cancel(completion: @escaping (Error?) -> ()) {
        network.cancel(reservation: reservation) { (result) in
            switch result {
            case .success:
                completion(nil)
                NotificationCenter.default.post(name: .reservation, object: nil)
            case .failure(let error):
                completion(error)
            }
        }
    }
    
    func startTrip(completion: @escaping (Result<Trip, Error>) -> ()) {
        network.startTrip(reservation: reservation, completion: completion)
    }
}
