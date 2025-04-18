//
//  ModeControlView.swift
//  LeMond
//
//  Created by Ravil Khusainov on 01.01.2022.
//

import SwiftUI
import AudioToolbox

struct ModeControlView: View {
    
    @Binding var secure: Bool
    let action: () -> Void
    
    @State private var loading = false
    @State private var loadingOpacity: CGFloat = 0
    @GestureState private var press = false
    
    var body: some View {
        ZStack {
            Circle()
                .strokeBorder(secure ? Color.theft : Color.ride, lineWidth: 32)
                .overlay(progressOverlay)
                .overlay(loadingOverlay)
            VStack(spacing: 24) {
                Text(secure ? "Theft Mode" : "Ride Mode")
                    .font(.largeTitle)
                    .fontWeight(.medium)
                Text(secure ? "Hold to disarm" : "Hold to arm")
                    .font(.title)
            }
            .padding(44)
            
        }
        .gesture(
            LongPressGesture(minimumDuration: 0.5, maximumDistance: 1)
                .updating($press) { currentState, gestureState, transaciton in
                    transaciton.animation = .easeInOut
                    gestureState = currentState
                }
                .onEnded { value in
                    let generator = UIImpactFeedbackGenerator(style: .heavy)
                    generator.impactOccurred()
                    action()
                    loadingOpacity = 1
                    withAnimation(.easeInOut(duration: 1).repeatForever(autoreverses: false)) {
                        loading.toggle()
                    }
                }
        )
        .disabled(loading)
        .onChange(of: secure) { newValue in
            loadingOpacity = 0
            withAnimation(.easeInOut) {
                loading.toggle()
            }
        }
        .animation(.easeInOut, value: secure)
    }
    
    @ViewBuilder
    var progressOverlay: some View {
        Circle()
            .trim(from: 0, to: press ? 1 : 0)
            .stroke(Color.blue, style: StrokeStyle(lineWidth: 4, lineCap: .round))
            .rotationEffect(Angle(degrees: -90))
    }
    
    @ViewBuilder
    var loadingOverlay: some View {
        Circle()
            .trim(from: 0, to: 0.2)
            .stroke(Color.blue, style: StrokeStyle(lineWidth: 4, lineCap: .round))
            .rotationEffect(Angle(degrees: loading ? 270 : -90))
            .opacity(loadingOpacity)
            .animation(.linear, value: loadingOpacity)
    }
}

struct ModeControlView_Previews: PreviewProvider {
    @State static var secure = false
    static var previews: some View {
        ModeControlView(secure: $secure) {
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                secure.toggle()
            }
        }
    }
}
