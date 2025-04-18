//
//  AssetRentalView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 14.03.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI

struct AssetRentalView: View {
    
    @StateObject var viewModel: ViewModel
    @State var isScanning: Bool = false
    
    var body: some View {
        ZStack(alignment: .init(horizontal: .leading, vertical: .cardTop)) {
            if viewModel.hintMessage != nil {
                HintView(hint: $viewModel.hintMessage, padding: 40, permanent: true, backgroundColor: viewModel.hintColor)
                    .transition(.move(edge: .bottom))
            }
            VStack(alignment: .leading, spacing: 0) {
                if let thing = viewModel.thing {
                    EquipmentControlView(controller: viewModel.conroller(thing: thing))
                        .padding()
                        .transition(.opacity)
                        .opacity(viewModel.hintMessage == nil ? 1 : 0)
                }
                HStack {
                    Image(systemName: "timer")
                    Text(viewModel.time)
                    Spacer()
                    Capsule()
                        .fill(.white.opacity(0.3))
                        .frame(width: 2)
                        .padding(.vertical, 4)
                    Text(viewModel.price)
                        .font(.theme(weight: .bold, size: .title))
                }
                .font(.theme(weight: .bold, size: .body))
                .foregroundColor(.white)
                .padding()
                .background(
                    Color.black
                        .cornerRadius(.containerCornerRadius, corners: [.topLeft, .topRight])
                        .shadow(color: .black.opacity(0.11), radius: 5)
                )
                .frame(height: 56)
                .padding(.bottom, -8)
                .zIndex(10)
                VStack(spacing: .zero) {
                    HStack {
                        VStack(alignment: .leading, spacing: 5) {
                            Text(viewModel.asset.kind)
                                .font(.theme(weight: .bold, size: .small))
                            Text(viewModel.asset.name)
                                .font(.theme(weight: .bold, size: .title))
                            Text(viewModel.asset.fleetName)
                                .font(.theme(weight: .bold, size: .small))
                                .foregroundColor(.gray)
                        }
                        Spacer()
                    }
                    .padding(.vertical)
                    .foregroundColor(.black)
                    controlView
                        .padding(.top)
                }
                .padding()
                .background(Color.white)
                .alignmentGuide(.cardTop) { d in
                    d[VerticalAlignment.top]
                }
            }
        }
        .disabled(viewModel.isLoading)
        .sheet(isPresented: $isScanning) {
            QRScannerView(viewModel: .init(viewModel.asset) {
                isScanning = false
                viewModel.start()
            })
        }
        .animation(.spring(), value: viewModel.alert)
        .animation(.spring(), value: viewModel.thing)
        .animation(.spring(), value: viewModel.hintMessage)
    }
    
    @ViewBuilder
    fileprivate var controlView: some View {
        if viewModel.tripStarted {
            Button(action: {
                viewModel.end()
            }) {
                Text("end_ride")
            }
            .buttonStyle(.action($viewModel.isLoading))
        } else {
            FlowControlView(negative: ("cancel", {
                viewModel.end()
            }), positive: ("booking_begin_trip", {
                isScanning.toggle()
            }), isLoading: $viewModel.isLoading)
        }
    }
}

struct ActionButtonStyle: ButtonStyle {
    
    @Binding var isLoading: Bool
    func makeBody(configuration: Configuration) -> some View {
        body(configuration)
            .font(.theme(weight: .medium, size: .body))
            .padding()
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .background(
                Capsule().fill(Color.accent)
            )
            .frame(height: 44)
            .shadow(color: .black.opacity(0.08), radius: 3)
    }
    
    @ViewBuilder
    func body(_ configuration: Configuration) -> some View {
        if isLoading {
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: .white))
        } else {
            configuration.label
        }
    }
}

extension ButtonStyle where Self == ActionButtonStyle {
    static func action(_ isLoading: Binding<Bool> = .constant(false)) -> ActionButtonStyle {
        .init(isLoading: isLoading)
    }
}

extension Color {
    static var accent: Color {
        if #available(iOS 15.0, *) {
            return .init(uiColor: UITheme.theme.color.accent)
        } else {
            return .init(UITheme.theme.color.accent)
        }
    }
    
    static var secondaryBackground: Color {
        .init(UIColor.secondaryBackground)
    }
}
