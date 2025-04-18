//
//  VehicleStatusView.swift
//  Operator
//
//  Created by Ravil Khusainov on 29.04.2021.
//

import SwiftUI

struct VehicleStatusView: View {
    
    @EnvironmentObject var viewModel: VehicleStatusViewModel
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            VStack {
                Form {
                    Section(header: Text("status-primary")) {
                        ForEach(viewModel.statusList) { st in
                            RadioButton(title: st.displayValue, value: st, selected: $viewModel.status)
                        }
                    }
                    if let status = viewModel.status, let list = viewModel.usageList[status] {
                        Section(header: Text("status-secondary")) {
                            ForEach(list) { usage in
                                RadioButton(title: usage.displayValue, value: usage, selected: $viewModel.usage)
                                    .disabled(viewModel.disabled(usage: usage))
                            }
                        }
                    }
                }
                Button(action: save, label: {
                    Text("save")
                })
                .buttonStyle(CreateButtonStyle())
                .disabled(!viewModel.valid())
                .padding(.horizontal)
            }
            .padding(.bottom)
            .navigationTitle("status")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Text("cancel")
                    }
                    .foregroundColor(.white)
                }
            }
        }
    }
    
    func save() {
        presentationMode.wrappedValue.dismiss()
        viewModel.save()
    }
}

