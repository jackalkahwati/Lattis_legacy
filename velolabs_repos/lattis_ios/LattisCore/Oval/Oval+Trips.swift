//
//  Oval+Trips.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 02/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import OvalAPI
import Model

fileprivate extension API {
    static func trips(_ path: Path) -> API {
        return .init(path: "trips/" + path.rawValue)
    }
    static func fleet(_ path: Path) -> API {
        return .init(path: "fleet/" + path.rawValue)
    }
    
    enum Path: String {
        case start = "start-trip"
        case details = "get-trip-details"
        case checkFee = "check-parking-fee"
        case end = "end-trip"
        case rate = "update-rating"
        case update = "update-trip"
        case all = "get-trips"
    }
}

extension Session: TripAPI {
    func getTrip(by tripId: Int, completion: @escaping (Result<Trip.Details, Error>) -> ()) {
        send(.post(json: ["trip_id": tripId], api: .trips(.details))) { (result: Result<Trip.Details, Error>) in
            switch result {
            case .success(let wrap):
                var trip = wrap.trip
                if let bikeId = trip.bikeId {
                    self.getBike(by: bikeId, qrCodeId: nil, iotCode: nil, completion: { (result) in
                        switch result {
                        case .success(let bike):
                            trip.bike = bike
                            completion(.success(.init(trip: trip, hub: nil)))
                        case .failure(let e):
                            completion(.failure(e))
                        }
                    })
                } else {
                    completion(.success(wrap))
                }
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func startTrip(with start: Trip.Start, completion: @escaping (Result<Trip, Error>) -> ()) {
        struct Wrap: Decodable {
            let tripId: Int
            let doNotTrackTrip: Bool?
        }
        send(.post(json: start, api: .trips(.start))) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let wrap):
                self.getTrip(by: wrap.tripId) { result in
                    switch result {
                    case .success(let details):
                        completion(.success(details.trip))
                    case .failure(let error):
                        completion(.failure(error))
                    }
                }
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func checkParkingFee(check: Parking.Check, completion: @escaping (Result<Parking.Fee, Error>) -> ()) {
        send(.post(json: check, api: .fleet(.checkFee)), completion: completion)
    }
    
    func end(trip: Trip.End, completion: @escaping (Result<Trip, Error>) -> ()) {
        send(.post(json: trip, api: .trips(.end)), completion: completion)
    }
    
    func rate(trip: Trip.Rating, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: trip, api: .trips(.rate)), completion: completion)
    }
    
    func update(trip: Trip.Update, completion: @escaping (Result<Trip.Invoice, Error>) -> ()) {
        send(.post(json: trip, api: .trips(.update)), completion: completion)
    }
    
    func getTrips(completion: @escaping (Result<[Trip], Error>) -> ()) {
        send(.get(.trips(.all))) { (res: Result<[FailableDecodable<Trip, FailedTrip>], Error>) in
            switch res {
            case .failure(let error):
                completion(.failure(error))
            case .success(let trips):
                let failed = trips.compactMap(\.alter).map({"\($0)"})
                Analytics.report(nil, with: ["trips": failed.joined(separator: ",")])
                completion(.success(trips.compactMap(\.base)))
            }
        }
//        send(.get(.trips(.all)), completion: completion)
    }
}


struct FailableDecodable<Base : Decodable, Alter: Decodable> : Decodable {

    let base: Base?
    var alter: Alter?

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        self.base = try? container.decode(Base.self)
        self.alter = try? container.decode(Alter.self)
        if base != nil {
            self.alter = nil
        }
    }
}

struct FailedTrip: Decodable {
    let tripId: Int
}
