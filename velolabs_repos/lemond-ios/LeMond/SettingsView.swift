//
//  SettingsView.swift
//  LeMond
//
//  Created by Ravil Khusainov on 27.12.2021.
//

import SwiftUI

struct SettingsView: View {
    
    @EnvironmentObject var viewModel: SettingsViewModel
    
    var body: some View {
        Form {
            Section("Info") {
                SettingsFormItemInfo(name: "Bike name", value: "Greg's Prolog")
                SettingsFormItemInfo(name: "Owner", value: viewModel.bikeOwner)
                SettingsFormItemInfo(name: "Serial Number", value: "1376090002")
                SettingsFormItemInfo(name: "Firmware", value: "1.0.1")
            }
            Section("Sharing") {
                ForEach(viewModel.sharedWith) { person in
                    Button(action: { viewModel.revokePerson = person }) {
                        SettingsFormItemInfo(name: person.status, value: person.fullName)
                    }
                    .foregroundColor(.black)
                }
                Button(action: { viewModel.sheetState = .share }){
                    Label("Share", systemImage: "plus.circle")
                }
            }
        }
        .background(Color.background)
        .actionSheet(item: $viewModel.revokePerson) { person in
            ActionSheet(title: Text(person.fullName), message: Text("This person will no longer have access to your bike. Do you want to revoke access?"), buttons: [
                .cancel(),
                .default(Text("Revoke")) {
                    viewModel.revokeAccess(person)
                }
            ])
        }
        .sheet(item: $viewModel.sheetState) { state in
            NewPersonView()
                .environmentObject(NewPersonViewModel(viewModel.granAccess(_:)))
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView()
    }
}

struct SettingsFormItemInfo: View {
    
    let name: String
    let value: String
    
    var body: some View {
        HStack {
            Text(name)
                .fontWeight(.light)
            Spacer()
            Text(value)
        }
        .font(.headline)
    }
}
