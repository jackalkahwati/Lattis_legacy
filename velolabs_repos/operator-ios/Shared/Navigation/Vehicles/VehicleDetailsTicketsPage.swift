//
//  VehicleDetailsTicketsPage.swift
//  Operator
//
//  Created by Ravil Khusainov on 24.03.2021.
//

import SwiftUI

struct TicketFormItem: View {
    let ticket: Ticket
    var body: some View {
        HStack {
            Image(systemName: ticket.imageName)
            VStack(alignment: .leading) {
                Text(ticket.createdAt())
                    .font(.footnote)
                Text(ticket.category)
                    .font(.subheadline)
            }
//            Label(ticket.category, systemImage: ticket.imageName)
        }
    }
}

struct VehicleDetailsTicketsPage: View {
    
    @StateObject var logic: VehicleDetailsTicketsLogicController
    @State fileprivate var creatingTicket: Bool = false
    @State fileprivate var isTicketShown = false {
        didSet {
            if isTicketShown == false {
                logic.selected = nil
            }
        }
    }
    @Environment(\.presentationMode) var presentationMode
    @EnvironmentObject var settings: UserSettings
    
    var body: some View {
        VStack {
            Form {
                ForEach(logic.tickets) { ticket in
                    NavigationLink(
                        destination: TicketPageItem(logic: .init(ticket, settings: settings)),
                        tag: ticket,
                        selection: $logic.selected,
                        label: {
                            TicketFormItem(ticket: ticket).id(ticket)
                    })
                }
            }
            Button(action: {
                creatingTicket.toggle()
            }) {
                Text("create-ticket")
            }
            .buttonStyle(CreateButtonStyle())
            .padding([.leading, .bottom, .trailing])
        }
        .sheet(isPresented: $creatingTicket) {
            CreateTicketView()
                .environmentObject(CreateTicketLogicController(settings, created: logic.ticketCreated, vehicle: logic.vehicle))
        }
    }
}

//struct VehicleDetailsTicketsPage_Previews: PreviewProvider {
//    static var previews: some View {
//        VehicleDetailsTicketsPage()
//    }
//}
