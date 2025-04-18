//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 02.08.2021.
//

import Foundation
import Vapor
import Fluent
import JWTKit

enum GPSTracking {
    
    static var token: Token!
    static func headers() throws -> HTTPHeaders {
        if let token = token, token.expiresAt > Date() {
            return .init([
                ("x-access-token", token.value)
            ])
        }
        guard let secret = Environment.get("GPS_TRACKING_JWT_SECRET") else { throw Abort(.internalServerError, reason: "GPS_TRACKING_JWT_SECRET env is missing")}
        let signers = JWTSigners()
        signers.use(.hs256(key: secret))
        let exp = Date().addingTimeInterval(180)
        let value = try signers.sign(TokenPayload(exp: .init(value: exp)), kid: "lattis-circle")
        token = .init(value: value, expiresAt: exp)
        return .init([
            ("x-access-token", value)
        ])
    }
    static func url(path: String) throws -> URI {
        guard let endpoint = Environment.get("GPS_TRACKING_API") else { throw Abort(.internalServerError, reason: "GPS_TRACKING_API env is missing") }
        var uri = URI(string: endpoint)
        uri.path = path
        return uri
    }
    static func update(status: VehicleStatus, vehicles: [Int], client: Client) throws -> EventLoopFuture<HTTPStatus> {
        let url = try url(path: "vehicles")
        let headers = try headers()
        return client.patch(url, headers: headers) { request in
            try request.content.encode(Command(values: status, filter: Vehicles(bike_id: vehicles)))
        }
        .map { response in
            let str = String(buffer: response.body!)
            print(str)
            return response.status
        }
    }
}

extension GPSTracking {
    final class Thing: Model, Content {
        static var schema: String = "bikesAndControllers"
        
        @ID(custom: "controller_id")
        var id: Int?
        
        @Field(key: "bike_id")
        var bikeId: Int?
        
        @Field(key: "controller_key")
        var key: String?
        
        @Field(key: "position")
        var coordinate: Coordinate?
        
        @Field(key: "battery")
        var battery: Int
        
        @OptionalField(key: "lastValidLocation")
        var validLocation: ValidLocation?
    }
    
    struct ValidLocation: Codable {
        let latitude: Double
        let longitude: Double
        let timestamp: Double
    }
    
    struct Command<Values: Codable, Filters: Codable>: Content {
        let values: Values
        let filter: Filters
    }
    
    struct VehicleStatus: Codable {
        let status: String
        let current_status: String
        
        static let live = VehicleStatus(status: "active", current_status: "parked")
    }
    
    struct Vehicles: Codable {
        let bike_id: [Int]
    }
    
    struct TokenPayload: JWTPayload {
        let exp: ExpirationClaim
        func verify(using signer: JWTSigner) throws {
        }
    }
    
    struct Token {
        let value: String
        let expiresAt: Date
    }
}


