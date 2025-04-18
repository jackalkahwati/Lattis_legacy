//
//  Oval+Bikes.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 01/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import OvalAPI
import CoreLocation
import Model

fileprivate var searchTask: URLSessionTask?

extension API {
    fileprivate enum BikePath: String {
        case search = "search"
        case find = "find-bikes"
        case booking = "create-booking"
        case unbooking = "cancel-booking"
        case details = "get-bike-details"
        case lock = "user-lock"
        case unlock = "user-unlock"
        case iotStatus = "iot/status"
        case metadata = "update-metadata-for-user"
    }
    
    fileprivate static func bikes(_ path: BikePath, query: [String: String] = [:]) -> API {
        var stringPath = "bikes/" + path.rawValue
        if !query.isEmpty {
            stringPath += "?"
            for (key, value) in query {
                stringPath += "\(key)=\(value)&"
            }
            stringPath = stringPath.trimmingCharacters(in: .init(charactersIn: "&"))
        }
        return .init(path: stringPath)
    }
    
    fileprivate static func command(_ command: BikePath, id: Int) -> API {
        .init(path: "bikes/\(id)/" + command.rawValue)
    }
    
    fileprivate static func command(_ command: BikePath, id: Int, controller key: String) -> API {
        .init(path: "bikes/\(id)/" + command.rawValue + "?controller_key=\(key)")
    }
}

extension Session: BikeAPI {
    func scan(qrCode: String) async throws -> Rental {
        struct Param: Encodable {
            let qrCodeId: String
        }
        return try await withCheckedThrowingContinuation({ continuation in
            send(.post(json: Param(qrCodeId: qrCode), api: .init(path: "bikes/get-bike-details"))) { (result: Result<Rental, Error>) in
                switch result {
                case .success(let success):
                    continuation.resume(returning: success)
                case .failure(let failure):
                    continuation.resume(throwing: failure)
                }
            }
        })
    }
    
    func find(in region: MapRegion, completion: @escaping (Result<[Bike], Error>) -> ()) {
        let query: [String: String] = [
            "ne": String(format: "%0.6f,%0.6f", region.ne.latitude, region.ne.longitude),
            "sw": String(format: "%0.6f,%0.6f", region.sw.latitude, region.sw.longitude)
        ]
        searchTask?.cancel()
        searchTask = send(.get(.bikes(.search, query: query), dateAsTimestamp: false), completion: completion)
//        find(in: .init(55.749752, 49.199658), completion: completion)
//        searchTask = send(.get(.init(path: "bikes/search?ne=55.756060,49.214986&sw=55.734891,49.184169")), completion: completion)
    }
    
    func find(in coordinates: CLLocationCoordinate2D, completion: @escaping (Result<[Bike], Error>) -> ()) {
        let query: [String: Double] = [
            "latitude": coordinates.latitude,
            "longitude": coordinates.longitude
        ]
        struct Wrap: Decodable {
            let bikes: [Bike]
        }
        send(.post(json: query, api: .bikes(.find))) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let wrap):
                completion(.success(wrap.bikes))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func getBike(by bikeId: Int?, qrCodeId: Int?, iotCode: String? , completion: @escaping (Result<Bike, Error>) -> ()) {
        struct Wrap: Decodable {
            let bike: Bike
        }
        struct Param: Encodable {
            let bikeId: Int?
            let qrCodeId: Int?
            let iotQrCode: String?
        }
        let params = Param(bikeId: bikeId, qrCodeId: qrCodeId, iotQrCode: iotCode)
        send(.post(json: params, api: .bikes(.details))) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let wrap):
                completion(.success(wrap.bike))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func book(bike: Bike, pricingId: Int?, completion: @escaping (Result<Bike.Booking, Error>) -> ()) {
        struct Params: Encodable {
            let bikeId: Int
            let deviceToken: String?
            let pricingOptionId: Int?
        }
        send(.post(json: Params(bikeId: bike.bikeId, deviceToken: AppRouter.shared.notificationToken, pricingOptionId: pricingId), api: .bikes(.booking)), completion: completion)
    }
    
    func cancelBooking(with info: Bike.Unbooking, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: info, api: .bikes(.unbooking)), completion: completion)
    }
    
    func send(metadata: Metadata, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: metadata, api: .bikes(.metadata)), completion: completion)
    }
    
    func lock(bikeId: Int, controllers: [String], completion: @escaping (Result<String?, Error>) -> ()) {
        struct Wrap: Decodable {
            let commandId: String?
        }
        let handle: (Result<Wrap?, Error>) -> Void = { result in
            switch result {
            case .failure(let error):
                completion(.failure(error))
            case .success(let wrap):
                completion(.success(wrap?.commandId))
            }
        }
        let params: [String: [String]] = ["controller_key": controllers]
        #if DEBUG
//        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
//            completion(.success((nil)))
//        }
        send(.post(json: params, api: .command(.lock, id: bikeId)), completion: handle)
        #else
        send(.post(json: params, api: .command(.lock, id: bikeId)), completion: handle)
        #endif
    }
    
    func unlock(bikeId: Int, controllers: [String], completion: @escaping (Result<String?, Error>) -> ()) {
        struct Wrap: Decodable {
            let commandId: String?
        }
        let handle: (Result<Wrap?, Error>) -> Void = { result in
            switch result {
            case .failure(let error):
                completion(.failure(error))
            case .success(let wrap):
                completion(.success(wrap?.commandId))
            }
        }
        let params: [String: [String]] = ["controller_key": controllers]
        #if DEBUG
//        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
//            completion(.success((nil)))
//        }
        send(.post(json: params, api: .command(.unlock, id: bikeId)), completion: handle)
        #else
        send(.post(json: params, api: .command(.unlock, id: bikeId)), completion: handle)
        #endif
    }
    
    func iotStatus(bikeId: Int, key: String, completion: @escaping (Result<Thing.Status, Error>) -> Void) {
        #if DEBUG
//        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
//            completion(.success(.init(locked: true)))
//        }
//        DispatchQueue.main.asyncAfter(deadline: .now() + 5) {
//            NotificationCenter.default.post(name: .vehicleLocked, object: nil, userInfo: ["trip_id": "8041"])
//        }
        send(.get(.command(.iotStatus, id: bikeId, controller: key)), completion: completion)
        #else
        send(.get(.command(.iotStatus, id: bikeId, controller: key)), completion: completion)
        #endif
    }
    
    func lockStatus(bikeId: Int, commandId: String, completion: @escaping (Result<Thing.CommandFeedback, Error>) -> Void) {
        struct Wrap: Codable {
            let data: Thing.CommandFeedback
        }
        send(.get(.init(path: "bikes/\(bikeId)/command/\(commandId)"))) { (result: Result<Wrap, Error>) in
            switch result {
            case .failure(let error):
                completion(.failure(error))
            case .success(let wrap):
                completion(.success(wrap.data))
            }
        }
    }
}

enum FakeError: Error {
    case some
}

extension Thing {
    enum LockStatus: Int, Codable {
        case sent, processing, finished
    }
    
    enum LockCommand: String, Codable {
        case lock = "LOCK"
        case unlock = "UNLOCK"
    }
    
    enum StatusDesc: String, Codable {
        case errorStall = "ERROR_STALL"
        case locking = "LOCKING"
        case unlocking = "UNLOCKING"
        case lockSuccess = "LOCK_SUCCESS"
        case unlockSuccess = "UNLOCK_SUCCESS"
        case lockJam = "ERROR_LOCK_JAM"
        case unlockJam = "ERROR_UNLOCK_JAM"
    }
    
    struct CommandFeedback: Codable {
        let status: LockStatus
        let command: LockCommand
        let statusDesc: StatusDesc
    }
}
