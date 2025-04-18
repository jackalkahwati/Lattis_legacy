//
//  Model+UI.swift
//  Operator
//
//  Created by Ravil Khusainov on 29.04.2021.
//

import SwiftUI

extension Ticket.Category {
    var title: LocalizedStringKey {
        .init(rawValue)
    }
}

extension Vehicle {
    var status: LocalizedStringKey {
        metadata.status.displayValue
    }
}

extension Vehicle.Status {
    var displayValue: LocalizedStringKey {
        switch self {
        case .active:
            return "status-live"
        case .inactive:
            return "status-staging"
        case .suspended:
            return "status-out-of-service"
        case .deleted:
            return "status-archive"
        }
    }
}

extension Vehicle.Usage {
    var displayValue: LocalizedStringKey {
        switch self {
        case .defleet, .defleeted:
            return "defleet"
        case .lock_assigned, .controller_assigned:
            return "equipment-assigned"
        default:
            return .init(rawValue)
        }
    }
}
