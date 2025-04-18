//
//  VehicleRentalView.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 25.01.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI
import AuthenticationServices

struct VehicleRentalView: View {
    
    @StateObject var viewModel: ViewModel
    
    var body: some View {
        VStack {
            VehicleDetailsView(viewModel: .init(viewModel.qrCode))
                .padding()
            bottomView
        }
        .background(Color.white)
    }
    
    @ViewBuilder
    fileprivate var bottomView: some View {
        switch viewModel.rentalStatus {
        case .authorized:
            Button(action: viewModel.startTrip) {
                HStack {
                    Spacer()
                    Text("Start trip")
                        .font(.title)
                    Spacer()
                }
                
            }
            .frame(height: 60)
            .background(Color.black)
            .foregroundColor(.white)
            .cornerRadius(5)
            .padding()
        case .unauthorized:
            SignInWithAppleButton(.signIn) { request in
                request.requestedScopes = [.email, .fullName]
            } onCompletion: { viewModel.signIn(with: $0)}
            .frame(height: 60)
            .padding()
        }
    }
}

struct VehicleRentalView_Previews: PreviewProvider {
    static var previews: some View {
        VehicleRentalView(viewModel: .init(.lattis(556), startTrip: {}))
    }
}
