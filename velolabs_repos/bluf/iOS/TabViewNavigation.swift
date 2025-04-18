//
//  TabViewNavigation.swift
//  BLUF (iOS)
//
//  Created by Ravil Khusainov on 13.08.2020.
//

import SwiftUI

struct TabViewNavigation: View {
    
    @State fileprivate var selected: NavigationItem = .bikes
    
    var body: some View {
        TabView(selection: $selected) {
            NavigationView {
                BikesView()
            }
            .tabItem {
                Label("Bikes", systemImage: "bicycle")
                    .accessibility(label: Text("Bikes"))
            }
            .tag(NavigationItem.bikes)
            NavigationView {
                LocksView()
            }
            .tabItem {
                Label("Locks", systemImage: "lock")
                    .accessibility(label: Text("Locks"))
            }
            .tag(NavigationItem.locks)
            NavigationView {
                UsersView()
            }
            .tabItem {
                Label("Users", systemImage: "person")
                    .accessibility(label: Text("Users"))
            }
            .tag(NavigationItem.users)
            NavigationView {
                FleetsView()
            }
            .tabItem {
                Label("Fleets", systemImage: "network")
                    .accessibility(label: Text("Fleets"))
            }
            .tag(NavigationItem.fleets)
            NavigationView {
                ThingsView()
            }
            .tabItem {
                Label("Things", systemImage: "simcard")
                    .accessibility(label: Text("IoT"))
            }
            .tag(NavigationItem.IoT)
        }
    }
}


struct TabViewNavigation_Previews: PreviewProvider {
    static var previews: some View {
        TabViewNavigation()
            .previewDevice("iPhone 11")
    }
}
