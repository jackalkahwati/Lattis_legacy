//
//  RideViewController+Error.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 31.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import OvalAPI
import EllipseLock

extension EllipseError: AlertPresentable {
    public var title: String? {
        switch self {
        case .timeout:
            return "label_note".localized()
        default:
            return "general_error_title".localized()
        }
    }
    
    public var message: String? {
        switch self {
        case .timeout:
            return "bike_out_of_range_connection_error".localized()
        default:
            return "general_error_message".localized()
        }
    }
}

extension RideViewController {
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        if let err = error as? SessionError {
            switch err.code {
            case .conflict:
                let alert = AlertController(title: "general_error_title".localized(), body: "credit_card_required".localized())
                alert.actions = [
                    .plain(title: "update_payment_details".localized()) { [unowned self] in
                        self.openPaymentMethods()
                    },
                    .plain(title: "damage_report_success_continue_ride".localized())
                ]
                return viewController.present(alert, animated: true, completion: nil)
            default:
                break
            }
        }
        super.handle(error, from: viewController, retryHandler: retryHandler)
    }
}
