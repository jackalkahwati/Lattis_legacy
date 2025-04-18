//
//  VehiclesFilterViewModel.swift
//  Operator
//
//  Created by Ravil Khusainov on 22.07.2021.
//

import Foundation
import SwiftUI
import Combine

final class VehiclesFilterViewModel: ObservableObject {
    
    @Binding var fileters: [Vehicle.Filter]
    @Published var name: String = ""
    @Published var usages: [Vehicle.Usage] = []
    @Published var battryLevel: Int? = nil
    
    var cleanable: Bool {
        !name.isEmpty || !usages.isEmpty || battryLevel != nil
    }
    
    init(_ filters: Binding<[Vehicle.Filter]>) {
        _fileters = filters
        for filter in filters.wrappedValue {
            switch filter {
            case .name(let name):
                self.name = name
            case .usage(let use):
                self.usages = use
            case .batterLevel(let level):
                self.battryLevel = level
            default: break
            }
        }
    }
    
    func done() {
        var ftr: [Vehicle.Filter] = []
        if !name.isEmpty {
            ftr.append(.name(name))
        }
        if !usages.isEmpty {
            if usages.contains(.controller_assigned) {
                usages.append(.lock_assigned)
            }
            ftr.append(.usage(usages))
        }
        if let level = battryLevel {
            ftr.append(.batterLevel(level))
        }
        fileters = ftr
    }
}
