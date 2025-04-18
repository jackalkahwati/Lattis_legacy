//
//  PricePresentable.swift
//  Lattis
//
//  Created by Ravil Khusainov on 7/24/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

protocol PricePresentable {
    var doubleValue: Double? {get}
    func priceValue(_ currency: String) -> String?
}

extension PricePresentable {
    func priceValue(_ currency: String) -> String? {
        guard let dbl = doubleValue, dbl >= 0 else { return nil }
        let formatter = AppDelegate.shared.currencyFormatter
        formatter.currencyCode = currency
        return formatter.string(from: NSNumber(value: dbl))
    }
}

extension Double: PricePresentable {
    var doubleValue: Double? {
        return self
    }
}

extension Int: PricePresentable {
    var doubleValue: Double? {
        return Double(self)
    }
}
