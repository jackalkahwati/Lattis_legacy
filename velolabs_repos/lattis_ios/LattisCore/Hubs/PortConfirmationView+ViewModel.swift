//
//  PortConfirmationView+ViewModel.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 17.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import SwiftUI
import Combine
import Model
import OvalBackend

extension PortConfirmationView {
    
    @MainActor
    final class ViewModel: ObservableObject {
        let port: Hub.Port
        let hub: Hub
        let discount: Double?
        let oval = OvalBackend()
        let onConfirm: (Booking) -> Void
        
        @Published fileprivate(set) var card: Payment.Card?
        @Published var pricing: Pricing.Option = .none {
            didSet {
                switch pricing {
                case .pricing:
                    unlockFee = nil
                    surchargeFee = nil
                    surchargeFee = settings?.surchargePrice
                default:
                    unlockFee = settings?.unlockPrice
                    surchargeFee = settings?.surchargePrice
                }
            }
        }
        @Published var isLoading: Bool = false
        @Published var cardState: CardState = .none
        @Published var phoneState: PhoneState = .none
        @Published var paymentsOpen = false
        @Published var modalScreen: ModalScreen?
        @Published var hint: String?
        @Published var unlockFee: String?
        @Published var surchargeFee: String?
        
        fileprivate var user: User?
        fileprivate let userStorage = UserStorage()
        fileprivate let storage = CardStorage()
        
        
        init(_ port: Hub.Port, hub: Hub, discount: Double?, confirm: @escaping (Booking) -> Void) {
            self.port = port
            self.hub = hub
            self.onConfirm = confirm
            self.discount = discount
            if pricingOptions.isEmpty {
                self.surchargeFee = settings?.surchargePrice
                self.unlockFee = settings?.unlockPrice
            }
            subscribeToCardUpdates()
            fetchUser()
            refreshCard()
        }
        
        func subscribeToCardUpdates() {
            NotificationCenter.default.addObserver(self, selector: #selector(refreshCard), name: .creditCardUpdated, object: nil)
            NotificationCenter.default.addObserver(self, selector: #selector(refreshCard), name: .creditCardAdded, object: nil)
            NotificationCenter.default.addObserver(self, selector: #selector(refreshCard), name: .creditCardRemoved, object: nil)
        }

        @objc fileprivate func fetchUser() {
            userStorage.current(needRefresh: false, completion: { [unowned self] user in
                self.user = user
            })
        }
        
        @objc fileprivate func refreshCard() {
            storage.fetch { [weak self] cards in
                self?.card = cards.filter(\.gateway == .stripe).first(where: { Payment.card($0).isCurrent })
            }
        }
        
        var hubName: String {
            hub.hubName
        }

        var needPhoneNumber: Bool {
            guard let required = hub.fleet.requirePhoneNumber,
                required,
                !hasPhoneNumber else { return false }
            return true
        }
        
        var hasPhoneNumber: Bool {
            guard let phone = user?.phoneNumber else { return false }
            return !phone.isEmpty
        }
        
        var fleetName: String {
            hub.fleet.name ?? "Invalid"
        }
        
        var perUsePrice: String {
            hub.fleet.paymentSettings?.fullPrice ?? "Invalid"
        }
        
        var cardNumber: String? {
            guard !hub.fleet.isFree else { return nil }
            guard let title = card?.title else {
                return "add_credit_card".localized()
            }
            return title
        }
        
        var pricingOptions: [Pricing] {
            return hub.pricingOptions ?? []
        }
        
        var baseFare: String {
            if let settings = settings {
                if let price = settings.price {
                    return price
                } else {
                    return settings.fullPrice
                }
            } else {
                return "bike_detail_bike_cost_free".localized()
            }
        }
        
        var pricingOptinsFare: String {
            switch pricing {
            case .perUse:
                return baseFare
            default:
                return pricing.title
            }
        }
        
        var settings: Payment.Settings? {
            hub.fleet.paymentSettings
        }
        
        func confirm() {
            if let message = AppRouter.shared.hintMessage {
                hint = message
                return
            }
            if TutorialManager.shared.shouldPresent {
                modalScreen = .tutorial
                return
            }
            if card == nil && hub.fleet.isPayment {
                modalScreen = .paymentMethods
                return
            }
            if !pricingOptions.isEmpty && pricing == .none {
                cardState = .pricing
                return
            }
            
            if needPhoneNumber {
                phoneState = .add
                return
            }

            isLoading = true
            Task {
                do {
                    let booking = try await oval.createBooking(port: port)
                    onConfirm(booking)
                } catch {
                    print(error)
                }
                isLoading = false
            }
        }
    }
}

extension PortConfirmationView {
    enum CardState: Int, Equatable {
        case none
        case confirmation
        case pricing
    }
    
    enum PhoneState: Int, Equatable {
        case none
        case add
        case profile
    }
    
    enum ControlState: Int, Equatable {
        case confirm
        case pricing
        case payment
    }
    
    enum ModalScreen: Identifiable {
        case paymentMethods
        case safari(URL)
        case tutorial
        
        var id: Int {
            switch self {
            case .paymentMethods:
                return 0
            case .safari:
                return 1
            case .tutorial:
                return 2
            }
        }
    }
}

func ==<T, V: Equatable>(lhs: KeyPath<T, V>, rhs: V) -> (T) -> Bool {
    return { $0[keyPath: lhs] == rhs }
}
