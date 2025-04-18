//
//  ProfileView.swift
//  LeMond
//
//  Created by Ravil Khusainov on 27.12.2021.
//

import SwiftUI

struct ProfileView: View {
    
    @EnvironmentObject var settings: AppSettings
    
    var body: some View {
        VStack {
            Form {
                Section("Info") {
                    HStack {
                        Image(systemName: "person")
                        TextField("Name", text: $settings.name)
                    }
                    HStack {
                        Image(systemName: "envelope")
                        TextField("Email", text: $settings.email)
                    }
                    HStack {
                        Image(systemName: "phone")
                        TextField("Phone number", text: $settings.phone)
                    }
                    
//                    SettingsFormItemInfo(name: "Name", value: "Greg LeMond")
//                    SettingsFormItemInfo(name: "Email", value: "greg@lemond.com")
//                    SettingsFormItemInfo(name: "Phone", value: "+33 898 787347")
                }
                Section("Statistics") {
                    SettingsFormItemInfo(name: "Today", value: "23 miles")
                    SettingsFormItemInfo(name: "Total", value: "1098 miles")
                }
                Section {
                    Button {
                        
                    } label: {
                        HStack {
                            Spacer()
                            Text("Log Out")
                            Spacer()
                        }
                    }
                }
            }
        }
        .background(Color.background)
    }
}

struct ProfileView_Previews: PreviewProvider {
    static var previews: some View {
        ProfileView()
    }
}
