//
//  Oval+Hubs.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 15.01.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import Foundation
import Model
import OvalAPI
import CoreLocation
import SwiftUI

extension API {
    static func rentals(_ region: MapRegion) -> API {
        let ne = String(format: "ne=%0.6f,%0.6f", region.ne.latitude, region.ne.longitude)
        let sw = String(format: "sw=%0.6f,%0.6f", region.sw.latitude, region.sw.longitude)
        return .init(path: "rentals?\(ne)&\(sw)")
    }
    
    static func undock(_ id: String) -> API {
        .init(path: "hubs/\(id)/undock")
    }
    
    static func search(bikeName: String) -> API {
        .init(path: "rentals/search?bike_name=\(bikeName.addingPercentEncoding(withAllowedCharacters: .urlHostAllowed)!)")
    }
}

extension Session: HubsAPI {
    func fetchRentals(in region: MapRegion, completion: @escaping (Result<Rentals, Error>) -> Void) -> URLSessionTask? {
        send(.get(.rentals(region)), completion: completion)
    }
    
    func find(by qrCode: String) async throws -> Rental {
        try await withCheckedThrowingContinuation { continuation in
            send(.get(.init(path: "rentals/find?qr_code=\(qrCode)"))) { (result: Result<Rental, Error>) in
                switch result {
                case .success(let success):
                    continuation.resume(returning: success)
                case .failure(let failure):
                    continuation.resume(throwing: failure)
                }
            }
        }
    }
    
    func undock(vehicle: Bike, completion: @escaping (Result<Void, Error>) -> Void) {
        guard let id = vehicle.adapterId else { return }
        struct Res: Decodable {
            let uuid: String
        }
        struct Body: Encodable {
            let hubType: String?
        }
        let body = Body(hubType: vehicle.controllers?.first(where: {$0.deviceType == "adapter"})?.vendor.lowercased())
        send(.post(json: body, api: .undock(id))) { (result: Result<Void, Error>) in
            switch result {
            case .failure(let error):
                completion(.failure(error))
            case .success:
                completion(.success(()))
            }
        }
    }
    
    func filter(bikeName: String, completion: @escaping (Result<[Bike], Error>) -> Void) -> URLSessionTask? {
        struct Envelope: Codable {
            let bikes: [Bike]
        }
        return send(.get(.search(bikeName: bikeName)), completion: { (result: Result<Envelope, Error>) in
            switch result {
            case .success(let env):
                completion(.success(env.bikes))
            case .failure(let error):
                completion(.failure(error))
            }
        })
    }
}


extension Hub: MapPoint {
    public var coordinate: CLLocationCoordinate2D {
        .init(latitude, longitude)
    }
    
    public var identifier: String {
        switch type {
        case "parking_station":
            return "annotation_parking_spot"
        default:
            return "annotation_bike_hub"
        }
    }
    
    public var title: String? {
        hubName
    }
    
    public var subtitle: String? {
        description
    }
    
    public var color: UIColor {
        .accent
    }
    
    public var bage: Int? {
        switch integration {
        case .custom:
            return ports?.count
        default:
            return bikes?.count
        }
    }
    
    public func isEqual(to: MapPoint) -> Bool {
        guard let hub = to as? Hub else { return false }
        return hubId == hub.hubId
    }

    public var batteryLevel: Int? { nil }
}
