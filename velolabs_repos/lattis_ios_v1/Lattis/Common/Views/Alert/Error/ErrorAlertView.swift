//
//  ErrorAlertView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 28/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit

class ErrorAlertView: UIView, AlertView {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var subtitleLabel: UILabel!
    @IBOutlet weak var actionButton: UIButton!
    
    var action: () -> () = {}

    class func alert(title: String, subtitle: String? = nil, hint: String? = nil, button: String? = nil) -> ErrorAlertView {
        let alertView = ErrorAlertView.nib() as! ErrorAlertView
        alertView.titleLabel.text = title
        alertView.subtitleLabel.text = alertView.subtitle(text: subtitle, hint: hint)
        let btnText = button ?? "general_btn_ok".localized().uppercased()
        alertView.actionButton.setTitle(btnText, for: .normal)
        return alertView
    }
    
    private func subtitle(text: String?, hint: String?) -> String? {
        if let txt = text, let hnt = hint {
            return "\(txt)\n\n\(hnt)"
        }
        return text ?? hint
    }
    
    @IBAction func close(_ sender: Any) {
        hide(completion: action)
    }
}
