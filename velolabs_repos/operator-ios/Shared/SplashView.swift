//
//  SplashView.swift
//  Operator
//
//  Created by Ravil Khusainov on 04.03.2021.
//

import SwiftUI

struct SplashView: View {
    var body: some View {
        ZStack {
            Color.accentColor
                .ignoresSafeArea(.all)
            SplashItem()
        }
    }
}

struct SplashItem: View {
    var body: some View {
        VStack {
            Spacer()
            Image("lattis_logo_color")
            Text("operator")
                .font(.largeTitle)
            Spacer()
        }
    }
}

struct SplashView_Previews: PreviewProvider {
    static var previews: some View {
        SplashView()
    }
}
