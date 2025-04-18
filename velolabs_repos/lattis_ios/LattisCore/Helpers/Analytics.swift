//
//  Analytics.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 01.11.2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import FirebaseCrashlytics
import FirebaseAnalytics

struct Analytics {
    typealias FireLytics = FirebaseAnalytics.Analytics
    static let crashlitycs = Crashlytics.crashlytics()
    static func report(_ error: Error?, file: String = #file, function: String = #function, line: Int = #line, with info: [String: String] = [:]) {
        let filename = file.split(separator: "/").compactMap(String.init).last ?? file
        if let e = error {
            crashlitycs.log("\(e)")
        }
        crashlitycs.setCustomKeysAndValues(info)
        let ex = ExceptionModel(name: "Error", reason: "Find the reason in logs")
        ex.stackTrace = [
            .init(symbol: function, file: filename, line: line)
        ]
        crashlitycs.record(exceptionModel: ex)
    }
    
    static func set(user: Int) {
        let userId = String(user)
        crashlitycs.setUserID(userId)
        FireLytics.setUserID(userId)
    }
    
    static func log(_ event: Event) {
        FireLytics.logEvent(event.name, parameters: event.params)
    }
}

extension Analytics {
    struct Event {
        let name: String
        let params: [String: String]
        
        static func tripStarted(_ trip: Trip) -> Event {
            .init(name: "START_RIDE", params: [
                "trip_id": String(trip.tripId),
                "fleet_id": String(trip.fleetId)
            ])
        }
        
        static func tripEnded(_ trip: Trip, rating: Int?) -> Event {
            .init(name: "END_RIDE", params: [
                "trip_id": String(trip.tripId),
                "fleet_id": String(trip.tripId),
                "duaration": String(trip.duration),
                "rating": rating == nil ? "N/A" : String(rating!)
            ])
        }
        
        static func jamming(lockId: Int) -> Event {
            .init(name: "JAMMING", params: [
                "lock_id": String(lockId)
            ])
        }
        
        static func outOfParking(fleetId: Int) -> Event {
            .init(name: "OUT_OF_PARKING", params: [
                "fleet_id": String(fleetId)
            ])
        }
        
        static func qrCodeScanned(vehicle: Int) -> Event {
            .init(name: "QR_CODE_SCANNING", params: ["vehicle": String(vehicle)])
        }
        
        static func confirmed(vehicle: Int) -> Event {
            .init(name: "CONFIRM", params: ["vehicle": String(vehicle)])
        }
        
        static func signUp() -> Event {
            .init(name: "SIGN_UP", params: [:])
        }
        
        static func addCard() -> Event {
            .init(name: "ADD_CREDIT_CARD", params: [:])
        }
        
        static func saveCard() -> Event {
            .init(name: "SAVE_CREDIT_CARD", params: [:])
        }
        
        static func parkingView() -> Event {
            .init(name: "PARKING_VIEW", params: [:])
        }
        
        static func qrCodeMain() -> Event {
            .init(name: "QR_CODE_SCAN_MAIN", params: [:])
        }
        
        static func qrCodeVehicle() -> Event {
            .init(name: "QR_CODE_SCAN_VEHICLE", params: [:])
        }
        
        static func reserve() -> Event {
            .init(name: "RESERVE", params: [:])
        }
        
        static func help() -> Event {
            .init(name: "HELP", params: [:])
        }
        
        static let search = Event(name: "SEARCH", params: [:])
        static let history = Event(name: "RIDE_HISTORY", params: [:])
        static let logout = Event(name: "LOGOUT", params: [:])
    }
}

struct ErrorMessage: LocalizedError {
    let message: String
    var errorDescription: String? { message }
    
    init(_ message: String) {
        self.message = message
    }
}

extension Error {
    static func message(_ text: String) -> ErrorMessage {
        .init(text)
    }
}
