//
//  VehiclesListMapViewModel.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.06.2021.
//

import Foundation
import Combine

final class VehiclesListMapViewModel: ObservableObject {
    @Published var annotations: [CustomMap.ValueAnnotation] = []
    @Published var selected: Vehicle?
    @Published var vehicleDetailsShown: Bool = false
    @Published var shouldFocusOnUser: Bool = false
    @Published var sheetState: VehiclesListMapView.SheetState?
    fileprivate var storage: Set<AnyCancellable> = []
    fileprivate var bbox: BBox?
    @Published var filters: [Vehicle.Filter] = []
    @Published var timeToRefresh: Bool = false
    fileprivate var refreshTimer: Timer!
    let settings: UserSettings
    
    init(settings: UserSettings) {
        self.settings = settings
        startTimer()
    }
    
    deinit {
        refreshTimer.invalidate()
    }
    
    func update(bbox: BBox) {
        self.bbox = bbox
        refresh()
    }
    
    func select(annotation: CustomMap.ValueAnnotation) {
        guard let vehicle = annotation.value as? Vehicle else { return }
        selected = vehicle
    }
    
    func refresh(clenCache: Bool = false) {
        guard let bbox = bbox else { return }
        timeToRefresh = false
        refreshTimer?.invalidate()
        storage.forEach{$0.cancel()}
        CircleAPI.vehicles(map: bbox, fleetId: settings.fleet.id, filters: filters)
            .map {
                $0.map(Vehicle.init)
            }
            .sink { [weak self] result in
                self?.startTimer()
            } receiveValue: { [unowned self] vehicles in
                CustomMap.cleanCache = clenCache
                annotations = vehicles.compactMap(CustomMap.ValueAnnotation.init)
            }
            .store(in: &storage)
    }
    
    private func startTimer() {
        refreshTimer?.invalidate()
        refreshTimer = Timer.scheduledTimer(withTimeInterval: 10, repeats: false, block: { [weak self] _ in
            self?.timeToRefresh = true
        })
    }
}
