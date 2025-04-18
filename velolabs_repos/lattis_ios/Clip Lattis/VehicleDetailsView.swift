//
//  VehicleDetailsView.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 25.01.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI

struct VehicleDetailsView: View {
    @StateObject var viewModel: ViewModel
    var body: some View {
        VStack {
            if let vehicle = viewModel.vehicle {
                Spacer()
                HStack {
                    VStack(alignment: .leading) {
                        Text(vehicle.name)
                            .font(.largeTitle)
                        Text(vehicle.fleet.name)
                            .font(.subheadline)
                    }
                    .foregroundColor(.black)
                    Spacer()
                }
            } else {
                Spacer()
                ProgressView()
                    .foregroundColor(.black)
                Spacer()
            }
        }
    }
}

//struct VehicleDetailsView_Previews: PreviewProvider {
//    static var previews: some View {
//        VehicleDetailsView(viewModel: .init(334))
//    }
//}
