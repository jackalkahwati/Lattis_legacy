//
//  TicketsTicketsInteractorProtocols.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol TicketsInteractorDelegate: class {
    func select(ticket: Ticket)
    func createTicket()
}

protocol TicketsInteractorInput {
    var delegate: TicketsInteractorDelegate! {get set}
    func viewLoaded()
    func refreshTickets()
    func didSelect(value: FilterRepresentable) -> Bool
}

protocol TicketsInteractorOutput: LoaderPresentable, ErrorPresentable {
    func show(tickets: [Ticket])
    func endRefresh()
}

