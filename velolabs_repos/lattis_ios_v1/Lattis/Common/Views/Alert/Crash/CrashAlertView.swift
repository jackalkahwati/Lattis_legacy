//
//  CrashAlertView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 28/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit
import SwiftyTimer
import KDCircularProgress

class CrashAlertView: UIView, AlertView {
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var notifyButton: UIButton!
    @IBOutlet weak var countdownLabel: UILabel!
    @IBOutlet weak var progressView: KDCircularProgress!
    var cancel: (() -> ())?
    var notify: (() -> ())?
    
    class func alert() -> CrashAlertView {
        let alertView = CrashAlertView.nib() as! CrashAlertView
        alertView.notifyButton.addTarget(alertView, action: #selector(performNotify), for: .touchUpInside)
        alertView.cancelButton.addTarget(alertView, action: #selector(performCancel), for: .touchUpInside)

        return alertView
    }
    
    func start(duration: TimeInterval) {
        var countdown = duration
        self.countdownLabel.text = String(format: "%02d", Int(duration))
        self.progressView.progress = 1
        Timer.every(1.second) { [weak self] in
            guard let `self` = self else {
                return
            }
            self.countdownLabel.text = String(format: "%02d", Int(countdown))
            self.progressView.progress = countdown/duration
            countdown -= 1
            if countdown < 0 {
                self.performNotify()
                //invalidate
            }
        }
    }
    
    @objc private func performNotify() {
        notify?()
        hide()
    }
    
    @objc private func performCancel() {
        cancel?()
        hide()
    }
}
