//
//  TicketDetailsView.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import SwiftUI

struct TicketDetailsView: View {
    
    @StateObject var logic: TicketDetailsLogicController
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        VStack {
            PageViewNavigationBar(titles: logic.pages.map(\.id), selected: $logic.selected)
            TabView(selection:$logic.selected) {
                ForEach(logic.pages) { page in
                    switch page {
                    case .ticket:
                        TicketPageItem(logic: logic)
                            .tag(page.id)
                    case .vehicle(let vehicle):
                        VehiclePageItem(logic: .init(vehicle, settings: logic.settings))
                            .tag(page.id)
                    case .map(let coordinate):
                        MapPageItem(coordinate)
                            .tag(page.id)
                    case .equipment(let equipment):
                        EquipmentPageItem(logic: .init(equipment))
                            .tag(page.id)
                    default: Text("No corresponding view").tag(page.id)
                    }
                }
            }
            .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))
        }
        .navigationTitle("Ticket")
        .viewState($logic.viewState)
    }
}

#if os(iOS)
struct TicketDetailsView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            TicketDetailsView(logic: .init([Ticket].dummy.first!, settings: UserSettings()))
                .navigationBarTitleDisplayMode(.inline)
        }
        .onAppear {
            UINavigationBar.appearance().backgroundColor = .clear
            UINavigationBar.appearance().barTintColor = .accentColor
            UINavigationBar.appearance().tintColor = .black
        }
    }
}
#endif
