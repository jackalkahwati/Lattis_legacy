//
//  TicketsLogicController.swift
//  Operator
//
//  Created by Ravil Khusainov on 05.03.2021.
//

import Foundation
import Combine

final class TicketsLogicController: ObservableObject {
    
    let ticketCreated: PassthroughSubject<Ticket, Never> = .init()
    @Published fileprivate(set) var searchTags: [Ticket.SearchTag] = []
    @Published fileprivate(set) var tickets: [Ticket] = .dummy
    @Published var viewState: ViewState = .initial
    @Published var presentingFilter: Bool = false
    @Published var sheetState: TicketsView.Sheet?
    @Published var isRefreshing: Bool = false
    let settings: UserSettings
    fileprivate var storage: Set<AnyCancellable> = []
    fileprivate var updatedAt = Date().addingTimeInterval(-updateInterval)
    
    init(_ settings: UserSettings) {
        self.settings = settings
        ticketCreated
            .sink { [weak self] (ticket) in
                self?.tickets.insert(ticket, at: 0)
            }
            .store(in: &storage)
        settings.inject.ticket
            .sink { [weak self] (ticket) in
                if ticket.metadata.status == .resolved {
                    self?.remove(ticket: ticket)
                }
            }
            .store(in: &self.storage)
        settings.inject.vehicle
            .sink { [weak self] (vehicle) in
                self?.update(vehicle: vehicle)
            }
            .store(in: &self.storage)
        settings.inject.fleet
            .sink { [unowned self] _ in
                updatedAt = Date().addingTimeInterval(-updateInterval)
                tickets = .dummy
                fetch()
            }
            .store(in: &storage)
    }
    
    func fetch() {
        guard settings.fleet.id != 0,
              -updatedAt.timeIntervalSinceNow > updateInterval else { return }
        isRefreshing = true
        viewState = .loading
        CircleAPI.tickets(settings.fleet.id, searchTags: searchTags)
            .map({$0.map({Ticket(metadata: $0)})})
            .sink(receiveCompletion: { [weak self] (com) in
                self?.isRefreshing = false
                self?.updatedAt = Date()
                switch com {
                case .finished:
                    self?.viewState = .screen
                case .failure(let error):
                    self?.tickets = []
                    self?.viewState = .error("Warning", error.localizedDescription)
                }
            }, receiveValue: { [weak self] (tickets) in
                self?.tickets = tickets
            })
            .store(in: &storage)
    }
    
    func search(tags: [Ticket.SearchTag]) {
        searchTags =  tags
        fetch()
    }
    
    func remove(ticket: Ticket) {
        guard let idx = tickets.firstIndex(where: {$0.id == ticket.id}) else { return }
        tickets.remove(at: idx)
    }
    
    fileprivate func update(vehicle: Vehicle) {
        for (idx, ticket) in tickets.enumerated() where ticket.vehicle?.id == vehicle.id {
            tickets[idx].vehicle = vehicle
        }
    }
}
