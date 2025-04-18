//
//  RentalBookingView+ViewModel.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 07.03.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Combine
import Model
import OvalBackend
import SideMenu
import SwiftUI

extension AssetBookingView {
    
    @MainActor
    final class ViewModel: ObservableObject {
        
        @Published var isLoading: Bool = false
        @Published var timeLeft: String = "--:--"
        @Published var hintMessage: String?
        
        fileprivate let timeFormatter = DateComponentsFormatter()
        fileprivate var timer: Timer?
        fileprivate var secondsLeft: TimeInterval = 0
        fileprivate var storage: Set<AnyCancellable> = []
        
        let asset: Asset
        let booking: Booking
        @Binding var card: AssetDashboardView.Card
        
        fileprivate let oval = OvalBackend()
        fileprivate let map: MapObserver = .shared
        
        init(_ asset: Asset, card: Binding<AssetDashboardView.Card>, booking: Booking) {
            self.asset = asset
            self.booking = booking
            self._card = card
            
            hintMessage = asset.timeHint
            
            timeFormatter.allowedUnits = [.hour, .minute, .second]
            timeFormatter.unitsStyle = .positional
            
            secondsLeft = booking.deadline.timeIntervalSinceNow
            startCountdown()
            updateTime()
            observe()
        }
        
        func startCountdown() {
            if let timer = timer, timer.isValid { return }
            timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] (_) in
                self?.decrement()
            }
        }
        
        @objc
        func stopCountdown() {
            timer?.invalidate()
        }
        
        fileprivate func observe() {
            map.selected
                .sink { [unowned self] point in
                    guard let spot = point as? Parking.Spot else { return }
                    self.card = .parking(spot)
                }
                .store(in: &storage)
        }
        
        fileprivate func decrement() {
            secondsLeft -= 1
            updateTime()
            if secondsLeft <= 0 {
                timerExpired()
            }
        }
        
        fileprivate func updateTime() {
            timeLeft = timeFormatter.string(from: secondsLeft) ?? ""
        }
        
        fileprivate func timerExpired() {
            stopCountdown()
            isLoading = true
            Task {
                try await Task.sleep(nanoseconds: 2_000_000_000) // 2 seconds
                let info = try await oval.currentStatus()
                
                if asset.isFree {
                    isLoading = false
                    card = .dismiss
                } else if let trip = info.trip {
                    self.card = .rental(trip)
                    isLoading = false
                }
            }
        }
        
        func start() {
            isLoading = true
            Task {
                do {
                    let trip = try await oval.startTrip(with: asset)
                    self.card = .rental(trip)
                } catch {
                    print(error)
                }
                isLoading = false
            }
        }
        
        func cancel() {
            isLoading = true
            Task {
                do {
                    try await oval.cancel(booking: booking, for: asset)
                    card = .dismiss
                } catch {
                    print(error)
                }
                isLoading = false
            }
        }
        
        func closeHint() {
            withAnimation(.spring()) {
                hintMessage = nil
            }
        }
        
        func showTimeHint() {
            guard hintMessage == nil else { return }
            withAnimation(.spring()) {
                hintMessage = asset.timeHint
            }
        }
    }
}

extension Asset {
    var timeHint: String {
        isFree ? "reservation_timer_hint_free".localized() : "reservation_timer_hint".localized()
    }
}
