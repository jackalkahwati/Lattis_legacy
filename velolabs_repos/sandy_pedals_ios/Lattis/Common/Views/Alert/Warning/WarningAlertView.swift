//
//  WarningAlertView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 29/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit

class WarningAlertView: UIView, AlertView {
    @IBOutlet weak var closeButton: UIButton!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var subtitleLabel: UILabel!
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var actionButton: UIButton!
    
    var action: AlertAction? {
        didSet {
            actionButton.add(action: action)
        }
    }
    
    var cancel: AlertAction? {
        didSet {
            cancelButton.add(action: cancel)
        }
    }
    
    class func alert(title: String, subtitle: String? = nil) -> WarningAlertView {
        let alertView = WarningAlertView.nib() as! WarningAlertView
        alertView.titleLabel.text = title
        alertView.subtitleLabel.text = subtitle
        alertView.closeButton.addTarget(alertView, action: #selector(close), for: .touchUpInside)
        alertView.actionButton.addTarget(alertView, action: #selector(performAction), for: .touchUpInside)
        alertView.cancelButton.addTarget(alertView, action: #selector(performCancel), for: .touchUpInside)
        return alertView
    }
    
    @objc private func performAction() {
        action?.action()
        hide()
    }
    
    @objc private func performCancel() {
        cancel?.action()
        hide()
    }
    
    @objc private func close() {
        hide()
    }
}
