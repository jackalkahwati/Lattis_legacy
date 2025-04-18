//
//  UsersView.swift
//  BLUF
//
//  Created by Ravil Khusainov on 13.08.2020.
//

import SwiftUI

struct UsersView: View {
    @State var isFilterShown = false
    var body: some View {
        Text("Users")
            .navigationTitle("Users")
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

struct UsersView_Previews: PreviewProvider {
    static var previews: some View {
        UsersView()
    }
}
