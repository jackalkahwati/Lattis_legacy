//
//  DateFormatter.swift
//  Operator
//
//  Created by Ravil Khusainov on 20.03.2021.
//

import Foundation

extension DateFormatter {
    static let `default`: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        formatter.doesRelativeDateFormatting = true
        return formatter
    }()
}
