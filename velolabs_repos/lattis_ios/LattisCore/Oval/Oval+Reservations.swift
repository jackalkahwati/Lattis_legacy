//
//  Oval+Reservations.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 07.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import OvalAPI
import Model

fileprivate extension API {
    static func reservation(_ bikeId: Int) -> API {
        .init(path: "reservations?bike_id=\(bikeId)")
    }
    static func next(_ bikeId: Int) -> API {
        .init(path: "reservations/next-reservation?bike_id=\(bikeId)")
    }
    static func cancel(_ reservationId: Int) -> API {
        .init(path: "reservations/\(reservationId)/cancel")
    }
    static func bikes(_ request: Reservation.BikesRequest) -> API {
        .init(path: "reservations/available-vehicles" + request.string)
    }
    static func startTrip(_ reservation: Reservation) -> API {
        .init(path: "reservations/\(reservation.reservationId)/start-trip")
    }
    static var reservations: API { .init(path: "reservations") }
    static var estimate: API { .init(path: "reservations/cost-estimate") }
}

extension Session: ReservationsNetwork {
    func cancel(reservation: Reservation, completion: @escaping (Result<Reservation, Error>) -> ()) {
        send(.put(Empty(), api: .cancel(reservation.reservationId), dateAsTimestamp: false), completion: completion)
    }
    
    func estimate(request: Reservation.Request, completion: @escaping (Result<Reservation.Estimate, Error>) -> ()) {
        send(.post(json: request, api: .estimate, dateAsTimestamp: false), completion: completion)
    }
    
    func fetchReservations(completion: @escaping (Result<[Reservation], Error>) -> ()) {
        send(.get(.reservations, dateAsTimestamp: false), completion: completion)
    }
    
    func fetch(by bikeId: Int, completion: @escaping (Result<[Reservation], Error>) -> ()) {
        send(.get(.reservation(bikeId), dateAsTimestamp: false), completion: completion)
    }
    
    func fetchBikes(request: Reservation.BikesRequest, completion: @escaping (Result<[Model.Bike], Error>) -> ()) {
        send(.get(.bikes(request), dateAsTimestamp: false), completion: completion)
    }
    
    func nextReservation(by bikeId: Int, completion: @escaping (Result<Reservation?, Error>) -> ()) {
        send(.get(.next(bikeId), dateAsTimestamp: false), completion: { (result: Result<Reservation?, Error>) in
            switch result {
            case .success(let res):
                completion(.success(res))
            case .failure(let error):
                if let e = error as? SessionError, e.code == .unexpectedResponse {
                    completion(.success(nil))
                } else {
                    completion(.failure(error))
                }
            }
        })
    }
    
    func createReservation(reques: Reservation.Request, completion: @escaping (Result<Reservation, Error>) -> ()) {
        send(.post(json: reques, api: .reservations, dateAsTimestamp: false), completion: completion)
    }
    
    func startTrip(reservation: Reservation, completion: @escaping (Result<Trip, Error>) -> ()) {
        struct FakeTrip: Codable {
            let tripId: Int
        }
        send(.put(Empty(), api: .startTrip(reservation))) { (result: Result<FakeTrip, Error>) in
            switch result {
            case .failure(let error):
                completion(.failure(error))
            case .success(let trip):
                self.getTrip(by: trip.tripId) { details in completion(details.unwrap(\.trip)) }
            }
        }
    }
}

extension Result {
    func unwrap<T>(_ keyPath: KeyPath<Success, T>) -> Result<T, Error> {
        switch self {
        case .success(let t):
            return .success(t[keyPath: keyPath])
        case .failure(let error):
            return .failure(error)
        }
    }
}
