//
//  CreateTicketCreateTicketInteractorProtocols.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 28/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

protocol CreateTicketInteractorInput {
    var photo: UIImage? {get set}
    func viewLoaded()
    func createTicket(note: String)
    func select(category: Ticket.Category)
}

protocol CreateTicketInteractorOutput: LoaderPresentable, ErrorPresentable  {
    func show(bike: Bike)
    func showSuccess()
}
