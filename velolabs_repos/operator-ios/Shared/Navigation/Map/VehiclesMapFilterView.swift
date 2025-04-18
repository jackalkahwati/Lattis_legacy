//
//  VehiclesMapFilterView.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.07.2021.
//

import SwiftUI

struct VehiclesMapFilterView: View {
    
    @EnvironmentObject var viewModel: VehiclesMapFilterViewModel
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        VStack {
            Form {
                Section {
                    Button(action: viewModel.selectAll, label: {
                        Label("select-all", systemImage: "checkmark")
                    })
                    Button(action: viewModel.unselectAll, label: {
                        Label("unselect-all", systemImage: "xmark")
                    })
                }
                ForEach(viewModel.states) { state in
                    Section(header: Text(state.displayValue)) {
                        ForEach(viewModel.usageTable[state]!) { usage in
                            PrettyToggleButton(
                                action: { viewModel.toggle(usage: usage) },
                                title: usage.displayValue,
                                offImage: "xmark",
                                onImage: "checkmark",
                                isOn: viewModel.isSelected(usage: usage),
                                processing: false
                            )
                        }
                    }
                }
                Section(header: Text("maintenance")) {
                    PrettyToggleButton(
                        action: {viewModel.lowBattery.toggle()},
                        title: "low-battery",
                        offImage: "xmark",
                        onImage: "checkmark",
                        isOn: viewModel.lowBattery,
                        processing: false
                    )
                }
            }
            Button(action: {
                viewModel.done()
                presentationMode.wrappedValue.dismiss()
            }, label: {
                Text("done")
            })
            .buttonStyle(CreateButtonStyle())
            .padding()
            .disabled(viewModel.doneDisabled)
        }
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: {
                    presentationMode.wrappedValue.dismiss()
                }) {
                    Text("cancel")
                }
            }
        }
        .navigationTitle("filter")
    }
}

struct VehiclesMapFilterView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            VehiclesMapFilterView()
        }
    }
}
