//
//  CircleAPI+Vehicles.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 03.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation

extension CircleAPI {
    static func vehicle(_ id: Int) async throws -> Vehicle {
        try await agent.run(.get("clip/vehicles/\(id)"))
    }
    
    static func scan(_ qrCode: ScanView.QRType) async throws -> Vehicle {
        var query: [URLQueryItem] = []
        switch qrCode {
        case .lattis(let code):
            query.append(.init(name: "qrCode", value: "\(code)"))
        case .url(let code):
            query.append(.init(name: "controllerCode", value: code))
        }
        return try await agent.run(.get("clip/vehicles", queryItems: query))
    }
}
