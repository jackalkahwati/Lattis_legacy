//
//  BikeNetwork.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 6/6/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Oval
import CoreLocation

protocol BikeNetwork {
    func getBikes(for fleet: Fleet, completion: @escaping (Result<[Bike], Error>) -> ())
    func send(metadata: Metadata, completion: @escaping (Result<Void, Error>) -> ())
    func unassignBike(from lock: Ellipse, completion: @escaping (Result<Ellipse, Error>) -> ())
    func assign(bikeWith qrCode: QRCodeBike, and group: Group?, to lock: Ellipse, completion: @escaping (Result<Ellipse, Error>) -> ())
    func assign(bikeWith qrCode: QRCodeBike, and group: Group?, to iotModule: IoTModule, completion: @escaping (Result<Bike, Error>) -> ())
    func getGroups(for fleet: Fleet, completion: @escaping (Result<[Group], Error>) -> ())
    func updae(state: BikeState, with coordinate: CLLocationCoordinate2D, for bike: Int, completion: @escaping (Result<Void, Error>) -> ())
    func getMacId(by qrCodeId: UInt, completion: @escaping (Result<String, Error>) -> ())
    func changeLabel(for bike: Int, qrCode: QRCodeBike, completion: @escaping (Result<Ellipse, Error>) -> ())
    func changeIoTLabel(for bike: Int, qrCode: QRCodeBike, completion: @escaping (Result<IoTModule, Error>) -> ())
}

fileprivate extension API {
    static func bikes(_ path: String) -> API {
        return .init(path: "bikes/" + path)
    }
    static let getBikes = bikes("get-bikes")
    static let metadata = bikes("update-metadata-for-operator")
    static let assign = bikes("assign-lock")
    static let assignIot = bikes("assign-controller")
    static let unassing = bikes("unassign-lock")
    static let groups = bikes("get-staging-bike-groups")
    static let status = bikes("change-bike-status")
    static let getMac = bikes("get-lock-by-qr-code")
    static let label = bikes("change-label")
}

extension Session: BikeNetwork {
    func getBikes(for fleet: Fleet, completion: @escaping (Result<[Bike], Error>) -> ()) {
        send(.post(json: fleet, api: .getBikes), completion: completion)
    }
    
    func send(metadata: Metadata, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: metadata, api: .metadata), completion: completion)
    }
    
    func assign(bikeWith qrCode: QRCodeBike, and group: Group?, to lock: Ellipse, completion: @escaping (Result<Ellipse, Error>) -> ()) {
        struct Params: Encodable {
            let qrCodeId: Int
            let lockId: Int
            let bikeGroupId: Int?
            let bikeName: String
        }
        struct Lock: Decodable {
            let lock: Ellipse
        }
        let params = Params(qrCodeId: Int(qrCode.id), lockId: lock.lockId, bikeGroupId: group?.groupId, bikeName: qrCode.name)
        send(.post(json: params, api: .assign)) { (result: Result<Lock, Error>) in
            switch result {
            case .success(let lock):
                completion(.success(lock.lock))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func assign(bikeWith qrCode: QRCodeBike, and group: Group?, to iotModule: IoTModule, completion: @escaping (Result<Bike, Error>) -> ()) {
        struct Params: Encodable {
            let qrCodeId: Int
            let controllerId: Int
            let bikeGroupId: Int?
            let bikeName: String
        }
        struct Lock: Decodable {
            let lock: Ellipse
        }
        let params = Params(qrCodeId: Int(qrCode.id), controllerId: iotModule.controllerId!, bikeGroupId: group?.groupId, bikeName: qrCode.name)
        send(.post(json: params, api: .assignIot)) { (result: Result<Bike, Error>) in
            switch result {
            case .success(let bike):
                completion(.success(bike))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func unassignBike(from lock: Ellipse, completion: @escaping (Result<Ellipse, Error>) -> ()) {
        struct Lock: Encodable {
            let lockId: Int
        }
        let params = Lock(lockId: lock.lockId)
        struct Wrap: Decodable {
            let lock: Ellipse
        }
        send(.post(json: params, api: .unassing)) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let wrap):
                completion(.success(wrap.lock))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func getGroups(for fleet: Fleet, completion: @escaping (Result<[Group], Error>) -> ()) {
        struct Wrap: Decodable {
            let bikeGroups: [Group]
        }
        send(.post(json: fleet, api: .groups)) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let wrap):
                completion(.success(wrap.bikeGroups))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    func updae(state: BikeState, with coordinate: CLLocationCoordinate2D, for bike: Int, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Status: Encodable {
            let bikeId: Int
            let to: String
            let latitude: Double
            let longitude: Double
            let category: ArchiveCategory?
        }
        let cat: ArchiveCategory?
        switch state {
        case .archived(let category):
            cat = category
        default:
            cat = nil
        }
        let params = Status(bikeId: bike, to: state.key, latitude: coordinate.latitude, longitude: coordinate.longitude, category: cat)
        send(.post(json: params, api: .status), completion: completion)
    }
    
    func getMacId(by qrCodeId: UInt, completion: @escaping (Result<String, Error>) -> ()) {
        struct Params: Encodable {
            let qrCodeId: UInt
        }
        let params = Params(qrCodeId: qrCodeId)
        struct Wrap: Decodable {
            let bike: Bike
            struct Bike: Decodable {
                let macId: String
            }
        }
        send(.post(json: params, api: .getMac)) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let wrap):
                completion(.success(wrap.bike.macId))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func changeLabel(for bike: Int, qrCode: QRCodeBike, completion: @escaping (Result<Ellipse, Error>) -> ()) {
        struct Params: Encodable {
            let qrCodeId: UInt
            let bikeId: Int
            let bikeName: String
        }
        let params = Params(qrCodeId: qrCode.id, bikeId: bike, bikeName: qrCode.name)
        struct Wrap: Decodable {
            let lock: Ellipse
        }
        send(.put(params, api: .label), completion: { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let envelope):
                completion(.success(envelope.lock))
            case .failure(let error):
                completion(.failure(error))
            }
        })
    }
    
    func changeIoTLabel(for bike: Int, qrCode: QRCodeBike, completion: @escaping (Result<IoTModule, Error>) -> ()) {
        struct Params: Encodable {
            let qrCodeId: UInt
            let bikeId: Int
            let bikeName: String
        }
        let params = Params(qrCodeId: qrCode.id, bikeId: bike, bikeName: qrCode.name)
        struct Wrap: Decodable {
            let controllers: [IoTModule]
        }
        send(.put(params, api: .label), completion: { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let envelope) where !envelope.controllers.isEmpty:
                completion(.success(envelope.controllers.first!))
            case .success:
                completion(.failure(IoTModule.Error.noModuleFound))
            case .failure(let error):
                completion(.failure(error))
            }
        })
    }
}
