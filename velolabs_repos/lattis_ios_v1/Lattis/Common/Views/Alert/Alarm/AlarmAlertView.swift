//
//  AlarmAlertView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 28/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit

class AlarmAlertView: UIView, AlertView {
    @IBOutlet weak var checkButton: UIButton!
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var confirmButton: UIButton!

    var cancel: (() -> ())?
    var confirm: (() -> ())?
    var check: (() -> ())?
    
    class func alert() -> AlarmAlertView {
        let alertView = AlarmAlertView.nib() as! AlarmAlertView
        alertView.confirmButton.addTarget(alertView, action: #selector(performConfirm), for: .touchUpInside)
        alertView.cancelButton.addTarget(alertView, action: #selector(perfornCancel), for: .touchUpInside)
        alertView.checkButton.addTarget(alertView, action: #selector(performCheck), for: .touchUpInside)
        return alertView
    }

    
    @objc private func perfornCancel() {
        cancel?()
        hide()
    }
    
    @objc private func performConfirm() {
        confirm?()
    }
    
    @objc private func performCheck() {
        check?()
    }
}
