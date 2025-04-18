//
//  AlertAction.swift
//  Lattis
//
//  Created by Ravil Khusainov on 25/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit

struct AlertAction {
    let title: String
    let action: () -> ()
}

extension UIButton {
    func add(action: AlertAction?) {
        if let act = action {
            setTitle(act.title, for: .normal)
        }
        isHidden = action == nil
    }
}


protocol AlertView {
    func show(parrent: UIView?)
    func hide(completion: (() -> ())?)
}

extension AlertView where Self: UIView {
    func show(parrent: UIView? = nil) {
        guard let parentView = parrent ?? UIApplication.shared.keyWindow else { return }
        alpha = 0
        parentView.addSubview(self)
        translatesAutoresizingMaskIntoConstraints = false
        constrainEdges(to: parentView)
        UIView.animate(withDuration: .defaultAnimation) {
            self.alpha = 1
        }
    }
    
    func hide(completion: (() -> ())? = nil) {
        UIView.animate(withDuration: .defaultAnimation, animations: { 
            self.alpha = 0
        }, completion: { _ in
            completion?()
            self.removeFromSuperview()
        })
    }
}
