//
//  TicketDetailsTicketDetailsInteractorProtocols.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 27/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import CoreLocation

protocol TicketDetailsInteractorInput {
    func viewLoaded()
    func assign(oper: Operator?)
    func getDirection(userCoordinate: CLLocationCoordinate2D)
    func resolve()
}

protocol TicketDetailsInteractorOutput: LoaderPresentable, ErrorPresentable {
    func show(ticket: Ticket)
    func show(operators: [Operator], selected: Int, unassigned: Bool)
    func show(address topLine: String, bottomLine: String?)
}
