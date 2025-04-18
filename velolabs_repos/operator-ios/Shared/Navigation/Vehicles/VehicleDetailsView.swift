//
//  VehicleDetailsView.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.03.2021.
//

import SwiftUI

struct ThingSectionListItem: View {
    let thing: Thing
    var body: some View {
        HStack {
            Label(thing.metadata.vendor, systemImage: thing.deviceType.imageName)
            Spacer()
            Text(thing.metadata.deviceType)
                .foregroundColor(.secondary)
        }
    }
}

struct VehicleDetailsView: View {
    
    @StateObject var logic: VehicleDetailsLogicController
    @EnvironmentObject var settings: UserSettings
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        VStack {
            PageViewNavigationBar(titles: logic.pages.map(\.id), selected: $logic.selected)
            TabView(selection: $logic.selected) {
                ForEach(logic.pages) { page in
                    switch page {
                    case .vehicle:
                        VehiclePageItem(logic: logic)
                            .tag(page.id)
                            .id(page)
                    case .map(let coordinate):
                        MapPageItem(coordinate)
                            .tag(page.id)
                            .id(page)
                    case .tickets(let vehicle):
                        VehicleDetailsTicketsPage(logic: .init(vehicle, settings: settings))
                            .tag(page.id)
                            .id(page)
                    case .equipment(let equipment):
                        EquipmentPageItem(logic: .init(equipment))
                            .tag(page.id)
                            .id(page)
                    default: Text("No corresponding view").tag(page.id).id(page)
                    }
                }
            }
            .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))
            .animation(.easeIn)
        }
        .viewState($logic.viewState)
        .navigationTitle(logic.title)
        .animation(nil)
    }
}

struct VehicleDetailsView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            VehicleDetailsView(logic: .init([Vehicle].dummy.first!, settings: UserSettings()))
                .navigationBarTitleDisplayMode(.inline)
        }
        .onAppear {
            UINavigationBar.appearance().backgroundColor = .clear
            UINavigationBar.appearance().barTintColor = .accentColor
            UINavigationBar.appearance().tintColor = .black
        }
    }
}
