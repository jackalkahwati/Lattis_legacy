//
//  RentalBookingView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 07.03.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI

struct AssetBookingView: View {
    
    @StateObject var viewModel: ViewModel
    @State var isScanning: Bool = false
    
    var body: some View {
        ZStack(alignment: .init(horizontal: .leading, vertical: .cardTop)) {
            if let _ = viewModel.hintMessage {
                HintView(hint: $viewModel.hintMessage, padding: .margin/2, permanent: false)
            }
            VStack {
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
                .padding(.top, .margin)
                FlowControlView(negative: ("cancel", {
                    viewModel.cancel()
                }), positive: ("booking_begin_trip", {
                    isScanning = true
                }), isLoading: $viewModel.isLoading)
                    .padding(.top)
            }
            .padding()
            .background(
                Color.white
                    .cornerRadius(.containerCornerRadius, corners: [.topLeft, .topRight])
                    .shadow(color: .black.opacity(0.11), radius: 5, y: -5)
            )
            Button {
                viewModel.showTimeHint()
            } label: {
                HStack {
                    Text("reserved_label")
                    Circle().fill(Color.white)
                        .frame(width: 4, height: 4)
                    Text(viewModel.timeLeft)
                }
                .font(.theme(weight: .medium, size: .text))
                .foregroundColor(.white)
                .padding()
                .frame(height: 38)
                .background(
                    Color.black
                        .cornerRadius(10, corners: [.topRight, .bottomRight])
                        .shadow(radius: 2)
                )
            }
            .padding(.top, -19)
            .alignmentGuide(.cardTop) { d in
                d[VerticalAlignment.top]
            }
        }
        .disabled(viewModel.isLoading)
        .sheet(isPresented: $isScanning) {
            QRScannerView(viewModel: .init(viewModel.asset) {
                isScanning = false
                viewModel.start()
            })
        }
    }
}


extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape( RoundedCorner(radius: radius, corners: corners) )
    }
}

struct RoundedCorner: Shape {

    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}



extension VerticalAlignment {
    struct CardTop: AlignmentID {
        static func defaultValue(in context: ViewDimensions) -> CGFloat {
            context[.top]
        }
    }
    
    static let cardTop = VerticalAlignment(CardTop.self)
}

struct HintView: View {
    
    @Binding var hint: String?
    let padding: CGFloat
    let permanent: Bool
    let backgroundColor: Color
    
    init(hint: Binding<String?>, padding: CGFloat, permanent: Bool = false, backgroundColor: Color = .blue) {
        self._hint = hint
        self.padding = padding
        self.permanent = permanent
        self.backgroundColor = backgroundColor
    }
    
    var body: some View {
        HStack {
            Text(hint ?? "")
                .font(.theme(weight: .medium, size: .text))
            Spacer()
            if !permanent {
                Button {
                    hint = nil
                } label: {
                    Image(systemName: "xmark")
                        .padding(6)
                        .background(
                            Circle().fill(.black.opacity(0.2))
                        )
                        .font(.subheadline)
                }
            }
        }
        .foregroundColor(.white)
        .padding()
        .alignmentGuide(.cardTop) { d in
            d[VerticalAlignment.bottom] + padding
        }
        .padding(.bottom, .containerCornerRadius + padding)
        .background(
            backgroundColor
                .cornerRadius(.containerCornerRadius, corners: [.topRight, .topLeft])
                .shadow(radius: 2)
        )
        .transition(.move(edge: .bottom))
    }
}
