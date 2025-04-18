//
//  PortItem.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 31.01.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI
import Model

struct PortItem: View {
    
    let port: Hub.Port
    
    var body: some View {
        HStack {
            Text("#\(port.portNumber ?? 0)")
            Spacer()
        }
        .padding()
        .background(
            Capsule()
                .foregroundColor(Color(UIColor.secondaryBackground))
                .shadow(color: .gray.opacity(0.4), radius: 2, x: 0, y: 1)
        )
        .padding(.horizontal)
    }
}
