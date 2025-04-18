//
//  VehiclesMapFilterViewModel.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.07.2021.
//

import Combine
import SwiftUI

final class VehiclesMapFilterViewModel: ObservableObject {
    @Binding var filter: [Vehicle.Filter]
    
    init(_ filter: Binding<[Vehicle.Filter]>) {
        self._filter = filter
        filter.wrappedValue.forEach { filter in
            switch filter {
            case .maintenance:
                lowBattery = true
            case .usage(let use):
                self.usages = use
            default: break
            }
        }
    }
    
    let usageTable: [Vehicle.Status: [Vehicle.Usage]] = [
        .active: [.on_trip, .parked, .reserved, .collect],
        .inactive: [.controller_assigned, .lock_not_assigned, .balancing],
        .suspended: [.damaged, .under_maintenance, .reported_stolen, .transport]
    ]
    let states: [Vehicle.Status] = [.active, .inactive, .suspended]
    @Published var usages: [Vehicle.Usage] = []
    @Published var lowBattery: Bool = false
    
    var doneDisabled: Bool {
        usages.isEmpty && !lowBattery
    }
    
    func isSelected(usage: Vehicle.Usage) -> Bool { usages.contains(usage) }
    
    func toggle(usage: Vehicle.Usage) {
        if let idx = usages.firstIndex(of: usage) {
            usages.remove(at: idx)
        } else {
            usages.append(usage)
        }
    }
    
    func selectAll() {
        usages = usageTable.values.flatMap{$0}
        lowBattery = true
    }
    
    func unselectAll() {
        usages.removeAll()
        lowBattery = false
    }
    
    func done() {
        if usages.contains(.controller_assigned) {
            usages.append(.lock_assigned)
        }
        var result: [Vehicle.Filter] = [.usage(usages)]
        if lowBattery {
            result.append(.maintenance([.lowBattery]))
        }
        filter = result
    }
}
