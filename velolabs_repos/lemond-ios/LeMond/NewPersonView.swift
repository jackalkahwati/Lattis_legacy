//
//  NewPersonView.swift
//  LeMond
//
//  Created by Ravil Khusainov on 04.01.2022.
//

import SwiftUI

struct NewPersonView: View {
    
    @EnvironmentObject var viewModel: NewPersonViewModel
    @Environment(\.dismiss) var dismiss
    @FocusState var focused: Bool
    
    var body: some View {
        NavigationView {
            Form {
                Section {
                    HStack {
                        Image(systemName: "person")
                        TextField("Name", text: $viewModel.name)
                            .textContentType(.name)
                            .focused($focused)
                    }
                    HStack {
                        Image(systemName: "phone")
                        TextField("Phone Number", text: $viewModel.phoneNumber)
                            .keyboardType(.phonePad)
                            .textContentType(.telephoneNumber)
                    }
                }
                Button(action: {
                    viewModel.share()
                    dismiss()
                }) {
                    HStack {
                        Spacer()
                        Text("Share")
                        Spacer()
                    }
                }
                .disabled(!viewModel.canShare)
            }
            .navigationTitle(Text("Share to:"))
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { dismiss() }) {
                        Text("Cancel")
                    }
                }
            }
        }
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                focused = true
            }
        }
    }
}

struct NewPersonView_Previews: PreviewProvider {
    static var previews: some View {
        NewPersonView()
    }
}
