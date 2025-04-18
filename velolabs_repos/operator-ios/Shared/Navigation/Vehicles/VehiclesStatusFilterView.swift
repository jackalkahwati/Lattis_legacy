//
//  VehiclesStatusFilterView.swift
//  Operator
//
//  Created by Ravil Khusainov on 22.07.2021.
//

import SwiftUI

struct VehiclesStatusFilterView: View {
    
    @Environment(\.presentationMode) var presentationMode
    @Binding var original: [Vehicle.Usage]
    
    @State fileprivate var usages: [Vehicle.Usage] = []
    fileprivate let states: [Vehicle.Status] = [.active, .inactive, .suspended]
    fileprivate let usageTable: [Vehicle.Status: [Vehicle.Usage]] = [
        .active: [.on_trip, .parked, .reserved, .collect],
        .inactive: [.controller_assigned, .lock_not_assigned, .balancing],
        .suspended: [.damaged, .under_maintenance, .reported_stolen, .transport]
    ]
    
    var body: some View {
        VStack {
            Form {
                Section(header: Text("")) {
                    Button(action: selectAll, label: {
                        Label("select-all", systemImage: "checkmark")
                    })
                    Button(action: unselectAll, label: {
                        Label("unselect-all", systemImage: "xmark")
                    })
                }
                ForEach(states) { state in
                    Section(header: Text(state.displayValue)) {
                        ForEach(usageTable[state]!) { usage in
                            PrettyToggleButton(
                                action: { toggle(usage: usage) },
                                title: usage.displayValue,
                                offImage: "xmark",
                                onImage: "checkmark",
                                isOn: isSelected(usage: usage),
                                processing: false
                            )
                        }
                    }
                }
            }
            Button(action: {
                presentationMode.wrappedValue.dismiss()
            }, label: {
                Text("done")
            })
            .buttonStyle(CreateButtonStyle())
            .padding()
        }
        .navigationTitle("status")
        .onAppear {
            usages = original
        }
        .onChange(of: usages) { v in
            original = v
        }
    }
    
    fileprivate func selectAll() {
        usages = usageTable.values.flatMap{$0}
    }
    
    fileprivate func unselectAll() {
        usages.removeAll()
    }
    
    fileprivate func toggle(usage: Vehicle.Usage) {
        if let idx = usages.firstIndex(of: usage) {
            usages.remove(at: idx)
        } else {
            usages.append(usage)
        }
    }
    
    fileprivate func isSelected(usage: Vehicle.Usage) -> Bool {
        usages.contains(usage)
    }
}

//struct VehiclesStatusFilterView_Previews: PreviewProvider {
//    static var previews: some View {
//        VehiclesStatusFilterView()
//    }
//}
