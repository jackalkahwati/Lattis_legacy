//
//  TicketsView.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import SwiftUI

struct TicketsView: View {
    
    @EnvironmentObject var logic: TicketsLogicController
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        VStack {
            ScrollView {
                LazyVGrid(
                    columns: [GridItem(.adaptive(minimum: 300))],
                    spacing: 5,
                    pinnedViews: [.sectionHeaders],
                    content: {
                        Section(header: sectionHeader) {
                            ForEach(logic.tickets) { ticket in
                                NavigationLink(
                                    destination: TicketDetailsView(logic: .init(ticket, settings: logic.settings)),
                                    label: {
                                        TicketItem(ticket: ticket)
                                            .id(ticket)
                                    })
                                    .buttonStyle(PlainButtonStyle())
                            }
                        }
                    })
                    .padding(.horizontal)
            }
            .placeholder(logic.tickets.isEmpty, view: EmptyListView(message: "no-tickets"))
            Button {
                logic.sheetState = .create
            } label: {
                Label("create-ticket", systemImage: "plus.circle")
            }
            .buttonStyle(CreateButtonStyle())
            .padding(.horizontal)
        }
        .padding(.vertical)
        .animation(.none)
        .navigationTitle("tickets")
        .sheet(item: $logic.sheetState, content: { state in
            switch state {
            case .create:
                CreateTicketView()
                    .environmentObject(CreateTicketLogicController(logic.settings, created: logic.ticketCreated))
            case .filter:
                TicketsFilterView()
                    .environmentObject(TicketsFilterViewModel(logic))
            }
        })
        .viewState($logic.viewState, onAppear: logic.fetch)
    }
    
    @ViewBuilder
    var sectionHeader: some View {
        if logic.searchTags.isEmpty {
            EmptyView()
        } else {
            ScrollView(.horizontal) {
                HStack {
                    ForEach(logic.searchTags) { tag in
                        TagView(tag: tag, action: { logic.sheetState = .filter })
                    }
                }
            }
            .padding(.vertical)
            .background(Color.background)
        }
    }
}

extension TicketsView {
    enum Sheet: Identifiable {
        case create
        case filter
        
        var id: Self { self }
    }
}

struct TicketsView_Previews: PreviewProvider {
    @State static var settings = UserSettings()
    static var previews: some View {
        TicketsView()
            .environmentObject(settings.inject.ticketsLogic)
            .environmentObject(settings)
    }
}
