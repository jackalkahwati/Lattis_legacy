//
//  Trip.swift
//  Operator
//
//  Created by Ravil Khusainov on 03.04.2021.
//

import Foundation

struct Trip: Codable, Identifiable, Hashable {
    let id: Int
    let createdAt: Date
    let endedAt: Date?
    let vehicleId: Int
    let user: User
    let startAddress: String?
    let endAddress: String?
    let steps: String?
    let receipt: Receipt?
}

extension Trip {
    struct Receipt: Codable, Hashable {
        let id: Int
        let mettered: Double?
        let excess: Double?
        let total: Double
        let currency: String?
    }
    
    var duration: String? {
        let date = endedAt ?? Date()
        let lengh = date.timeIntervalSince(createdAt)
        guard lengh > 0 else { return nil }
        let units: NSCalendar.Unit = lengh > 60 ? .standard : [.second]
        return lengh.duration(units)
    }
}

extension Date {
    func asString(_ formatter: DateFormatter = .default) -> String {
        formatter.string(from: self)
    }
}


extension TimeInterval {
    func duration(_ units: NSCalendar.Unit) -> String {
        let formatter = DateComponentsFormatter()
        formatter.allowedUnits = units
        formatter.unitsStyle = .abbreviated
        return formatter.string(from: self)!
    }
}

extension NSCalendar.Unit {
    static let standard: NSCalendar.Unit = [.day, .hour, .minute]
}
