//
//  TicketsTicketsInteractor.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import Oval

class TicketsInteractor: NSObject {
    typealias Storage = TicketsStorage & OperatorsStorage & FleetsStorage
    weak var view: TicketsInteractorOutput!
    var router: TicketsRouter!
    weak var delegate: TicketsInteractorDelegate!
    
    fileprivate var filter: Filter = .all
    fileprivate var sorting: Sort = .date
    fileprivate let sort: (Ticket, Ticket) -> Bool = { $0.created > $1.created }
    fileprivate var showError: ((Error) -> ())!
    fileprivate var handler: StorageHandler?
    
    typealias Network = TicketNetwork & OperatorNetwork
    fileprivate let network: Network
    fileprivate let storage: Storage
    init(network: Network = Session.shared, storage: Storage = CoreDataStack.shared) {
        self.network = network
        self.storage = storage
        super.init()
        showError = { [weak self] error in
            self?.view.endRefresh()
            self?.view.show(error: error)
        }
    }
}

extension TicketsInteractor: TicketsInteractorInput {
    func didSelect(value: FilterRepresentable) -> Bool {
        if let filter = value as? Filter, self.filter != filter {
            self.filter = filter
            return true
        } else if let sort = value as? Sort, self.sorting != sort {
            self.sorting = sort
            return true
        }
        return false
    }

    func viewLoaded() {
        guard let fleet = storage.currentFleet else { return }
        handler = storage.subscribe(in: fleet) {  [weak self] (tickets) in
            self?.view.show(tickets: tickets.sorted(by: self!.sort))
        }
        
        refreshOperators(for: fleet)
    }
    
    func refreshTickets() {
        guard let fleet = storage.currentFleet else { return }
        refreshTickets(for: fleet)
    }
}

private extension TicketsInteractor {
    func refreshTickets(for fleet: Fleet) {
        network.getTickets(for: fleet) { [weak self] (result) in
            switch result {
            case .success(let tickets):
                self?.view.endRefresh()
                self?.storage.save(tickets, for: fleet, update: true){}
            case .failure(let e):
                self?.showError(e)
            }
        }
    }
    
    func refreshOperators(for fleet: Fleet) {
        network.getOperators(for: fleet) { [weak self] result in
            switch result {
            case .success(let operators):
                self?.storage.save(operators, for: fleet, update: true)
            case .failure(let e):
                self?.showError(e)
            }
        }
    }
}
