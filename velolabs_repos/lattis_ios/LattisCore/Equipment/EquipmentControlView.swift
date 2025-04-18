//
//  EquipmentControlView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 28.03.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI

struct EquipmentControlView: View {
    
    @StateObject var controller: EquipmentControler
    
    var body: some View {
        Button {
            controller.toggle()
        } label: {
            HStack {
                if controller.status ~~ [.fetching, .processing] {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .frame(width: 32, height: 32)
                } else {
                    Image("icon_lock_unlocked", bundle: .core)
                        .background(
                            Circle().fill(.white)
                                .frame(width: 32, height: 32)
                        )
                        .frame(width: 32, height: 32)
                        .foregroundColor(.black)
                }
                Text("unlock")
                    .font(.theme(weight: .medium, size: .body))
                    .padding(.trailing, 4)
            }
            .foregroundColor(.white)
            .padding(8)
            .background(
                Capsule().fill(Color.accent)
                    .shadow(color: .black.opacity(0.08), radius: 3)
            )
        }
        .disabled(!controller.active)
    }
}

