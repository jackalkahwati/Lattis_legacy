//
//  CreateTicketCreateTicketInteractor.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 28/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Oval

class CreateTicketInteractor {
    weak var view: CreateTicketInteractorOutput!
    var router: CreateTicketRouter!
    var bike: Bike!
    var category: Ticket.Category!
    var photo: UIImage?
    fileprivate var handleError: ((Error) -> ())!
    
    fileprivate let network: TicketNetwork & FileNetwork
    init(network: TicketNetwork & FileNetwork = Session.shared) {
        self.network = network
        handleError = { [weak self] error in self?.view.show(error: error) }
    }
}

extension CreateTicketInteractor: CreateTicketInteractorInput {
    func viewLoaded() {
        view.show(bike: bike)
    }
    
    func createTicket(note: String) {
        func ticket(image: URL? = nil) {
            let ticket = Ticket.Create(bikeId: bike.bikeId, lockId: bike.lockId!, fleetId: bike.fleetId, category: category, operatorPhoto: image, maintenanceNotes: note)
            network.create(ticket: ticket) { [weak self] result in
                switch result {
                case .success:
                    self?.view.showSuccess()
                case .failure(let e):
                    self?.handleError(e)
                }
            }
        }
        view.startLoading(title: "create_ticket_loader".localized())
        if let photo = photo {
            network.upload(data: photo.jpegData(compressionQuality: 0.5)!, for: .parking) { [weak self] result in
                switch result {
                case .success(let url):
                    guard self != nil else { return }
                    ticket(image: url)
                case .failure(let e):
                    self?.handleError(e)
                }
            }
        } else {
            ticket()
        }
        
    }
    
    func select(category: Ticket.Category) {
        self.category = category
    }
}
