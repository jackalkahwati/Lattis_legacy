//
//  ThingControlView.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.03.2021.
//

import SwiftUI

struct ThingControlView: View {
    
    let thing: Thing
    @State var isLocked = true
    
    var body: some View {
        VStack {
            Form {
                FormLabel(title: "Key", value: thing.metadata.key)
                FormLabel(title: "Device Type", value: thing.metadata.deviceType)
                Toggle("Locked", isOn: $isLocked)
            }
            Button(action: {}, label: {
                Text("Unassign")
            })
            .buttonStyle(CreateButtonStyle())
            .padding()
        }
        .navigationTitle(thing.metadata.vendor)
    }
}

#if os(iOS)
struct ThingControlView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ThingControlView(thing: [Thing].dummy.last!)
                .navigationBarTitleDisplayMode(.inline)
        }
        .onAppear {
            UINavigationBar.appearance().backgroundColor = .clear
            UINavigationBar.appearance().barTintColor = .accentColor
            UINavigationBar.appearance().tintColor = .black
        }
    }
}
#endif
