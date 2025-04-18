//
//  AddBikeView.swift
//  LeMond
//
//  Created by Ravil Khusainov on 04.01.2022.
//

import SwiftUI

struct AddBikeView: View {
    
    @EnvironmentObject var viewModel: AddBikeViewModel
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationView {
            VStack {
                QRCodeViewFinder(scanning: $viewModel.scanning, found: viewModel.found(code:))
                    .background(Color.black)
                    .cornerRadius(10)
                    .padding()
                Spacer()
                Button(action: {}) {
                    Text("Continue")
                }
                .buttonStyle(CreateButtonStyle())
                .padding()
                .disabled(true)
                Button(action: viewModel.orderBike) {
                    Text("Order new bike")
                }
                .padding()
            }
            .navigationTitle(Text("Add bike"))
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { dismiss() }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

struct AddBikeView_Previews: PreviewProvider {
    static var previews: some View {
        AddBikeView()
    }
}

struct CreateButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        CreateButtonStyleView(configuration: configuration)
    }
}


private extension CreateButtonStyle {
    struct CreateButtonStyleView: View {
        
        @Environment(\.isEnabled) var isEnabled
        let configuration: CreateButtonStyle.Configuration
        
        var body: some View {
            configuration.label
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.cyan.opacity(isEnabled ? 1 : 0.5))
                .cornerRadius(10)
        }
    }
}
