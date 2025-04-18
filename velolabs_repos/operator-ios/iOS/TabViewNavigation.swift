//
//  TabViewNavigation.swift
//  Operator (iOS)
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import SwiftUI
//import StatefulTabView

final class TabViewNavigationLogicController: ObservableObject {
    
    @Published var selectedPage: Int = 0
    @Published var selectedTab: NavigationItem = .tickets
    let settings: UserSettings
    
    init(_ settings: UserSettings) {
        self.settings = settings
    }
    
    func searchAction() {
        switch selectedTab {
        case .tickets:
            settings.inject.ticketsLogic.sheetState = .filter
        case .vehicles:
            settings.inject.vehiclesLogic.sheetState = .search
        case .map:
            settings.inject.mapLogic.sheetState = .filter
        default:
            break
        }
    }
}

struct TabViewNavigation: View {
    
    @EnvironmentObject var settings: UserSettings
    @State fileprivate var activeSheet: Sheet?
    @Environment(\.presentationMode) var presentationMode
    @EnvironmentObject var logic: TabViewNavigationLogicController
    
    #if os(iOS)
    init() {
        let coloredAppearance = UINavigationBarAppearance()
        coloredAppearance.configureWithOpaqueBackground()
        coloredAppearance.backgroundColor = .accentColor
        coloredAppearance.titleTextAttributes = [.foregroundColor: UIColor.white]
        coloredAppearance.largeTitleTextAttributes = [.foregroundColor: UIColor.white]
        
        let button = UIBarButtonItemAppearance(style: .plain)
        button.normal.titleTextAttributes = [.foregroundColor: UIColor.white]
        coloredAppearance.buttonAppearance = button
        coloredAppearance.backButtonAppearance = button
        coloredAppearance.doneButtonAppearance = button
        
        UINavigationBar.appearance().standardAppearance = coloredAppearance
        UINavigationBar.appearance().scrollEdgeAppearance = coloredAppearance
        UINavigationBar.appearance().barTintColor = .white
        UINavigationBar.appearance().tintColor = .white
        
        if #available(iOS 15.0, *) {
            UITabBar.appearance().scrollEdgeAppearance = UITabBarAppearance()
        }
    }
    #endif
    
    var body: some View {
        NavigationView {
            TabView(selection: $logic.selectedTab) {
                TicketsView()
                    .environmentObject(settings.inject.ticketsLogic)
                    .tabItem {
                        Label("tickets", systemImage: "exclamationmark.triangle")
                    }
                    .tag(NavigationItem.tickets)
                    .id(NavigationItem.tickets)
                VehiclesListMapView()
                    .environmentObject(settings.inject.mapLogic)
                    .environmentObject(settings.inject.vehiclesLogic)
                    .tabItem {
                        Label("map", systemImage: "globe")
                    }
                    .tag(NavigationItem.map)
                    .id(NavigationItem.map)
                VehiclesView()
                    .environmentObject(settings.inject.vehiclesLogic)
                    .tabItem {
                        Label("vehicles", systemImage: "bicycle")
                    }
                    .tag(NavigationItem.vehicles)
                    .id(NavigationItem.vehicles)
                #if DEV
                EquipmentView()
                    .tabItem {
                        Label("equipment", systemImage: "lock.rectangle.on.rectangle")
                    }
                    .tag(NavigationItem.equipment)
                    .id(NavigationItem.equipment)
                #endif
            }
            .navigationBarTitleDisplayMode(.inline)
            .navigationTitle(settings.fleet.name ?? "undefined")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Menu(content: {
                        Button(action: { activeSheet = .userSettings }, label: {
                            Label("\(settings.user.fullName)\n\(settings.user.email)", systemImage: "person")
                        })
                        Button(action: { activeSheet = .fleetSettings }, label: {
                            Label("fleet-settings", systemImage: "gearshape")
                        })
                        Button(action: settings.logOut, label: {
                            Label("logout", systemImage: "arrow.turn.down.left")
                        })
                    }, label: {
                        Image(systemName: "person.crop.circle")
                    })
                    .foregroundColor(.white)
                }
                ToolbarItem(placement: .principal) {
                    Button(action: { settings.appState = .fleet }, label: {
                        Text(settings.fleet.name ?? "No name")
                    })
                    .buttonStyle(SelectButtonStyle())
                }
                ToolbarItem(placement: .automatic) {
                    Button(action: logic.searchAction) {
                        Image(systemName: "slider.horizontal.3")
                            
                    }
                    .foregroundColor(.white)
                }
            }
        }
        .appState(settings)
        .sheet(item: $activeSheet, content: { (sheet) in
            switch sheet {
            case .userSettings:
                UserSettingsView()
                    .environmentObject(settings)
            case .fleetSettings:
                FleetSettingsView()
                    .environmentObject(settings)
            }
        })
        .onAppear(perform: settings.checkCurrentStatus)
        .navigationViewStyle(.stack)
    }
}

extension TabViewNavigation {
    enum Sheet: Identifiable {
        case userSettings
        case fleetSettings
        
        var id: Int { hashValue }
    }
}


struct TabViewNavigation_Previews: PreviewProvider {
    static var previews: some View {
        TabViewNavigation()
            .environmentObject(UserSettings())
            .preferredColorScheme(.light)
    }
}

