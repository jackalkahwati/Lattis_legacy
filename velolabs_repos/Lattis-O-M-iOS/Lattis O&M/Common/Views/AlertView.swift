//
//  AlertView.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 03/05/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

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
