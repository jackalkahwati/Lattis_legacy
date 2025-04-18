//
//  TicketItem.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import SwiftUI

struct TicketItem: View {
    
    @State var ticket: Ticket
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack(spacing: 5) {
                Image(systemName: ticket.imageName)
                VStack(alignment: .leading) {
                    HStack {
                        Text(ticket.vehicleName)
                        Spacer()
                    }
                    Text(ticket.category)
                        .fontWeight(.regular)
                        .foregroundColor(.secondary)
                    HStack {
                        Text(ticket.createdAt())
                        Spacer()
                    }
                    .font(.subheadline)
                    .padding(.top, 3)
                    .foregroundColor(.secondary)
                }
            }
            .font(.headline)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color.accentColor.opacity(0.5))
        .cornerRadius(10)
    }
}

struct TicketItem_Previews: PreviewProvider {
    static var previews: some View {
        TicketItem(ticket: [Ticket].dummy.first!)
    }
}

extension Ticket {
    var imageName: String {
        metadata.category.imageName
    }
    
    var vehicleName: String {
        vehicle?.name ?? "No vehicle"
    }
    
    var category: LocalizedStringKey {
        metadata.category.title
    }
}
