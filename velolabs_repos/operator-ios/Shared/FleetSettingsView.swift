//
//  FleetSettingsView.swift
//  Operator
//
//  Created by Ravil Khusainov on 03.03.2021.
//

import SwiftUI

struct FleetSettingsView: View {
    
    @EnvironmentObject fileprivate var settings: UserSettings
    @Environment(\.presentationMode) fileprivate var presentation
    @ObservedObject fileprivate var logic = FleetSettingsLogicController()
    
    var body: some View {
        NavigationView {
            Form {
                Section {
                    Picker(selection: $settings.fleet, label: Text("name"), content: {
                        ForEach(logic.fleets) { fleet in
                            Text(fleet.name ?? "undefined").tag(fleet)
                        }
                    })
                }
                if let address = settings.fleet.fullAddress {
                    Section(header: Text("address")) {
                        Text(address)
                    }
                }
            }
            .navigationTitle("fleet")
            .toolbar(content: {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { presentation.wrappedValue.dismiss() }, label: {
                        Text("close")
                    })
                }
            })
        }
    }
}

struct FleetSettingsView_Previews: PreviewProvider {
    static var previews: some View {
        FleetSettingsView()
            .environmentObject(UserSettings())
    }
}
