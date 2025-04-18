//
//  SideMenuView.swift
//  LiveView
//
//  Created by Ravil Khusainov on 25.05.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import SwiftUI
import UIKit
@testable import LattisCore

struct SideMenuView: UIViewControllerRepresentable {
    typealias UIViewControllerType = SideMenuViewController
    
    func makeUIViewController(context: Self.Context) -> Self.UIViewControllerType {
        .init()
    }

    func updateUIViewController(_ uiViewController: Self.UIViewControllerType, context: Self.Context) {}
}



struct SideMenuView_Previews: PreviewProvider {
    static var previews: some View {
        ZStack(alignment: .leading) {
            Rectangle()
                .background(Color(.lightText))
            SideMenuView()
                .frame(width: 300)
        }
    }
}
