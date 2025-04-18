//
//  CrashAlertView.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/21/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Atributika

class CrashAlertView: AlertView {
    
    fileprivate let countdownLabel = UILabel()
    fileprivate var left: Int = 30 // Secounds for countdown
    fileprivate var timer: Timer?
    fileprivate var action: () -> () = {}
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        countdownLabel.textAlignment = .center
        countdownLabel.font = .elRegular
        countdownLabel.textColor = .elSlateGreyTwo
        countdownLabel.numberOfLines = 0
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    class func alert(completion: @escaping () -> ()) -> CrashAlertView {
        let view = CrashAlertView()
        view.configure(view: view.countdownLabel, actions: [
            .init(title: "alert_my_contacts".localized().lowercased().capitalized, handler: { [weak view] (_) in
                view?.timer?.invalidate()
                completion()
            }),
            .init(title: "cancel_i_m_ok".localized().lowercased().capitalized, style: .cancel)
            ])
        view.titleLabel.text = "action_label_crash_description2".localized()
        view.updateCountdownLabel()
        view.action = completion
        view.startTimer()
        return view
    }
    
    fileprivate func updateCountdownLabel() {
        let all = Style.font(.elRegular).foregroundColor(.elSlateGreyTwo)
        let b = Style("b").font(.elTitle).foregroundColor(.black)
        countdownLabel.attributedText = "crash_alert_message".localizedFormat(String(left)).style(tags: b).styleAll(all).attributedString
    }
    
    fileprivate func startTimer() {
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true, block: { [weak self] (_) in
            self?.updateCountdownLabel()
            self?.left -= 1
            if self?.left == -1 {
                self?.timer?.invalidate()
                self?.hide(completion: {
                    self?.action()
                })
            }
        })
    }
    
    deinit {
        timer?.invalidate()
    }
}
