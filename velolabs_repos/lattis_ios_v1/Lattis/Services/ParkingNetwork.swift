//
//  ParkingNetwork.swift
//  Lattis
//
//  Created by Ravil Khusainov on 07/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import Oval
import CoreLocation

protocol ParkingNetwork {
    func find(in location: CLLocationCoordinate2D, completion: @escaping (Result<[Parking], Error>) -> ())
    func getZones(fleet: Int, completion: @escaping (Result<[ParkingZone], Error>) -> ())
    func getSpots(fleet: Int, completion: @escaping (Result<[Parking], Error>) -> ())
    func checkParking(on location: CLLocation, fleet: Int, completion: @escaping (Result<Parking.Check, Error>) -> ())
}

fileprivate extension API {
    static func parking(path: String) -> API {
        return .init(path: "parking/" + path)
    }
    static let find = parking(path: "get-parking-spots")
    static let zones = parking(path: "get-parking-zones-for-fleet")
    static let spots = parking(path: "get-parking-spots-for-fleet")
    static let check = API(path: "fleet/check-parking-fee")
}

extension Session: ParkingNetwork {
    fileprivate struct Fleet: Encodable {
        let fleetId: Int
        let latitude: Double?
        let longitude: Double?
        let accuracy: Double?
    }
    
    func find(in location: CLLocationCoordinate2D, completion: @escaping (Result<[Parking], Error>) -> ()) {
        send(.post(json: location, api: .find), completion: completion)
    }
    
    func getZones(fleet: Int, completion: @escaping (Result<[ParkingZone], Error>) -> ()) {
        let fleet = Fleet(fleetId: fleet, latitude: nil, longitude: nil, accuracy: nil)
        struct Wrap: Decodable {
            let parkingZones: [ParkingZone]
        }
        send(.post(json: fleet, api: .zones)) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let wrap):
                completion(.success(wrap.parkingZones))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func getSpots(fleet: Int, completion: @escaping (Result<[Parking], Error>) -> ()) {
        let fleet = Fleet(fleetId: fleet, latitude: nil, longitude: nil, accuracy: nil)
        struct Wrap: Decodable {
            let parkingSpots: [Parking]
        }
        send(.post(json: fleet, api: .spots)) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let wrap):
                completion(.success(wrap.parkingSpots))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    public func checkParking(on location: CLLocation, fleet: Int, completion: @escaping (Result<Parking.Check, Error>) -> ()) {
        let fleet = Fleet(fleetId: fleet, latitude: location.coordinate.latitude, longitude: location.coordinate.longitude, accuracy: location.horizontalAccuracy)
        send(.post(json: fleet, api: .check), completion: completion)
    }
}


extension CLLocationCoordinate2D: Encodable {
    enum CodingKeys: String, CodingKey {
        case latitude
        case longitude
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(latitude, forKey: .latitude)
        try container.encode(longitude, forKey: .longitude)
    }
}
