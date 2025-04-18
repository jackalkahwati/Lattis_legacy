//
//  VehicleDetailsTicketsLogicController.swift
//  Operator
//
//  Created by Ravil Khusainov on 24.03.2021.
//

import Foundation
import Combine

final class VehicleDetailsTicketsLogicController: ObservableObject {
    
    let vehicle: Vehicle
    let settings: UserSettings
    let ticketCreated: PassthroughSubject<Ticket, Never> = .init()
    @Published var selected: Ticket?
    @Published fileprivate(set) var tickets: [Ticket] = []
    fileprivate var cancellables: Set<AnyCancellable> = []
    
    init(_ vehicle: Vehicle, settings: UserSettings) {
        self.vehicle = vehicle
        self.settings = settings
        fetchTickets()
        ticketCreated.sink { [weak self] ticket in
            self?.tickets.insert(ticket, at: 0)
        }
        .store(in: &cancellables)
        settings.inject.ticket.sink { [weak self] (ticket) in
            if ticket.metadata.status == .resolved {
                self?.resolved(ticket)
            }
        }
        .store(in: &cancellables)
    }
    
    fileprivate func fetchTickets() {
        CircleAPI.tickets(vehicle.metadata.fleetId, searchTags: [.vehicle(vehicle.id)])
            .map({$0.map{Ticket.init(metadata: $0)}})
            .sink { (com) in
                
            } receiveValue: { [weak self] (tickets) in
                self?.tickets = tickets
            }
            .store(in: &cancellables)
    }
    
    fileprivate func resolved(_ ticket: Ticket) {
        guard let idx = tickets.firstIndex(where: {$0.id == ticket.id}) else { return }
        tickets.remove(at: idx)
    }
}
