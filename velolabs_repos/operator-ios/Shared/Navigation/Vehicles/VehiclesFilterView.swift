//
//  VehiclesFilterView.swift
//  Operator
//
//  Created by Ravil Khusainov on 21.07.2021.
//

import SwiftUI

struct VehiclesFilterView: View {
    @EnvironmentObject var viewModel: VehiclesFilterViewModel
    @Environment(\.presentationMode) var presentationMode
    var body: some View {
        NavigationView {
            VStack {
                Form {
                    Section(header: Text("")) {
                        ListItem(title: "vehicle-name", value: nameValue, hasValue: !viewModel.name.isEmpty) {
                            viewModel.name = ""
                        }
                        if viewModel.usages.isEmpty {
                            statusValue
                        } else {
                            ListItem(title: "status", value: statusValue, hasValue: !viewModel.usages.isEmpty) {
                                viewModel.usages.removeAll()
                            }
                        }
                        if viewModel.battryLevel != nil {
                            ListItem(title: "battery-level", value: BatteryLevelFilterView(level: $viewModel.battryLevel), hasValue: viewModel.battryLevel != nil) {
                                viewModel.battryLevel = nil
                            }
                        } else {
                            Button(action: { viewModel.battryLevel = 15 }, label: {
                                Text("battery-level")
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            })
                            .foregroundColor(.accentColor)
                        }
                    }
                }
                .buttonStyle(PlainButtonStyle())
                Button(action: {
                    viewModel.done()
                    presentationMode.wrappedValue.dismiss()
                }, label: {
                    Text("done")
                })
                .buttonStyle(CreateButtonStyle())
                .padding([.leading, .bottom, .trailing])
            }
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Text("cancel")
                    }
                    .foregroundColor(.white)
                }
                ToolbarItem(placement: .automatic) {
                    Button(action: {
                        viewModel.battryLevel = nil
                        viewModel.usages.removeAll()
                            viewModel.name = ""
                    }) {
                        Text("clear")
                    }
                    .disabled(!viewModel.cleanable)
                    .foregroundColor(.white)
                }
            }
            .navigationTitle("filter")
        }
    }
    
    private var nameValue: some View {
        TextField("vehicle-name", text: $viewModel.name)
            .textFieldStyle(RoundedBorderTextFieldStyle())
    }
    
    private var statusValue: some View {
        NavigationLink(
            destination: VehiclesStatusFilterView(original: $viewModel.usages),
            label: {
                if viewModel.usages.isEmpty {
                    Text("status")
                        .font(.headline)
                } else {
                    viewModel.usages.localizedText
                }
            })
    }
    
    struct BatteryLevelFilterView: View {
        @Binding var level: Int?
        @State var value: Double = 0
        
        private var stringValue: String {
            String(format: "%.0f%%", value)
        }
        var body: some View {
            HStack {
                Text(stringValue)
                    .frame(width: 50, alignment: .leading)
                Slider(value: $value, in: 0...100, step: 1)
            }
            .onAppear {
                if let l = level {
                    value = Double(l)
                }
            }
            .onChange(of: value) { v in
                level = Int(v)
            }
        }
    }
    
    struct ListItem<V: View>: View {
        let title: LocalizedStringKey
        let value: V
        let hasValue: Bool
        let clear: () -> Void
        var body: some View {
            HStack {
                VStack(alignment: .leading) {
                    HStack {
                        Text(title)
                            .font(.headline)
                        Spacer()
                        if hasValue {
                            Button(action: clear, label: {
                                Text("clear")
                            })
                            .font(.footnote)
                            .foregroundColor(.accentColor)
                        }
                    }
                    value
                }
                Spacer()
            }
        }
    }
}

struct VehilesFilterView_Previews: PreviewProvider {
    @State static var filters: [Vehicle.Filter] = []
    static var previews: some View {
        VehiclesFilterView()
            .environmentObject(VehiclesFilterViewModel($filters))
    }
}

extension Dictionary where Key == Vehicle.Status, Value == [Vehicle.Usage] {
    var localizedText: Text {
        var result = ""
        for (key, value) in self {
            result += key.displayValue.stringValue() + " (" + value.map{$0.displayValue.stringValue()}.joined(separator: ", ") + ")\n"
        }
        result = result.trimmingCharacters(in: .newlines)
        return Text(result)
    }
}

extension Array where Element == Vehicle.Usage {
    var localizedText: Text {
        Text(self.map{$0.displayValue.stringValue()}.joined(separator: ", "))
    }
}

extension LocalizedStringKey {
    var stringKey: String {
        let description = "\(self)"

        let components = description.components(separatedBy: "key: \"")
            .map { $0.components(separatedBy: "\",") }

        return components[1][0]
    }
}

extension String {
    static func localizedString(for key: String,
                                locale: Locale = .current) -> String {
        
        let language = locale.languageCode
        let path = Bundle.main.path(forResource: language, ofType: "lproj")!
        let bundle = Bundle(path: path)!
        let localizedString = NSLocalizedString(key, bundle: bundle, comment: "")
        
        return localizedString
    }
}

extension LocalizedStringKey {
    func stringValue(locale: Locale = .current) -> String {
        return .localizedString(for: self.stringKey, locale: locale)
    }
}
