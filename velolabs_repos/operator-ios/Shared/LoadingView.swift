//
//  SpinnerView.swift
//  Spinner
//
//  Created by Pablo Malvasio on 8/11/20.
//  Copyright © 2020 Rootstrap. All rights reserved.
//
import SwiftUI

struct SpinnerConfiguration {
    var spinnerColor: Color = .white
    var blurredBackground: Color = .black
    var spinnerBackgroundColor: Color = .gray
    var backgroundCornerRadius: CGFloat = 20
    var width: CGFloat = 50
    var height: CGFloat = 50
    var speed: Double = 1
}

struct LoadingView: View {
    
    let labelText: String?
    var configuration: SpinnerConfiguration = SpinnerConfiguration()
    @State var isAnimating = false
    
    var body: some View {
        let multiplier = configuration.width / 50
        
        return
            ZStack {
                configuration.blurredBackground.opacity(1)
                    .edgesIgnoringSafeArea(.all)
                    .blur(radius: 300)
                
                ZStack {
                    configuration.spinnerBackgroundColor.opacity(0.5)
                    
                    Circle()
                        .trim(from: 0.2, to: 1)
                        .stroke(
                            configuration.spinnerColor,
                            style: StrokeStyle(
                                lineWidth: 3 * multiplier,
                                lineCap: .round
                            )
                        )
                        .frame(width: configuration.width, height: configuration.height)
                        .shadow(color: Color.black.opacity(0.1), radius: 3, x: 0, y: 3)
                        .rotationEffect(.degrees(isAnimating ? 360 : 0))
                        .animation(
                            Animation.linear(duration: configuration.speed)
                                .repeatForever(autoreverses: false)
                        )
                }
                .frame(width: 100 * multiplier, height: 100 * multiplier)
                .background(Color.white)
                .cornerRadius(configuration.backgroundCornerRadius)
                //        .shadow(color: Color.white.opacity(0.3), radius: 5, x: 0, y: 5)
                .shadow(color: Color.black.opacity(0.1), radius: 2, x: 0, y: 2)
                .onAppear {
                    self.isAnimating = true
                }
            }
    }
}

struct LoadingView_Previews: PreviewProvider {
    static var previews: some View {
        LoadingView(labelText: nil)
    }
}
