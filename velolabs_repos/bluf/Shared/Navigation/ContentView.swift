//
//  ContentView.swift
//  Shared
//
//  Created by Ravil Khusainov on 11.08.2020.
//

import SwiftUI

struct ContentView: View {
    #if os(iOS)
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    #endif
    
    var body: some View {
        #if os(iOS)
        if horizontalSizeClass == .compact {
            TabViewNavigation()
        } else {
            SidebarNavigation()
        }
        #else
        SidebarNavigation()
        #endif
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
            .preferredColorScheme(.dark)
            .previewDevice("iPad Pro (11-inch) (2nd generation)")
    }
}
