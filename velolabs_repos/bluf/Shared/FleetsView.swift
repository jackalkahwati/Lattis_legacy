//
//  FleetsView.swift
//  BLUF
//
//  Created by Ravil Khusainov on 13.08.2020.
//

import SwiftUI

struct FleetsView: View {
    @State var isFilterShown = false
    var body: some View {
        Text("Fleets")
            .navigationTitle("Fleets")
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

struct FleetsView_Previews: PreviewProvider {
    static var previews: some View {
        FleetsView()
    }
}
