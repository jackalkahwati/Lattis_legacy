//
//  IoTModulesView.swift
//  BLUF
//
//  Created by Ravil Khusainov on 22.08.2020.
//

import SwiftUI


struct ThingsView: View {
    @State var isFilterShown = false
    var body: some View {
        Text("Things")
            .navigationTitle("Things")
            .toolbar {
                ToolbarItem {
                    Button() {
                        isFilterShown.toggle()
                    } label: {
                        Image(systemName: "magnifyingglass")
                    }
                }
            }
    }
}

struct ThingsView_Previews: PreviewProvider {
    static var previews: some View {
        ThingsView()
    }
}
