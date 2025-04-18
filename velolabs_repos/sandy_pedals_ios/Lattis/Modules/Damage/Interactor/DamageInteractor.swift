//
//  DamageDamageInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import Oval

class DamageInteractor {
    weak var view: DamageInteractorOutput!
    var router: DamageRouter!
    var bike: Bike!
    fileprivate var category: DamageReport.Category?
    fileprivate var picture: UIImage?
    fileprivate let network: MaintenanceNetwork

    init(network: MaintenanceNetwork = Session.shared) {
        self.network = network
    }
}

extension DamageInteractor: DamageInteractorInput {
    func didMake(picture: UIImage) {
        self.picture = picture
        validate()
    }
    
    func didSelect(category: DamageReport.Category) {
        self.category = category
        validate()
    }
    
    func submit(with note: String?) {
        guard let picture = picture, let data = picture.jpegData(compressionQuality: 0.5), let category = category else { return }
        view.startLoading(with: "damage_report_submit_loading".localized())
        let tripId: Int?
        switch AppRouter.shared.currentState {
        case .trip(let id):
            tripId = id
        case .booking(let id, _, _):
            tripId = id
        default:
            tripId = nil
        }
        let report = DamageReport(bike: bike, category: category, picture: data, notes: note, tripId: tripId)
        network.submit(report: report) { [weak self] result in
            switch result {
            case .success:
                self?.view.stopLoading() { self?.view.showSuccess() }
                
            case .failure(let error):
                self?.view.show(error: error, file: #file, line: #line)
                
                Analytics.report(error)
            }
        }
    }
    
    func cancelBooking() {
        router.close()
        AppRouter.shared.damage = true
        AppRouter.shared.cancelBooking?(true)
    }
    
    func endRide() {
        router.close()
        AppRouter.shared.currentState = .none
        AppRouter.shared.damage = true
        AppRouter.shared.endTrip(false)
    }
}

private extension DamageInteractor {
    func validate() {
        view.setSubmition(enabled: category != nil && picture != nil)
    }
}
