//
//  RideHistoryDetailsRideHistoryDetailsInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 18/08/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

protocol RideHistoryDetailsInteractorInput {
    func viewDidLoad()
    func requestSnapshot(size: CGSize)
}

protocol RideHistoryDetailsInteractorOutput: BaseInteractorOutput {
    func show(trip: Trip, snapshot: UIImage?)
    func present(snapshot: UIImage)
}
