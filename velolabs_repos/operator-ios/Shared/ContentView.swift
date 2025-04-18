//
//  ContentView.swift
//  Shared
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import SwiftUI

struct ContentView: View {
    
    @StateObject var settings = UserSettings()
    #if os(iOS)
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    #endif
    
    var body: some View {
        #if os(iOS)
        if horizontalSizeClass == .compact {
            TabViewNavigation()
                .environmentObject(TabViewNavigationLogicController(settings))
                .environmentObject(settings)
        } else {
            SidebarNavigation()
                .environmentObject(settings)
        }
        #else
        SidebarNavigation()
            .environmentObject(settings)
        #endif
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
