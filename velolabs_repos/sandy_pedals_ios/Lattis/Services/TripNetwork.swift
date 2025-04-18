//
//  TripsNetwork.swift
//  Lattis
//
//  Created by Ravil Khusainov on 07/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import Oval
import CoreLocation

protocol TripNetwork {
    func start(trip: Trip, completion: @escaping (Result<(Int, Bool), Error>) -> ())
    func end(trip: Trip, location: CLLocation, with imageData: Data?, damage: Bool, completion: @escaping (Result<Trip, Error>) -> ())
    func update(trip: Trip, completion: @escaping (Result<Trip.Update, Error>) -> ())
    func getTrips(completion: @escaping (Result<[Trip], Error>) -> ())
    func rate(trip: Trip, with rating: Int, completion: @escaping (Result<Void, Error>) -> ())
    func getTrip(by tripId: Int, completion: @escaping (Result<Trip, Error>) -> ())
}

fileprivate extension API {
    static func trips(path: String) -> API {
        return .init(path: "trips/" + path)
    }
    static let start = trips(path: "start-trip")
    static let end = trips(path: "end-trip")
    static let update = trips(path: "update-trip")
    static let getTrips = trips(path: "get-trips")
    static let rate = trips(path: "update-rating")
    static let getTrip = trips(path: "get-trip-details")
}

extension Session: TripNetwork {
    func start(trip: Trip, completion: @escaping (Result<(Int, Bool), Error>) -> ()) {
        struct Res: Decodable {
            let tripId: Int
            let doNotTrackTrip: Bool?
        }
        send(.post(json: trip.start, api: .start)) { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                completion(.success((res.tripId, res.doNotTrackTrip ?? false)))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func end(trip: Trip, location: CLLocation, with imageData: Data?, damage: Bool, completion: @escaping (Result<Trip, Error>) -> ()) {
        func perform(image: URL? = nil) {
            send(.post(json: trip.end(image: image, damage: damage, location: location), api: .end), completion: completion)
        }
        if let data = imageData {
            upload(data: data, for: .parking) { result in
                switch result {
                case .success(let url):
                    perform(image: url)
                case .failure(let e):
                    completion(.failure(e))
                }
            }
        } else {
            perform()
        }
    }
    
    func update(trip: Trip, completion: @escaping (Result<Trip.Update, Error>) -> ()) {
        send(.post(json: trip, api: .update), completion: completion)
    }
    
    func getTrips(completion: @escaping (Result<[Trip], Error>) -> ()) {
        send(.get(.getTrips), completion: completion)
    }
    
    func rate(trip: Trip, with rating: Int, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Rating: Encodable {
            let tripId: Int
            let rating: Int
        }
        let params = Rating(tripId: trip.tripId, rating: rating)
        send(.post(json: params, api: .rate), completion: completion)
    }
    
    func getTrip(by tripId: Int, completion: @escaping (Result<Trip, Error>) -> ()) {
        struct TripReguest: Encodable {
            let tripId: Int
        }
        let params = TripReguest(tripId: tripId)
        struct Wrap: Decodable {
            let trip: Trip
        }
        send(.post(json: params, api: .getTrip)) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let envelope):
                completion(.success(envelope.trip))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
}


