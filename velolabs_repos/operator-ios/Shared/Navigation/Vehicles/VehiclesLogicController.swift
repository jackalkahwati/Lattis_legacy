//
//  VehiclesLogicController.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.03.2021.
//

import Foundation
import Combine

let updateInterval: TimeInterval = 10

final class VehiclesLogicController: ObservableObject {
    
    var fleetId: Int { settings.fleet.id }
    @Published var filters: [Vehicle.Filter] = []
    @Published var sectionTitle: String? = nil
    @Published var viewState: ViewState = .initial
    @Published var sheetState: VehiclesView.Sheet?
    @Published var selectedVehicle: Vehicle?
    @Published fileprivate(set) var vehicles: [Vehicle] = .dummy
    @Published fileprivate(set) var listState: ListState = .part
    fileprivate var storage: Set<AnyCancellable> = []
    fileprivate var page = 1
    fileprivate let per = 20
    fileprivate var total = 20
    fileprivate let settings: UserSettings
    fileprivate var updatedAt = Date().addingTimeInterval(-updateInterval)
    
    init(_ settings: UserSettings) {
        self.settings = settings
        settings.inject.fleet
            .sink { [unowned self] _ in
                page = 1
                total = per
                listState = .part
                vehicles = .dummy
                updatedAt = Date().addingTimeInterval(-updateInterval)
                fetch()
            }
            .store(in: &storage)
    }
    
    func fetch() {
        guard settings.fleet != .select,
              -updatedAt.timeIntervalSinceNow >= updateInterval else { return }
        selectedVehicle = nil
        viewState = .loading
        CircleAPI.vehicles(fleetId, filters: filters, page: 1, per: total)
            .map({
                $0.map(Vehicle.init).sorted(by: self.sort)
            })
            .sink { [weak self] (result) in
                self?.updatedAt = Date()
                switch result {
                case .failure(let error):
                    self?.vehicles = []
                    self?.viewState = .error("warning", error.localizedDescription)
                case .finished:
                    self?.viewState = .screen
                }
            } receiveValue: { [weak self] vehicles in
                self?.update(list: vehicles)
            }
            .store(in: &storage)
        settings.inject.vehicle
            .sink { [weak self] (vehicle) in
                self?.update(vehicle: vehicle)
            }
            .store(in: &storage)
    }
    
    func loadMore() {
        listState = .loading
        CircleAPI.vehicles(fleetId, filters: filters, page: page + 1, per: per)
            .map({
                $0.map(Vehicle.init)
                    .sorted(by: self.sort)
            })
            .sink { [weak self] (result) in
                switch result {
                case .failure(let error):
                    self?.viewState = .error("warning", error.localizedDescription)
                case .finished:
                    self?.viewState = .screen
                }
            } receiveValue: { [weak self] batch in
                self?.next(batch: batch)
            }
            .store(in: &storage)
    }
    
    func search() {
        guard fleetId != 0 else { return }
        page = 1
        total = per
        updatedAt = Date().addingTimeInterval(-updateInterval)
        fetch()
    }
    
    func update(vehicle: Vehicle) {
        if let idx = vehicles.firstIndex(where: {$0.id == vehicle.id}) {
            vehicles[idx] = vehicle
        }
    }
    
    fileprivate func update(list: [Vehicle]) {
        total = list.count
        vehicles = list
        listState = total%per > 0 ? .full : .part
    }
    
    fileprivate func next(batch: [Vehicle]) {
        total += batch.count
        listState = batch.count == per ? .part : .full
        vehicles += batch
        page += 1
    }
    
    fileprivate func sort(_ lhs: Vehicle, _ rhs: Vehicle) -> Bool {
        if lhs.metadata.status == .active && rhs.metadata.status != .active { return true }
        return false
    }
}

extension VehiclesLogicController {
    enum ListState {
        case full
        case part
        case loading
    }
}

extension Array where Element == Vehicle.SearchTag {
    var bikeName: String {
        for tag in self {
            switch tag {
            case .name(let name):
                return name
            default:
                continue
            }
        }
        return ""
    }
}
