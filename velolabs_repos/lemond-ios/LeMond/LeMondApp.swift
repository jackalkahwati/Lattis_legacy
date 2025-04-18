//
//  LeMondApp.swift
//  LeMond
//
//  Created by Ravil Khusainov on 27.12.2021.
//

import SwiftUI

@main
struct LeMondApp: App {
    var body: some Scene {
        WindowGroup {
            TabViewNavigation()
                .environmentObject(NavigationViewModel())
        }
    }
}
