//
//  ScheduleView.swift
//  LiveView
//
//  Created by Ravil Khusainov on 14.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import SwiftUI
import UIKit
@testable import LattisCore

struct ScheduleView: UIViewControllerRepresentable {
    typealias UIViewControllerType = ScheduleViewController
    
    func makeUIViewController(context: Self.Context) -> Self.UIViewControllerType {
        .init(reservation: .stab)
    }

    func updateUIViewController(_ uiViewController: Self.UIViewControllerType, context: Self.Context) {}
}

struct ScheduleView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ScheduleView()
        }
    }
}
