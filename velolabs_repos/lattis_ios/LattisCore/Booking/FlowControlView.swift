//
//  FlowControlView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 10.03.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI

struct FlowControlView: View {
    
    internal init(negative: FlowControlView.Action, positive: FlowControlView.Action, isLoading: Binding<Bool> = .constant(false)) {
        self.negative = negative
        self.positive = positive
        self._isLoading = isLoading
    }
    
    typealias Action =  (LocalizedStringKey, () -> Void)
    
    let negative: Action
    let positive: Action
    @Binding var isLoading: Bool
    
    var body: some View {
        HStack {
            Button(action: negative.1) {
                Text(negative.0)
                    .font(.theme(weight: .medium, size: .text))
                    .foregroundColor(.gray)
            }
            .padding(.trailing)
            Button(action: positive.1) {
                positiveButtonView
                    .font(.theme(weight: .medium, size: .body))
                    .padding()
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .background(
                        Capsule().fill(.black)
                    )
            }
            .frame(height: 44)
        }
    }
    
    @ViewBuilder var positiveButtonView: some View {
        if isLoading {
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: .white))
        } else {
            Text(positive.0)
        }
    }
}

