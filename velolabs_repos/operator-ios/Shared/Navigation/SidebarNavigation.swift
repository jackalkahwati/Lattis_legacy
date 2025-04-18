//
//  SidebarNavigation.swift
//  Operator
//
//  Created by Ravil Khusainov on 18.03.2021.
//

import SwiftUI

struct SidebarNavigation: View {
    
    @EnvironmentObject var settings: UserSettings
    @State fileprivate var selection: NavigationItem? = .tickets
    
    var sidebar: some View {
        List(selection: $selection) {
            NavigationLink(destination: FleetsListView(logic: .init(settings)), label: {
                Label(settings.fleet.name ?? "Select fleet", systemImage: "network")
            })
            .tag(NavigationItem.fleets)
            NavigationLink(
                destination: TicketsView().environmentObject(settings.inject.ticketsLogic),
                label: {
                    Label("Tickets", systemImage: "exclamationmark.triangle")
                })
                .tag(NavigationItem.tickets)
            NavigationLink(
                destination: VehiclesView().environmentObject(settings.inject.vehiclesLogic),
                label: {
                    Label("Vehicles", systemImage: "bicycle")
                })
                .tag(NavigationItem.vehicles)
            NavigationLink(
                destination: VehiclesListMapView().environmentObject(VehiclesListMapViewModel(settings: settings)),//(viewModel: .init(settings: settings)),
                label: {
                    Label("map", systemImage: "globe")
                })
                .tag(NavigationItem.map)
        }
        .navigationTitle("operator")
        .onAppear {
            selection = .tickets
        }
        .listStyle(SidebarListStyle())
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Menu(content: {
                    Button(action: { }, label: {
                        Label("\(settings.user.fullName)\n\(settings.user.email)", systemImage: "person")
                    })
                    Button(action: {  }, label: {
                        Label("Fleet settings", systemImage: "gearshape")
                    })
                    Button(action: settings.logOut, label: {
                        Label("Log Out", systemImage: "arrow.turn.down.left")
                    })
                }, label: {
                    Image(systemName: "person.crop.circle")
                        .font(.title)
                })
                .foregroundColor(.white)
            }
        }
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
        .appState(settings)
        .onAppear(perform: settings.checkCurrentStatus)
    }
    
    init() {
        UINavigationBar.appearance().backgroundColor = .clear
        UINavigationBar.appearance().barTintColor = .accentColor
        UINavigationBar.appearance().tintColor = .black
    }
}

struct SidebarNavigation_Previews: PreviewProvider {
    static var previews: some View {
        SidebarNavigation()
    }
}
