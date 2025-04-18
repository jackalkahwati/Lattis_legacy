//
//  PrettyToggleButton.swift
//  Operator
//
//  Created by Ravil Khusainov on 08.04.2021.
//

import SwiftUI

struct PrettyToggleButton: View {
    
    let action: () -> Void
    let title: LocalizedStringKey
    let offImage: String
    let onImage: String
    let isOn: Bool
    let processing: Bool
    
    var body: some View {
        Button(action: action, label: {
            HStack {
                Text(title)
                Spacer()
                if processing {
                    ProgressView()
                } else {
                    ZStack {
                        Capsule()
                            .fill(isOn ? Color.accentColor : Color.lightGray)
                        HStack {
                            if isOn {
                                Spacer()
                            }
                            Circle()
                                .fill(Color.white)
                                .frame(width: 30, height: 30)
                                .shadow(radius: 2)
                            if !isOn {
                                Spacer()
                            }
                        }
                        .padding(2)
                        HStack {
                            Image(systemName: offImage)
                                .frame(width: 22, height: 22)
                                .foregroundColor(isOn ? .white : .accentColor)
                            Spacer()
                            Image(systemName: onImage)
                                .frame(width: 22, height: 22)
                                .foregroundColor(isOn ? .accentColor : .white)
                        }
                        .padding(.horizontal, 6)
                    }
                    .frame(width: 66)
                    .animation(.default)
                }
            }
        })
        .disabled(processing)
    }
}
