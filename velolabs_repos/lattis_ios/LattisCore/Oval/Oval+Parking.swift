//
//  Oval+Parking.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 09/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import OvalAPI
import CoreLocation
import Model

fileprivate extension API {
    enum Path: String {
        case spots = "get-parking-spots-for-fleet"
        case zones = "get-parking-zones-for-fleet"
    }
    
    static func parking(_ path: Path) -> API {
        return .init(path: "parking/" + path.rawValue)
    }
    
    static func hubs(bikeId: Int, coordinate: CLLocationCoordinate2D) -> API {
        .init(path: "hubs/parking?bikeId=\(bikeId)&lat=\(coordinate.latitude)&lon=\(coordinate.longitude)")
//        .init(path: "hubs?bikeId=901&lat=55.74967504679253&lon=55.79720748346543")
//        .init(path: "hubs?bikeId=875&lat=45.49406970285171&lon=-73.61139865901836")
    }
}

extension Session: ParkingAPI {
    func getParkings(by fleetId: Int, bikeId: Int?, coordinate: CLLocationCoordinate2D?, completion: @escaping (Result<Parking, Error>) -> ()) {
        let params = ["fleet_id": fleetId]
        struct SpotsWrap: Decodable {
            let parkingSpots: [Parking.Spot]
        }
        struct ZonesWrap: Decodable {
            let parkingZones: [Parking.Zone]
        }
        send(.post(json: params, api: .parking(.spots))) { (result: Result<SpotsWrap, Error>) in
            switch result {
            case .success(let s):
                self.send(.post(json: params, api: .parking(.zones))) { (result: Result<ZonesWrap, Error>) in
                    switch result {
                    case .success(let z):
//                        completion(.success(.init(spots: s.parkingSpots, zones: z.parkingZones, hubs: [])))
                        if let cord = coordinate, let bike = bikeId {
                            self.send(.get(.hubs(bikeId: bike, coordinate: cord))) { (res: Result<[ParkingHub], Error>) in
                                switch res {
                                case .success(let hubs):
                                    completion(.success(.init(spots: s.parkingSpots, zones: z.parkingZones, hubs: hubs)))
                                case .failure(let error):
                                    completion(.failure(error))
                                }
                            }
                        } else {
                            completion(.success(.init(spots: s.parkingSpots, zones: z.parkingZones, hubs: [])))
                        }
                    case .failure(let e):
                        completion(.failure(e))
                    }
                }
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
}

