//
//  UserSettingsView.swift
//  Operator
//
//  Created by Ravil Khusainov on 03.03.2021.
//

import SwiftUI

struct UserSettingsView: View {
    
    @EnvironmentObject fileprivate var settings: UserSettings
    @Environment(\.presentationMode) fileprivate var presentation
    
    var body: some View {
        NavigationView {
            Group {
                if let user = settings.user {
                    Form {
                        Section(header: Text("")) {
                            if let name = user.firstName {
                                FormLabel(title: "First Name", value: name)
                            }
                            if let name = user.lastName {
                                FormLabel(title: "Last Name", value: name)
                            }
                            FormLabel(title: "email", value: user.email)
                            if let phone = user.phoneNumber {
                                FormLabel(title: "Phone number", value: phone)
                            }
                        }
                    }
                } else {
                    Text("User not found")
                }
            }
            .navigationTitle("operator")
            .toolbar(content: {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { presentation.wrappedValue.dismiss() }, label: {
                        Text("Close")
                    })
                        .foregroundColor(.white)
                }
            })
        }
    }
}

struct UserSettingsView_Previews: PreviewProvider {
    static var previews: some View {
        UserSettingsView()
            .environmentObject(UserSettings())
    }
}
