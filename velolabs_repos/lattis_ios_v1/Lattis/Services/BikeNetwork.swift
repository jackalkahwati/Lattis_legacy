//
//  BikesNetwork.swift
//  Lattis
//
//  Created by Ravil Khusainov on 06/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import Oval
import CoreLocation
import MapKit

protocol BikeNetwork {
    func find(in location: CLLocationCoordinate2D, completion: @escaping (Result<Bike.Search, Error>) -> ())
    func book(bike: Bike, coordinate: CLLocationCoordinate2D?, completion: @escaping (Result<(TimeInterval, String?), Error>) -> ())
    func unbook(bike: Bike, damageReported: Bool, connectionFailed: Bool, completion: @escaping (Result<Void, Error>) -> ())
    func getBike(by bikeId: Int?, qrCode: UInt?, completion: @escaping (Result<Bike, Error>) -> ())
    func send(metadata: Metadata, completion: @escaping (Result<Void, Error>) -> ())
    func assign(lock macId: String, to bike: Int)
}

fileprivate extension API {
    static func bikes(path: String) -> API {
        return .init(path: "bikes/" + path)
    }
    static let search = bikes(path: "search-bikes")
    static let book = bikes(path: "create-booking")
    static let unbook = bikes(path: "cancel-booking")
    static let bike = bikes(path: "get-bike-details")
    static let metadata = bikes(path: "update-metadata-for-user")
    static let assign = API(path: "operator/add-lock")
}

extension Session: BikeNetwork {
    func find(in location: CLLocationCoordinate2D, completion: @escaping (Result<Bike.Search, Error>) -> ()) {
        // Test
//        let kazan = CLLocationCoordinate2D(latitude: 55.838120, longitude: 49.082539)
//        let vancoover = CLLocationCoordinate2D(latitude: 45.556429, longitude: -73.533210)
//        let jeremy = CLLocationCoordinate2D(latitude: 45.494035, longitude: -73.611655)
//        let sf = CLLocationCoordinate2D(latitude: 37.777249, longitude: -122.411183)
//        let shiva = CLLocationCoordinate2D(latitude: 13.055514, longitude: 77.486374)
//        let ravi = CLLocationCoordinate2D(latitude: 13.119453, longitude: 80.205448)
//        let jignesh = CLLocationCoordinate2D(latitude: 23.0379131, longitude: 72.5239349)
//        let carignan = CLLocationCoordinate2D(latitude: 45.461204, longitude: -73.329854)
        let loc = location // To simulate location
        struct Bikes: Decodable {
            let nearest: [Bike]
            let available: [Bike]
        }
        let sort: (Bike, Bike) -> Bool = {$0.distance(from: loc) < $1.distance(from: loc)}
        send(.post(json: loc.params, api: .search)) { (result: Result<Bikes, Error>) in
            switch result {
            case .success(let bikes):
                if bikes.nearest.isEmpty == false {
                    return completion(.success(.nearest(bikes.nearest.sorted(by: sort).limited(by: 20))))
                } else if bikes.available.isEmpty == false {
                    return completion(.success(.available(bikes.available.sorted(by: sort).limited(by: 20))))
                } else {
                    return completion(.success(.busy))
                }
            case .failure(let error):
                completion(.failure(error))
            }
            
        }
    }
    
    func book(bike: Bike, coordinate: CLLocationCoordinate2D?, completion: @escaping (Result<(TimeInterval, String?), Error>) -> ()) {
        struct Params: Encodable {
            let bikeId: Int
            let latitude: Double?
            let longitude: Double?
            let byScan: Bool?
        }
        struct Res: Decodable {
            let expiresIn: TimeInterval
            let onCallOperator: String?
        }
        let params = Params(bikeId: bike.bikeId, latitude: coordinate?.latitude, longitude: coordinate?.longitude, byScan: coordinate != nil ? true : nil)
        send(.post(json: params, api: .book)) { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                completion(.success((res.expiresIn, res.onCallOperator)))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    func unbook(bike: Bike, damageReported: Bool, connectionFailed: Bool, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Encodable {
            let bikeId: Int
            let bikeDamaged: Bool?
            let lockIssue: Bool?
        }
        let params = Params(bikeId: bike.bikeId, bikeDamaged: damageReported ? true : nil, lockIssue: connectionFailed ? true : nil)
        send(.post(json: params, api: .unbook), completion: completion)
    }
    
    func getBike(by bikeId: Int?, qrCode: UInt?, completion: @escaping (Result<Bike, Error>) -> ()) {
        struct Params: Encodable {
            let bikeId: Int?
            let qrCodeId: UInt?
        }
        struct Res: Decodable {
            let bike: Bike
        }
        let params = Params(bikeId: bikeId, qrCodeId: qrCode)
        send(.post(json: params, api: .bike)) { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                completion(.success(res.bike))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    func send(metadata: Metadata, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: metadata, api: .metadata), completion: completion)
    }
    
    func assign(lock macId: String, to bike: Int) {
        struct Params: Encodable {
            let macId: String
            let bikeId: Int
        }
        let params = Params(macId: macId, bikeId: bike)
        send(.post(json: params, api: .assign)) { (result: Result<Void, Error>) in }
    }
}


