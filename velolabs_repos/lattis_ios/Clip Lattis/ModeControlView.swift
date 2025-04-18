//
//  ModeControlView.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 08.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI
import AudioToolbox

struct ModeControlView: View {
    
    @Binding var secure: Bool
    let action: () -> Void
    
    @State private var loading = false
    @State private var loadingOpacity: CGFloat = 0
    @GestureState private var press = false
    
    let lockedColor: Color = .red.opacity(0.7)
    let unlockedColor: Color = .green.opacity(0.7)
    
    var body: some View {
        ZStack {
            Circle()
                .strokeBorder(Color.cyan, lineWidth: 32)
                .background(Circle().fill(secure ? lockedColor : unlockedColor))
//                .strokeBorder(secure ? lockedColor : unlockedColor, lineWidth: 32)
                .overlay(progressOverlay)
                .overlay(loadingOverlay)
            VStack(spacing: 24) {
//                Image(systemName: secure ? "lock.fill" : "lock.fill.open")
//                    .resizable()
//                    .aspectRatio(1.0, contentMode: .fit)
//                    .frame(height: 100)
                Text(secure ? "Locked" : "Unlocked")
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
                loading = false
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
