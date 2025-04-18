//
//  RideViewController+Payment.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 13.05.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import UIKit
import Stripe

extension RideViewController: STPAuthenticationContext {
    func authenticationPresentingViewController() -> UIViewController {
        mapController?.activity ?? self
    }
}

extension EndRideViewController: STPAuthenticationContext {
    func authenticationPresentingViewController() -> UIViewController {
        self
    }
}
