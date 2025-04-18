//
//  RentalSummaryView+ViewModel.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 2022-05-18.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import Combine
import Model
import OvalBackend
import SwiftUI


extension RentalSummaryView {
    
    @MainActor
    final class ViewModel: ObservableObject {
        
        @Published var rating: Int?
        @Published var duration: String
        
        let trip: Trip
        let asset: Asset
        @Binding var card: AssetDashboardView.Card
        
        fileprivate let backend = OvalBackend()
        
        init(trip: Trip, asset: Asset, card: Binding<AssetDashboardView.Card>) {
            self.trip = trip
            self.asset = asset
            self._card = card
            
            let timeFormatter = DateComponentsFormatter()
            timeFormatter.allowedUnits = [.day, .hour, .minute, .second]
            timeFormatter.unitsStyle = .short
            
            self.duration = timeFormatter.string(from: trip.duration) ?? "--"
        }
        
        var endedAt: String? {
            guard let end = trip.endedAt else { return nil }
            return DateFormatter.localizedString(from: end, dateStyle: .medium, timeStyle: .none)
        }
        
        func submit() {
            card = .dismiss
            guard let rating = rating else {
                return
            }
            Task {
                try await backend.rate(trip.tripId, rating:rating)
            }
        }
    }
}
