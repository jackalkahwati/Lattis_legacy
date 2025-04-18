//
//  TabViewNavigation.swift
//  LeMond
//
//  Created by Ravil Khusainov on 27.12.2021.
//

import SwiftUI

struct TabViewNavigation: View {
    @EnvironmentObject var viewModel: NavigationViewModel
    var body: some View {
        NavigationView {
            TabView(selection: $viewModel.currentView) {
                HomeView()
                    .environmentObject(HomeViewModel(viewModel.settings))
                    .tabItem {
                        Label("Home", systemImage: "house")
                    }
                    .tag(NavigationItem.home)
                    .id(NavigationItem.home)
                SettingsView()
                    .environmentObject(SettingsViewModel(viewModel.settings))
                    .tabItem {
                        Label("Settings", systemImage: "gear")
                    }
                    .tag(NavigationItem.settings)
                    .id(NavigationItem.settings)
                MapView()
                    .environmentObject(MapViewModel(viewModel.settings))
                    .tabItem {
                        Label("Map", systemImage: "mappin.and.ellipse")
                    }
                    .tag(NavigationItem.map)
                    .id(NavigationItem.map)
                ProfileView()
                    .environmentObject(viewModel.settings)
                    .tabItem {
                        Label("Profile", systemImage: "person")
                    }
                    .tag(NavigationItem.profile)
                    .id(NavigationItem.profile)
            }
            .navigationTitle(viewModel.currentView.title)
            .toolbar {
                ToolbarItem(placement: .automatic) {
                    toolbarContent
                }
            }
        }
        .sheet(item: $viewModel.sheetState) { state in
            AddBikeView()
                .environmentObject(AddBikeViewModel())
        }
    }
    
    @ViewBuilder
    var toolbarContent: some View {
        switch viewModel.currentView {
        case .home:
            Button(action: viewModel.addBike) {
                Image(systemName: "plus.circle")
            }
            .foregroundColor(.black)
            .font(.title)
        default:
            EmptyView()
        }
    }
}

struct TabViewNavigation_Previews: PreviewProvider {
    static var previews: some View {
        TabViewNavigation()
            .environmentObject(NavigationViewModel())
    }
}

enum NavigationItem: Identifiable {
    case home, settings, map, profile
    
    var id: NavigationItem { self }
    
    var title: String {
        switch self {
        case .settings:
            return "Settings"
        case .home:
            return "Greg's Prolog"
        case .map:
            return "Find my bike"
        case .profile:
            return "Profile"
        }
    }
}


final class NavigationViewModel: ObservableObject {
    @Published var currentView: NavigationItem = .home
    @Published var settings: AppSettings = .init()
    @Published var sheetState: SheetState?
    
    func addBike() {
        sheetState = .addBike
    }
}

extension NavigationViewModel {
    enum SheetState: Identifiable {
        case addBike
        
        var id: Self { self }
    }
}
