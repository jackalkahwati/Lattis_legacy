//
//  Colors.swift
//  Operator
//
//  Created by Ravil Khusainov on 26.02.2021.
//

import SwiftUI
#if os(macOS)
import AppKit
#else
import UIKit
#endif

extension Color {
    static let paleWhite = Color("PaleWhite")
    static let lightGray = Color("LightGray")
    static let background = Color(.systemBackground)
}
