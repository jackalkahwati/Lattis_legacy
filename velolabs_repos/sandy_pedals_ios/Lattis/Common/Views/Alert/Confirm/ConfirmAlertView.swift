//
//  ConfirmAlertView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 29/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit

class ConfirmAlertView: UIView, AlertView {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var subtitleLabel: UILabel!
    @IBOutlet weak var actionButton: UIButton!
    
    var action: AlertAction? {
        didSet {
            actionButton.add(action: action)
        }
    }
    
    class func alert(title: String, subtitle: String? = nil, hint: String? = nil) -> ConfirmAlertView {
        let alertView = ConfirmAlertView.nib() as! ConfirmAlertView
        alertView.titleLabel.text = title
        alertView.subtitleLabel.text = alertView.subtitle(text: subtitle, hint: hint)
        alertView.actionButton.addTarget(alertView, action: #selector(performAction), for: .touchUpInside)
        return alertView
    }
    
    private func subtitle(text: String?, hint: String?) -> String? {
        if let txt = text, let hnt = hint {
            return "\(txt)\n\n\(hnt)"
        }
        return text ?? hint
    }
    
    @objc private func performAction() {
        action?.action()
        hide()
    }
    
}
