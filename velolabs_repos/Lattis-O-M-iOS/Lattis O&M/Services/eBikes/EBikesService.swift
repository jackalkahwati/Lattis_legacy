//
//  EBikesService.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 7/21/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Oval

public struct EBikeInfo {
    var batteryLevel: Double
    var request: Request
    
    struct Request {
        var key: String
        var lockId: Int32?
        var lockBatteryLevel: Double?
    }
    
    enum Result {
        case success(EBikeInfo)
        case error(Error, Request)
    }
}

extension EBikeInfo.Request {
    init?(ellipse: Ellipse) {
        guard let bikeId = ellipse.bikeId else { return nil }
        self.key = "\(bikeId)"
        self.lockId = nil
        self.lockBatteryLevel = nil
    }
}


extension EBikeInfo {
    var params: [String: Any] {
        guard let bikeId = Int(request.key) else { return [:] }
        return [
            "bike_id": bikeId,
            "bike_battery_level": Int(batteryLevel*100)
        ]
    }
}

protocol EBikesService {
    var request: EBikeInfo.Request {get}
    init(request: EBikeInfo.Request, callback:@escaping (EBikeInfo.Result) -> ())
}

public final class EBikeHandler {
    public static let shared = EBikeHandler()
    
    fileprivate var network: BikeNetwork = Session.shared
    fileprivate var services: [String: EBikesService] = [:]
    
    public func update(ellipse: Ellipse) {
        guard let request = EBikeInfo.Request(ellipse: ellipse),
            let service = service(for: ellipse, request: request) else { return }
        services[service.request.key] = service
        
//            let info = EBikeInfo(batteryLevel: 0.22, request: request)
//            send(info: info)
    }
    
    private func handleCallback(result: EBikeInfo.Result) {
        switch result {
        case let .error(error, request):
            services.removeValue(forKey: request.key)
            print(error)
        case .success(let info):
            services.removeValue(forKey: info.request.key)
            network.send(metadata: .bike(info)) { result in
                switch result {
                case .success:
                    break
                case .failure(let error):
                    report(error: error)
                }
            }
        }
    }
    
    private func service(for ellipse: Ellipse, request: EBikeInfo.Request) -> EBikesService? {
        return nil
//        return HyenaBikeService(request: request, callback: handleCallback(result: ))
    }
}

