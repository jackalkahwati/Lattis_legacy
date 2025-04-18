//
//  TicketDetailsTicketDetailsInteractor.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 27/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import Oval
import MapboxGeocoder

class TicketDetailsInteractor {
    typealias Storage = OperatorsStorage & FleetsStorage & TicketsStorage
    weak var view: TicketDetailsInteractorOutput!
    var router: TicketDetailsRouter!
    var ticket: Ticket!
    
    fileprivate let storage: Storage
    fileprivate let network: TicketNetwork
    init(storage: Storage = CoreDataStack.shared, network: TicketNetwork = Session.shared) {
        self.storage = storage
        self.network = network
    }
}

extension TicketDetailsInteractor: TicketDetailsInteractorInput {
    func viewLoaded() {
        if let isNew = ticket.isNew, let fleet = storage.currentFleet, isNew {
            ticket.isNew = false
            storage.save([ticket], for: fleet, update: false){}
        }
        resolveLocation()
        view.show(ticket: ticket)
        guard let fleet = storage.currentFleet else { return }
        let operators = storage.getOperators(by: fleet)
        let idx = operators.firstIndex(where: { $0.operatorId == ticket.assigneeId })
        view.show(operators: operators, selected: idx ?? 0, unassigned: idx == nil)
    }
    
    func assign(oper: Operator?) {
        guard let oper = oper, oper.operatorId != ticket.assigneeId else { return }
        network.assign(oper: oper, to: ticket) { [weak self] result in
            switch result {
            case .success:
                self?.save(assigned: oper)
            case .failure(let error):
                self?.view.show(error: error)
            }
        }
    }
    
    func getDirection(userCoordinate: CLLocationCoordinate2D) {
        guard CLLocationCoordinate2DIsValid(userCoordinate) else {
            return view.showAlert(title: "ticket_details_direction_alert_title".localized(), subtitle: "ticket_details_direction_alert_text".localized())
        }
        
        let action = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        let destination = "\(ticket.coordinate.latitude),\(ticket.coordinate.longitude)"
        let google = URL(string: "comgooglemaps://?daddr=\(destination)")!
        let waze = URL(string: "waze://?ll=\(destination)")!
        let apple = URL(string: "http://maps.apple.com/?daddr=\(destination)")!
        let app = UIApplication.shared
        if app.canOpenURL(google) {
            action.addAction(UIAlertAction(title: "ticket_details_navigation_google".localized(), style: .default, handler: { (_) in
                app.open(google, options: convertToUIApplicationOpenExternalURLOptionsKeyDictionary([:]), completionHandler: nil)
            }))
        }
        if app.canOpenURL(waze) {
            action.addAction(UIAlertAction(title: "ticket_details_navigation_waze".localized(), style: .default, handler: { (_) in
                app.open(waze, options: convertToUIApplicationOpenExternalURLOptionsKeyDictionary([:]), completionHandler: nil)
            }))
        }
        if action.actions.isEmpty {
            app.open(apple, options: convertToUIApplicationOpenExternalURLOptionsKeyDictionary([:]), completionHandler: nil)
        } else {
            action.addAction(UIAlertAction(title: "ticket_details_navigation_apple".localized(), style: .default, handler: { (_) in
                app.open(apple, options: convertToUIApplicationOpenExternalURLOptionsKeyDictionary([:]), completionHandler: nil)
            }))
            action.addAction(UIAlertAction(title: "general_btn_cancel".localized(), style: .cancel))
            router.show(action: action)
        }
    }
    
    func resolve() {
        view.startLoading(title: "ticket_details_loading".localized())
        network.resolve(ticket: ticket) { [weak self] result in
            switch result {
            case .success:
                self?.view.stopLoading {}
                self?.router.pop()
            case .failure(let error):
                self?.view.show(error: error)
            }
        }
    }
}

private extension TicketDetailsInteractor {
    func resolveLocation() {
        guard CLLocationCoordinate2DIsValid(ticket.coordinate) else { return }
        let options = ReverseGeocodeOptions(coordinate: ticket.coordinate)
        options.allowedScopes = [.address]
        _ = Geocoder.shared.geocode(options) { [weak self] (placemarks, _, error) in
            guard let placemark = placemarks?.first else {
                self?.view.show(address: "ticket_details_address_not_recognized".localized(), bottomLine: nil)
                return
            }
            self?.view.show(address: placemark.name, bottomLine: placemark.place?.name)
        }
    }
    
    func save(assigned: Operator) {
        ticket.assigneeId = assigned.operatorId
        guard let fleet = storage.currentFleet else { return }
        storage.save([ticket], for: fleet, update: false, completion: {})
    }
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertToUIApplicationOpenExternalURLOptionsKeyDictionary(_ input: [String: Any]) -> [UIApplication.OpenExternalURLOptionsKey: Any] {
	return Dictionary(uniqueKeysWithValues: input.map { key, value in (UIApplication.OpenExternalURLOptionsKey(rawValue: key), value)})
}
