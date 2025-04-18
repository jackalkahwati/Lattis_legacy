//
//  VehicleSearchView.swift
//  Operator
//
//  Created by Ravil Khusainov on 22.03.2021.
//

import SwiftUI

struct VehicleSearchView: View {
    
    @EnvironmentObject var logic: VehiclesLogicController
    @Environment(\.presentationMode) var presentationMode
    @State var name: String = ""
    @State var status: Vehicle.Status?
    fileprivate let statusValues: [Vehicle.Status] = [.active, .inactive, .suspended]
    
    var body: some View {
        NavigationView {
            VStack {
                TextField("vehicle-name", text: $name)
                    .padding(10)
                    .background(Color.accentColor.opacity(0.3))
                    .cornerRadius(5)
                    .padding(.horizontal)
                Form {
                    Section(header: Text("status")) {
                        RadioButton(title: "status-any", value: nil, selected: $status)
                        ForEach(statusValues) { status in
                            RadioButton(title: status.displayValue, value: status, selected: $status)
                        }
                    }
                }
                Button(action: search) {
                    Text("search")
                }
                .buttonStyle(CreateButtonStyle())
                .padding(.horizontal)
            }
            .padding(.vertical)
            .navigationTitle("filter")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }, label: {
                        Text("cancel")
                    })
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: clean, label: {
                        Text("clean")
                    })
                    .disabled(logic.filters.isEmpty)
                }
            }
        }
        .onAppear(perform: restoreValues)
    }
    
    func clean() {
        name = ""
        status = nil
        logic.filters = []
        logic.search()
        presentationMode.wrappedValue.dismiss()
    }
    
    func search() {
        var tags: [Vehicle.SearchTag] = []
        if let status = status {
            tags.append(.status(status))
        }
        if !name.isEmpty {
            tags.append(.name(name.trimmingCharacters(in: .whitespacesAndNewlines)))
        }
//        logic.search(tags: tags)
        presentationMode.wrappedValue.dismiss()
    }
    
    func restoreValues() {
//        for tag in logic.searchTags {
//            switch tag {
//            case .name(let name):
//                self.name = name
//            case .status(let status):
//                self.status = status
//            }
//        }
    }
}

struct RadioButton<ValueType: Equatable>: View {
    let title: LocalizedStringKey
    let value: ValueType?
    @Binding var selected: ValueType?
    var body: some View {
        Button(action: { selected = value }, label: {
            HStack {
                Text(title)
                    .foregroundColor(.primary)
                Spacer()
                Image(systemName: selected == value ? "largecircle.fill.circle" : "circle")
            }
        })
    }
}

#if os(iOS)
struct VehicleSearch_Previews: PreviewProvider {
    static var previews: some View {
        VehicleSearchView(name: "")
            .environmentObject(VehiclesLogicController(UserSettings()))
    }
}
#endif
