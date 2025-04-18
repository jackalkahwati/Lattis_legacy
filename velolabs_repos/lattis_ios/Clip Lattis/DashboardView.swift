//
//  DashboardView.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 03.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI

struct DashboardView: View {
    @StateObject var viewModel = ViewModel()
    var body: some View {
        VStack {
            switch viewModel.currentStatus {
            case .scan:
                ScanView(viewModel: .init(viewModel.scanned))
                    .padding()
            case .confirmation(let code):
                VehicleRentalView(viewModel: .init(code, startTrip: viewModel.startTrip))
            case .trip:
                TripView(viewModel: .init(viewModel.endTrip))
            }
            
        }
        .background(Color.white)
        .onOpenURL { url in
            viewModel.scanned(code: .url(url.lastPathComponent))
        }
        .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
            guard let url = activity.webpageURL, !url.lastPathComponent.contains("clip") else {
                            return
                    }
            viewModel.scanned(code: .url(url.lastPathComponent))
        }
    }
}

struct DashboardView_Previews: PreviewProvider {
    static var previews: some View {
        DashboardView()
    }
}
