//
//  TicketDetailsLogicController.swift
//  Operator
//
//  Created by Ravil Khusainov on 09.03.2021.
//

import Foundation
import Combine
import CoreLocation

final class TicketDetailsLogicController: ObservableObject {
    
    @Published var selected: String
    @Published var viewState: ViewState = .screen
    @Published var ticket: Ticket
    @Published var assingee: FleetOperator?
    let settings: UserSettings
    let pages: [InfoPage]
    var notes: String? { ticket.metadata.operatorNotes }
    fileprivate(set) var operators: [FleetOperator] = []
    fileprivate var cancellables: Set<AnyCancellable> = []
        
    init(_ ticket: Ticket, settings: UserSettings) {
        self.ticket = ticket
        self.settings = settings
        var pages: [InfoPage] = [.ticket(ticket)]
        if let vehicle = ticket.vehicle {
            pages.append(.vehicle(vehicle))
            if !vehicle.things.isEmpty {
                pages.append(.equipment(vehicle.things))
            }
        }
        self.pages = pages
        self.selected = pages[0].id
        if ticket.assignee != .unassigned {
            assingee = ticket.assignee
        }
        fetchOperators()
    }
    
    func get<Value>(_ keyPath: KeyPath<Ticket.Metadata, Value>) -> Value {
        ticket.metadata[keyPath: keyPath]
    }
    
    func save(notes: String) {
        viewState = .loading
        CircleAPI.patch(ticket: ticket.id, json: .init(notes: notes))
            .sink { [weak self] com in
                switch com {
                case .failure(let error):
                    self?.viewState = .error("Warning", error.localizedDescription)
                case .finished:
                    self?.viewState = .screen
                }
            } receiveValue: { [weak self] meta in
                self?.ticket.metadata = meta
            }
            .store(in: &cancellables)
    }
    
    func resolve(completion: @escaping () -> Void) {
        viewState = .loading
        CircleAPI.patch(ticket: ticket.id, json: .init(status: .resolved))
            .sink { [weak self] com in
                switch com {
                case .failure(let error):
                    self?.viewState = .error("Warning", error.localizedDescription)
                case .finished:
                    self?.viewState = .screen
                    completion()
                    self?.ticket.metadata.status = .resolved
                    self?.settings.inject.ticket.send(self!.ticket)
                }
            } receiveValue: { _ in
                
            }
            .store(in: &cancellables)
    }
    
    func fetchOperators() {
        let setAssignee: () -> Void = { [weak self] in
            if let assignee = self?.ticket.assignee, assignee != .unassigned {
                self?.assingee = assignee
            } else if let id = self?.ticket.metadata.assignee {
                self?.assingee = self?.operators.first(where: {$0.id == id})
            } else {
                self?.assingee = .unassigned
            }
        }
        func apiFetch() {
            CircleAPI.colleagues(fleetId: ticket.metadata.fleetId)
                .sink { com in
                } receiveValue: { [weak self] colleagues in
                    guard self != nil else { return }
                    self?.operators = colleagues
                    setAssignee()
                    self?.settings.storage.save(colleagues)
                }
                .store(in: &cancellables)
        }
        settings.storage.fetch(type: [FleetOperator].self)
            .sink { result in
            } receiveValue: { [weak self]  colleagues in
                guard self != nil else { return }
                self?.operators = colleagues
                if colleagues.isEmpty {
                    apiFetch()
                } else {
                    setAssignee()
                }
            }
            .store(in: &cancellables)
    }
    
    func assign(to oper: FleetOperator) {
        viewState = .loading
        CircleAPI.patch(ticket: ticket.id, json: .init(assignee: oper.id))
            .sink { com in
                switch com {
                case .finished:
                    self.viewState = .screen
                case .failure(let error):
                    self.viewState = .error("Warning", error.localizedDescription)
                }
            } receiveValue: { [weak self] meta in
                self?.ticket.metadata = meta
                self?.ticket.assignee = oper
                self?.assingee = oper
            }
            .store(in: &cancellables)
    }
    
    func update(vehicle: Vehicle) {
        ticket.vehicle = vehicle
    }
}
