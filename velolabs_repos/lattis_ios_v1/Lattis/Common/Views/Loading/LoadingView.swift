//
//  LoadingView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 25/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit
import JTMaterialSpinner

class LoadingView: UIView {
    @IBOutlet weak var spinner: LoadingIndicator!
    @IBOutlet weak var titleLabel: UILabel!
    let blurView = UIVisualEffectView(effect: UIBlurEffect(style: .dark))
    static func show(title: String? = nil, animated: Bool = true) -> LoadingView {
        let view = LoadingView.nib() as! LoadingView
        if title != nil {
            view.titleLabel.text = title
        }
        view.present(animated: animated)
        return view
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
//        spinner.circleLayer.lineWidth = 3
//        spinner.circleLayer.strokeColor = UIColor.white.cgColor
//        spinner.animationDuration = 1.5
        spinner.beginRefreshing()
    }
    
    private func present(animated: Bool) {
    
        guard let parent = UIApplication.shared.keyWindow else { return }
        
        blurView.effect = nil
        parent.addSubview(blurView)
        blurView.translatesAutoresizingMaskIntoConstraints = false
        blurView.constrainEdges(to: parent)
        
        alpha = 0
        parent.addSubview(self)
        translatesAutoresizingMaskIntoConstraints = false
        self.constrainEdges(to: parent)
        
        func action() {
            self.alpha = 1
            self.blurView.effect = UIBlurEffect(style: .dark)
        }
        
        if animated {
            UIView.animate(withDuration: .defaultAnimation, animations: action)
        } else {
            action()
        }
    }
    
    func hide(completion:(() -> ())?) {
        UIView.animate(withDuration: .defaultAnimation, animations: { 
            self.alpha = 0
            self.blurView.effect = nil
        }) { (_) in
            self.blurView.removeFromSuperview()
            self.removeFromSuperview()
            completion?()
        }
    }
}
