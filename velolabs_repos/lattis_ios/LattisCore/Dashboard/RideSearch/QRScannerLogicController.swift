//
//  QRScannerLogicController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 05.06.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import Model
import QRCodeView
import CoreLocation
import OvalBackend

enum QRScannerState {
    case bike(Bike)
    case port(Hub.Port, Hub)
    case hub(Hub)
    case trip(TripManager)
    case rental(Trip, Asset)
    case failed(Error)
}

class QRScannerLogicController {
    
    fileprivate let network: BikeAPI & HubsAPI = AppRouter.shared.api()
    fileprivate let backend = OvalBackend()
    fileprivate(set) var bike: Bike?
    var pricing: Pricing?
    var payPerUse: Bool = false
    var explicitConsent: Bool = false
    fileprivate let cardsStorage = CardStorage()
    fileprivate var currentCard: Payment.Card?
    fileprivate var tripService: TripManager?
    fileprivate var deviceManager: DeviceManager?
    fileprivate let bikeDecoder: QRCodeView.Decoder<Bike.QRCode> = .json()
    fileprivate let urlDecoder: QRCodeView.Decoder = .url()
    fileprivate var failedCodes: [String] = []
    fileprivate let storage = UserStorage()
    fileprivate var user: User?
    fileprivate let coordinate: CLLocationCoordinate2D
    
    init(coordinate: CLLocationCoordinate2D) {
        self.coordinate = coordinate
        fetchCreditCard()
        NotificationCenter.default.addObserver(self, selector: #selector(fetchCreditCard), name: .creditCardUpdated, object: nil)
        storage.current(needRefresh: false, completion: { [unowned self] user in
            self.user = user
        })
        AppRouter.shared.setupPushNotifications()
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc
    fileprivate func fetchCreditCard() {
        cardsStorage.fetch { [unowned self] (cards) in
            self.currentCard = cards.first(where: {Payment.card($0).isCurrent})
        }
    }
    
    var consentText: [String] {
        guard !explicitConsent else { return [] }
        return UITheme.theme.strictTNC
    }
    
    var canRent: Bool {
        guard let bike = bike else { return false }
        if bike.isFree { return true }
        if let card = currentCard {
            return card.gateway == bike.paymentGateway
        }
        return false
    }
    
    var heedPhoneNumber: Bool {
        guard let required = bike?.requirePhoneNumber,
            required,
            !hasPhoneNumber else { return false }
        return true
    }
    
    var hasPhoneNumber: Bool {
        guard let phone = user?.phoneNumber else { return false }
        return !phone.isEmpty
    }
    
    var shouldSelectPricing: Bool {
        guard let options = bike?.pricingOptions, !options.isEmpty else { return false }
        return pricing == nil && !payPerUse
    }
    
    @MainActor
    func handle(code: String, completion: @escaping (QRScannerState) -> ()) -> Bool {
        var checked: String?
        if let qr = bikeDecoder.decode(code) {
            checked = "\(qr.qr_id)"
        } else if let url = urlDecoder.decode(code), url.scheme != nil, !failedCodes.contains(url.lastPathComponent) {
            checked = url.lastPathComponent
        } else if !failedCodes.contains(code) {
            checked = code
        }
        guard let c = checked else { return false }
        Task {
            do {
                let rental = try await self.backend.find(c)
                switch rental {
                case .bike(let bike):
                    self.bike = bike
                    completion(.bike(bike))
                    Analytics.log(.qrCodeScanned(vehicle: bike.bikeId))
                case .hub(let hub):
                    completion(.hub(hub))
                case .port(let port, let hub):
                    completion(.port(port, hub))
                case .invalid:
                    break
                }
            } catch {
                completion(.failed(error))
                print(error)
                self.failedCodes.append(c)
            }
        }
        return true
    }
    
    func startTrip(completion: @escaping (QRScannerState) -> ()) {
        guard let bike = bike else { return completion(.failed(QRScannerStateError.noBikeToStartTrip)) }
        func start() {
            deviceManager = TripManager.startTrip(bike, coordinate: coordinate, pricing: pricing?.pricingOptionId) { (result) in
                switch result {
                case .failure(let error):
                    completion(.failed(error))
                case .success(let manager):
                    completion(.trip(manager))
                }
            }
        }
        if let _ = bike.adapterId {
            network.undock(vehicle: bike) { (result) in
                switch result {
                case .failure(let error):
                    completion(.failed(error))
                case .success:
                    start()
                }
            }
        } else {
            start()
        }
    }
    @MainActor
    func start(asset: Asset, booking: Booking, completion: @escaping (QRScannerState) -> Void) {
        Task {
            let trip = try await backend.startTrip(with: asset)
            completion(.rental(trip, asset))
        }
    }
}

enum QRScannerStateError: Error {
    case noBikeToStartTrip
}
