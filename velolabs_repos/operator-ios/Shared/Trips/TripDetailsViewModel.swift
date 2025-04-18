//
//  TripDetailsViewModel.swift
//  Operator
//
//  Created by Ravil Khusainov on 04.04.2021.
//

import Foundation
import Combine
import UIKit

extension TripDetailsView {
    final class ViewModel: ObservableObject {
        let trip: Trip
        let vehicle: Vehicle
        let finished: PassthroughSubject<Trip, Never>?
        
        @Published var blockedPhone: String?
        @Published var viewState: ViewState = .screen
        @Published fileprivate(set) var blockedUser: Bool?
        var isEnded: Bool { trip.endedAt != nil }
        var phoneNumber: String? { blockedPhone ?? trip.user.phoneNumber }
        fileprivate var storage: Set<AnyCancellable> = []
        
        init(_ trip: Trip, vehicle: Vehicle, finished: PassthroughSubject<Trip, Never>? = nil) {
            self.trip = trip
            self.vehicle = vehicle
            self.finished = finished
        }
        
        func endTrip(completion: @escaping () -> Void) {
            viewState = .loading
            CircleAPI.end(trip: trip.id)
                .sink { [weak self] (result) in
                    switch result {
                    case .failure(let error):
                        self?.viewState = .error("Warning", error.localizedDescription)
                    case .finished:
                        self?.viewState = .screen
                        completion()
                        self?.finished?.send(self!.trip)
                    }
                } receiveValue: {}
                .store(in: &storage)
        }
        
        func checkIfUserBlocked() {
            CircleAPI.getBlockedUser(id: trip.user.id)
                .sink { [weak self] result in
                    switch result {
                    case .finished:
                        break
                    case .failure:
                        self?.blockedUser = false
                        self?.blockedPhone = nil
                    }
                } receiveValue: { [weak self] number in
                    self?.blockedUser = true
                    self?.blockedPhone = number
                }
                .store(in: &storage)
        }
        
        func blockUser() {
            CircleAPI.blockUser(id: trip.user.id)
                .sink { [weak self] result in
                    
                } receiveValue: { [weak self] in
                    self?.blockedUser = true
                    self?.blockedPhone = self?.trip.user.phoneNumber
                }
                .store(in: &storage)
        }
        
        func unblockUser() {
            CircleAPI.unBlockUser(id: trip.user.id)
                .sink { [weak self] result in
                    
                } receiveValue: { [weak self] in
                    self?.blockedUser = false
                }
                .store(in: &storage)
        }
        
        func toggleUserLock() {
            guard let blocked = blockedUser else { return }
            if blocked {
                unblockUser()
            } else {
                blockUser()
            }
        }
        
        func copy(value: String) {
            UIPasteboard.general.string = value
        }
        
        func call(phoneNumber: String) {
            UIApplication.shared.open(URL(string: "tel://" + phoneNumber)!)
        }
    }
}
