//
//  TheftAlertView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 28/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit

class TheftAlertView: UIView, AlertView {
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var confirmButton: UIButton!
    var confirm: AlertAction?
    var cancel: AlertAction?
    private let height: CGFloat = 186
    private var topConstraint: NSLayoutConstraint?
    
    class func alert() -> TheftAlertView {
        let alertView = TheftAlertView.nib() as! TheftAlertView
        alertView.confirmButton.addTarget(alertView, action: #selector(performConfirm), for: .touchUpInside)
        alertView.cancelButton.addTarget(alertView, action: #selector(perfornCancel), for: .touchUpInside)
        return alertView
    }
    
    @objc private func perfornCancel() {
        cancel?.action()
        hide()
    }
    
    @objc private func performConfirm() {
        confirm?.action()
    }
    
    func show(parrent: UIView? = nil) {
        guard let parentView = parrent ?? UIApplication.shared.keyWindow else { return }
        parentView.addSubview(self)
        translatesAutoresizingMaskIntoConstraints = false
        topConstraint = constrainEqual(.top, to: parentView, .top, constant: -height)
        _ = constrainEqual(.leading, to: parentView, .leading)
        _ = constrainEqual(.trailing, to: parentView, .trailing)
        _ = constrainEqual(.height, to: nil, .height, constant: height)
        parentView.layoutIfNeeded()
        UIView.animate(withDuration: .defaultAnimation) {
            self.topConstraint?.constant = 0
            parentView.layoutIfNeeded()
        }
    }
    
    func hide(completion: (() -> ())? = nil) {
        UIView.animate(withDuration: .defaultAnimation, animations: {
            self.topConstraint?.constant = -self.height
            self.superview?.layoutIfNeeded()
        }) { (_) in
            self.removeFromSuperview()
        }
    }
    
}
