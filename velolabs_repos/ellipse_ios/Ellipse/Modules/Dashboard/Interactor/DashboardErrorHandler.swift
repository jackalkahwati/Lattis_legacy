//
//  DashboardErrorHandler.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/20/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Oval
import LattisSDK

final class DashboardErrorHandler: ErrorHandler {
    override func handleBLE(error: EllipseError) {
        switch error {
        case .accessDenided:
            view.show(warning: "ellipse_belongs_to_another_user_warning".localized(), title: "lock_belongs_another_user_alert".localized())
        default:
            view.show(error: error)
        }
    }
}
