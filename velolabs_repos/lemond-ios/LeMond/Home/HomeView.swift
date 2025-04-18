//
//  HomeView.swift
//  LeMond
//
//  Created by Ravil Khusainov on 27.12.2021.
//

import SwiftUI

struct HomeView: View {
    
    @EnvironmentObject var viewModel: HomeViewModel
    
    var body: some View {
        VStack {
            ModeControlView(secure: $viewModel.secure, action: viewModel.toggleSecurity)
                .padding(24)
            Image("image-bike")
                .resizable()
                .scaledToFit()
                .padding(.horizontal, 32)
            BatteryVew(level: 0.76)
            .padding([.bottom, .leading, .trailing])
        }
        .background(Color.background)
    }
}

struct HomeView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            HomeView()
        }
    }
}

