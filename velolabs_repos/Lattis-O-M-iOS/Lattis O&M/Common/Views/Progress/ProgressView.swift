//
//  ProgressView.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/15/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import UICircularProgressRing
import Cartography

class ProgressView: UIView {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var ringView: UICircularProgressRing!
    
    var progress: Double {
        set {
            ringView.value = CGFloat(newValue)*100
        }
        get {
            return Double(ringView.value)/100
        }
    }
    
    static func show(title: String? = nil) -> ProgressView {
        let view = ProgressView.nib() as! ProgressView
        if title != nil {
            view.titleLabel.text = title
        }
        view.present()
        return view
    }
    
    private func present() {
        guard let parent = UIApplication.shared.keyWindow else { return }

        alpha = 0
        parent.addSubview(self)
        translatesAutoresizingMaskIntoConstraints = false
        constrain(self) { (view) in
            view.edges == view.superview!.edges
        }
        
        UIView.animate(withDuration: 0.35, animations: {
            self.alpha = 1
        })
    }
    
    func hide(completion:@escaping () -> () = {}) {
        UIView.animate(withDuration: 0.35, animations: {
            self.alpha = 0
        }) { (_) in
            self.removeFromSuperview()
            completion()
        }
    }
}
