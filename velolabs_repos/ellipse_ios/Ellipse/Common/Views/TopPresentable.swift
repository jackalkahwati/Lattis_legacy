//
//  TopPresentable.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/24/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

protocol TopPresentable {
    func show()
    func show(in parrent: UIView)
    func hide(completion: (() -> ())?)
}


extension TopPresentable where Self: UIView {
    func show() {
        guard let view = UIApplication.shared.keyWindow else { return }
        show(in: view)
    }
    
    func show(in parent: UIView) {
        alpha = 0
        parent.addSubview(self)
        translatesAutoresizingMaskIntoConstraints = false
        constrain(self) { (view) in
            view.edges == view.superview!.edges
        }
        UIView.animate(withDuration: 0.35) {
            self.alpha = 1
        }
    }
    
    func hide(completion: (() -> ())? = nil) {
        UIView.animate(withDuration: 0.35, animations: {
            self.alpha = 0
        }, completion: { _ in
            completion?()
            self.removeFromSuperview()
        })
    }
}
