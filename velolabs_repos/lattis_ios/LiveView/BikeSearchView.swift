//
//  BikeSearchView.swift
//  LiveView
//
//  Created by Ravil Khusainov on 21.05.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import SwiftUI
import UIKit
@testable import LattisCore

struct BikeSearchView: UIViewControllerRepresentable {
    typealias UIViewControllerType = RideSearchViewController
    
    func makeUIViewController(context: Self.Context) -> Self.UIViewControllerType { .init() }
    
    func updateUIViewController(_ uiViewController: Self.UIViewControllerType, context: Self.Context) {}
}

struct BikeSearchView_Previews: PreviewProvider {
    static var previews: some View {
        BikeSearchView()
    }
}
