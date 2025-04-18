//
//  RideSearchViewController+Error.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 25.08.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import UIKit
import OvalAPI

extension RideSearchViewController {
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        if error.isHTTP(code: 409) {
            let alert = AlertController(title: "general_error_title".localized(), body: "preauthorization_warning".localized())
            present(alert, animated: true)
        } else {
            super.handle(error, from: viewController, retryHandler: retryHandler)
        }
    }
}
