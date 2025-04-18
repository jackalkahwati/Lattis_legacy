//
//  TripSummaryView.swift
//  LiveView
//
//  Created by Ravil Khusainov on 21.05.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import SwiftUI
import UIKit
@testable import LattisCore

struct TripSummaryView: UIViewControllerRepresentable {    
    typealias UIViewControllerType = TripSummaryViewController
    
    func makeUIViewController(context: Self.Context) -> Self.UIViewControllerType {
        .init(.stab, callback: {})
    }

    func updateUIViewController(_ uiViewController: Self.UIViewControllerType, context: Self.Context) {}
}

struct TripSummaryView_Previews: PreviewProvider {
    static var previews: some View {
        TripSummaryView()
    }
}
