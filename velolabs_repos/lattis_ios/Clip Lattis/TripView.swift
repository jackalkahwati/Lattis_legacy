//
//  TripView.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 08.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI

struct TripView: View {
    @StateObject var viewModel: ViewModel
    var body: some View {
        VStack {
            switch viewModel.currentStatus {
            case .loading:
                EmptyView()
            case .started:
                HStack {
                    VStack(alignment: .leading) {
                        Text(viewModel.vehicleName)
                            .font(.largeTitle)
                        Text(viewModel.fleetName)
                            .font(.title)
                        Text(viewModel.duration)
                            .font(.subheadline)
                    }
                    .foregroundColor(.black)
                    Spacer()
                }
                .padding()
                Spacer()
                ModeControlView(secure: $viewModel.secure, action: viewModel.toggleLock)
                    .padding()
                    .disabled(viewModel.vehicle == nil)
                Spacer()
                Button(action: viewModel.endTrip) {
                    HStack {
                        Spacer()
                        Text("End trip")
                            .font(.title)
                        Spacer()
                    }
                    
                }
                .frame(height: 60)
                .background(Color.black)
                .foregroundColor(.white)
                .cornerRadius(5)
                .padding()
                .disabled(!viewModel.secure)
            }
        }
        .background(Color.white)
    }
}

struct TripView_Previews: PreviewProvider {
    static var previews: some View {
        TripView(viewModel: .init({}))
    }
}
