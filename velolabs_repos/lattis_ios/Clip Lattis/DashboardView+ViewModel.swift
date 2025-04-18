//
//  DashboardView+ViewModel.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 03.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Combine
import SwiftUI

extension DashboardView {
    final class ViewModel: ObservableObject {
        
        @Published fileprivate(set) var currentStatus: CurrentStatus
        @AppStorage("lattis-token")
        fileprivate var restToken: String?
        @AppStorage("trip-id")
        fileprivate var tripId: Int?
        
        init() {
            currentStatus = .scan
            if let token = restToken {
                restoreStatus(with: token)
            }
        }
        
        private func restoreStatus(with token: String) {
            OvalAPI.logIn(token)
            Task {
                do {
                    let status = try await OvalAPI.appStatus()
                    if let id = status.trip?.trip_id {
                        self.tripId = id
                        startTrip()
                    }
                } catch {
                    print(error)
                }
            }
        }
        
        func scanned(code: ScanView.QRType) {
            currentStatus = .confirmation(code)
        }
        
        func startTrip() {
            currentStatus = .trip
        }
        
        func endTrip() {
            currentStatus = .scan
        }
    }
    
    enum CurrentStatus {
        case scan
        case confirmation(ScanView.QRType)
        case trip
    }
}
