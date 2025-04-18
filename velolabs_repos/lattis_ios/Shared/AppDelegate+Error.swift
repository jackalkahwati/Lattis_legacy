//
//  AppDelegate+Error.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 31.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import LattisCore
import OvalAPI
import OvalBackend

extension AppDelegate {
    
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        var title = "general_error_title".localized()
        var message = "general_error_message".localized()
        if let e = error as? AlertPresentable {
            if let t = e.title { title = t }
            if let m = e.message { message = m }
        }
        if let e = error as? ServerError {
            message = "\(e.message)"
        }
        
        // Use failure response from QR code scanning
        if let e = error as? Failure {
            message = "\(e.message)"
        }
        
        let alert = AlertController(title: title, body: message, handler: retryHandler)
        viewController.stopLoading {
            viewController.present(alert, animated: true, completion: nil)
        }
    }
}
