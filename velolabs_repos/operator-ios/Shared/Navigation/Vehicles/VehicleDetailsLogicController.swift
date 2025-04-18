//
//  VehicleDetailsLogicController.swift
//  Operator
//
//  Created by Ravil Khusainov on 09.03.2021.
//

import Foundation
import Combine

final class VehicleDetailsLogicController: ObservableObject, VehiclePatcher {
    
    @Published fileprivate(set) var vehicle: Vehicle
    @Published var isHistoryShown: Bool = false
    @Published var isMapShown: Bool = false
    @Published var selestedTrip: Trip?
    @Published var selectedBooking: Vehicle.Booking?
    @Published var tripMeta: Vehicle.TripMeta?
    @Published var selectedThing: Thing?
    @Published fileprivate(set) var pages: [InfoPage]
    @Published var selected: String
//    @Published var actionSheetStatus: ActionSheetStatus?
    @Published var sheetStatus: SheetStatus?
    @Published var vehicleStatusEdit: Bool = false
    @Published var viewState: ViewState = .initial
    var currentVehicle: Vehicle? { vehicle }
    let tripFinish = PassthroughSubject<Trip, Never>()
    fileprivate var cancellables: Set<AnyCancellable> = []
    fileprivate let settings: UserSettings
    
    init(_ vehicle: Vehicle, settings: UserSettings) {
        self.vehicle = vehicle
        self.settings = settings
        let pages: [InfoPage] = [.vehicle(vehicle), .equipment(vehicle.things), .tickets(vehicle)]
        self.pages = pages
        self.selected = pages.first!.id
        fetchTrips()
        tripFinish
            .sink { [unowned self] trip in
                self.finish(trip: trip)
            }
            .store(in: &cancellables)
    }
    
    var title: String { vehicle.name }
    var qrCode: QRCode? {
        if let code = vehicle.things.first?.metadata.qrCode {
            return .url(code)
        }
        if let code = vehicle.metadata.qrCode {
            return .lattis(.init(qr_id: code, bike_name: vehicle.name))
        }
        return nil
    }
    
    func fetchTrips() {
        CircleAPI.tripMeta(vehicleId: vehicle.id)
            .sink { [weak self]  (result) in
                switch result {
                case .failure(let error):
                    self?.viewState = .error(nil, nil)
                    print(error)
                case .finished:
                    self?.viewState = .screen
                }
            } receiveValue: { [weak self] meta in
                guard !meta.trips.isEmpty || meta.history > 0 else { return }
                self?.tripMeta = meta
            }
            .store(in: &cancellables)
    }
    
    func changeStatus() {
        if let trip = tripMeta?.trips.first {
            sheetStatus = .trip(trip)
            return
        }
        vehicleStatusEdit = true
    }
    
    func endTrip() {
        guard let trip = tripMeta?.trips.first else { return }
        viewState = .loading
        CircleAPI.end(trip: trip.id)
            .sink { [weak self] (result) in
                switch result {
                case .failure(let error):
                    self?.viewState = .error("Warning", error.localizedDescription)
                case .finished:
                    self?.finish(trip: trip)
                    self?.changeStatus()
                    self?.viewState = .screen
                }
            } receiveValue: {}
            .store(in: &cancellables)
    }
    
    func endBooking() {
        guard let booking = tripMeta?.bookings.first else { return }
        viewState = .loading
        CircleAPI.cancel(booking: booking.id)
            .sink { [weak self] result in
                switch result {
                case .failure(let error):
                    Analytics.report(.error(error), with: [.vehicle: self == nil ? nil : String(self!.vehicle.id)])
                    self?.viewState = .error("Warning", error.localizedDescription)
                case .finished:
                    self?.cancel(booking: booking)
                    self?.viewState = .screen
                }
            } receiveValue: {}
            .store(in: &cancellables)

    }
    
    func updateVehicle(patch: Vehicle.Patch) {
        let restore = vehicle.patch
        vehicle.update(patch: patch)
        CircleAPI.patch(vehicle: vehicle.id, json: patch)
            .sink { [weak self] (result) in
                switch result {
                case .failure(let error):
                    Analytics.report(.error(error), with: [.vehicle: self == nil ? nil : String(self!.vehicle.id)])
                    self?.viewState = .error(nil, nil)
                    self?.vehicle.update(patch: restore)
                case .finished:
                    self?.settings.inject.vehicle.send(self!.vehicle)
                }
            } receiveValue: {}
            .store(in: &cancellables)
    }
    
    func action(for sheet: SheetStatus) -> () -> Void {
        switch sheet {
        case .statusAlert:
            return { self.vehicleStatusEdit = true }
        case .trip:
            return endTrip
        case .booking:
            return endBooking
        }
    }
    
    fileprivate func finish(trip: Trip) {
        tripMeta = tripMeta?.finishing(trip: trip)
        if let trips = tripMeta?.trips, trips.isEmpty {
            sheetStatus = .statusAlert
        }
    }
    
    fileprivate func cancel(booking: Vehicle.Booking) {
        
    }
}

extension VehicleDetailsLogicController {
    enum SheetStatus: Identifiable {
        case trip(Trip)
        case statusAlert
        case booking(Vehicle.Booking)
        
        var id: String {
            switch self {
            case .trip(let trip):
                return "trip_\(trip.id)"
            case .booking(let booking):
                return "booking_\(booking.id)"
            case .statusAlert:
                return "status_alert"
            }
        }
    }
}
