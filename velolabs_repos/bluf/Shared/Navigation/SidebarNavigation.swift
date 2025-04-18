//
//  SidebarNavigation.swift
//  BLUF
//
//  Created by Ravil Khusainov on 13.08.2020.
//

import SwiftUI

struct SidebarNavigation: View {
    
    @State fileprivate var selection: Set<NavigationItem> = [.bikes]
    
    var sidebar: some View {
        List(selection: $selection) {
            NavigationLink(destination: BikesView()) {
                Label("Bikes", systemImage: "bicycle")
            }
            .accessibility(label: Text("Bikes"))
            .tag(NavigationItem.bikes)
            NavigationLink(destination: LocksView()) {
                Label("Locks", systemImage: "lock")
            }
            .accessibility(label: Text("Locks"))
            .tag(NavigationItem.locks)
            NavigationLink(destination: UsersView()) {
                Label("Users", systemImage: "person")
            }
            .accessibility(label: Text("Users"))
            .tag(NavigationItem.users)
            NavigationLink(destination: FleetsView()) {
                Label("Fleets", systemImage: "network")
            }
            .accessibility(label: Text("Fleets"))
            .tag(NavigationItem.fleets)
            NavigationLink(destination: ThingsView()) {
                Label("Things", systemImage: "simcard")
            }
            .accessibility(label: Text("Things"))
            .tag(NavigationItem.IoT)
        }
        .navigationTitle("BLUF")
        .listStyle(SidebarListStyle())
    }
    
    var body: some View {
        NavigationView {
            #if os(iOS)
            sidebar
            #else
            sidebar
                .frame(minWidth: 150, idealWidth: 200, maxWidth: 250)
            #endif
        }
    }
}

struct SidebarNavigation_Previews: PreviewProvider {
    static var previews: some View {
        SidebarNavigation()
    }
}
