//
//  NewReservationLogicController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 07.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import Model

extension Reservation {
    struct Price {
        let price: String?
        let parkingFee: String?
        let card: Payment.Card?
    }
    enum State {
        case estimate(Price?)
        case warning(String, String)
        case confirmation
        case failure(Error)
        case loading(String?)
        case cardRequred
        case bikes([Model.Bike])
        case startDate(String)
        case endDate(String)
    }
}

final class NewReservationLogicController {
    
    let fleet: Model.Fleet
    let settings: Reservation.Settings
    var legalUrl: String? { fleet.legal }
    var pricing: String? {
        guard let options = fleet.pricingOptions, !options.isEmpty, let sel = selectedPricing else { return fleet.paymentSettings?.price }
        let opt = options.first(where: {$0.pricingOptionId == sel})
        return opt?.title
    }
    fileprivate(set) var bike: Model.Bike?
    fileprivate(set) var startAt: Date?
    fileprivate(set) var endAt: Date?
    fileprivate(set) var selectedPricing: Int?
    fileprivate var payPerUse: Bool = false
    fileprivate let dateFormatter = DateFormatter()
    fileprivate var card: Payment.Card?
    fileprivate let storage = CardStorage()
        
    var minDateStart: Date {
        Date().rounded(minutes: 30, rounding: .ceil)
    }
    var maxDateStart: Date {
        minDateStart.addingTimeInterval(settings.bookingWindowDuration)
    }
    var minDateEnd: Date {
        if let star = startAt {
            return star.addingTimeInterval(settings.minDuration)
        }
        return minDateStart.addingTimeInterval(settings.minDuration)
    }
    var maxDateEnd: Date {
        if let start = startAt {
            return start.addingTimeInterval(settings.maxDuration)
        }
        return maxDateStart.addingTimeInterval(settings.maxDuration)
    }
    var hasPricingOptions: Bool {
        guard let options = fleet.pricingOptions, !options.isEmpty else { return false }
        return true
    }
    
    fileprivate var request: Reservation.Request?
    fileprivate let network: ReservationsNetwork = AppRouter.shared.api()

    init(fleet: Model.Fleet, settings: Reservation.Settings) {
        self.fleet = fleet
        self.settings = settings
        dateFormatter.dateStyle = .medium
        dateFormatter.timeStyle = .short
        dateFormatter.doesRelativeDateFormatting = true
        fetchCard()
    }
    
    func set(start: Date? = nil, end: Date? = nil, completion: @escaping (Reservation.State) -> ()) {
        if let s = start {
            startAt = s
            completion(.startDate(dateFormatter.string(from: s)))
            if endAt != nil {
                endAt = nil
                completion(.endDate("select_date_time".localized()))
            }
        }
        if let e = end {
            endAt = e
            completion(.endDate(dateFormatter.string(from: e)))
        }
        estimate(completion: completion)
    }
    
    func set(bike: Model.Bike, completion: @escaping (Reservation.State) -> ()) {
        self.bike = bike
        estimate(completion: completion)
    }
    
    func set(pricing: Int?, completion: @escaping (Reservation.State) -> ()) {
        selectedPricing = pricing
        payPerUse = pricing == nil
        estimate(completion: completion)
    }
    
    fileprivate func fetchCard() {
        storage.fetch { [weak self] (cards) in
            self?.card = cards.first(where: {Payment.card($0).isCurrent})
        }
    }
    
    func estimate(completion: @escaping (Reservation.State) -> ()) {
        guard let start = startAt, let end = endAt else { return }
        guard let id = bike?.bikeId else { return }
        if hasPricingOptions && selectedPricing == nil && !payPerUse { return }
        self.request = nil
        let request = Reservation.Request(bikeId: id, reservationStart: start, reservationEnd: end, pricingOptionId: selectedPricing)
        network.estimate(request: request) { [weak self] (result) in
            switch result {
            case .success(let estimate):
                self?.request = request
                completion(
                    .estimate(.init(price: estimate.price, parkingFee: self?.fleet.paymentSettings?.parkingFee, card: self?.card))
                )
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    func confirm(completion: @escaping (Reservation.State) -> ()) {
        guard let request = request else {
            return completion(.warning("general_error_title".localized(), "please_select_reservation_dates".localized()))
        }
        if let _ = bike, fleet.isPayment && card == nil {
            return completion(.cardRequred)
        }
        completion(.loading("loading".localized()))
        network.createReservation(reques: request) { (result) in
            switch result {
            case .success(let reservation):
                NotificationCenter.default.post(name: .reservation, object: reservation)
                completion(.confirmation)
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    func fetchBikes(completion: @escaping (Reservation.State) -> ()) {
        guard let start = startAt, let end = endAt else {
            return completion(.warning("warning".localized(), "please_select_reservation_dates".localized()))
        }
        completion(.loading("loading".localized()))
        network.fetchBikes(request: .init(fleetId: fleet.fleetId, reservationStart: start, reservationEnd: end)) { (result) in
            switch result {
            case .success(let bikes):
                completion(.bikes(bikes))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
}

enum DateRoundingType {
    case round
    case ceil
    case floor
}

extension Date {
    func rounded(minutes: TimeInterval, rounding: DateRoundingType = .round) -> Date {
        return rounded(seconds: minutes * 60, rounding: rounding)
    }
    func rounded(seconds: TimeInterval, rounding: DateRoundingType = .round) -> Date {
        var roundedInterval: TimeInterval = 0
        switch rounding  {
        case .round:
            roundedInterval = (timeIntervalSinceReferenceDate / seconds).rounded() * seconds
        case .ceil:
            roundedInterval = ceil(timeIntervalSinceReferenceDate / seconds) * seconds
        case .floor:
            roundedInterval = floor(timeIntervalSinceReferenceDate / seconds) * seconds
        }
        return Date(timeIntervalSinceReferenceDate: roundedInterval)
    }
}
