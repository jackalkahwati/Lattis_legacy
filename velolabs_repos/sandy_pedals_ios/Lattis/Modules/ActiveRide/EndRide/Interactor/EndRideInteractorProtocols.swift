//
//  EndRideEndRideInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 17/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

protocol EndRideInteractorInput {
    func submit(rating: Int)
    func didMake(picture: UIImage)
    func viewLoaded()
    func openPayments()
    func dismiss()
    func buildMap(size: CGSize)
}

protocol EndRideInteractorOutput: BaseInteractorOutput {
    var interactor: EndRideInteractorInput! {get set}
    func show(trip: Trip)
    func show(snapshot: UIImage)
}
