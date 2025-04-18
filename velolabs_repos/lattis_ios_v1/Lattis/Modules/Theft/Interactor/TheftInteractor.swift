//
//  TheftTheftInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 04/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Oval

class TheftInteractor {
    weak var view: TheftInteractorOutput!
    var router: TheftRouter!
    var bike: Bike!
    
    fileprivate let network: MaintenanceNetwork
    init(network: MaintenanceNetwork = Session.shared) {
        self.network = network
    }
}

extension TheftInteractor: TheftInteractorInput {
    func submit() {
        view.startLoading(with: "report_theft_loading".localized())
        let tripId: Int?
        switch AppRouter.shared.currentState {
        case .trip(let id):
            tripId = id
        default:
            tripId = nil
        }
        let theft = Theft(bikeId: bike.bikeId, tripId: tripId)
        network.report(theft: theft) { [weak self] (result) in
            switch result {
            case .success:
                self?.view.showSuccess()
            case .failure(let error):
                self?.view.show(error: error, file: #file, line: #line)
            }
        }
    }
}

