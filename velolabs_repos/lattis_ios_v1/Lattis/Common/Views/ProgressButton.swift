//
//  ProgressButton.swift
//  Lattis
//
//  Created by Ravil Khusainov on 8/30/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

@IBDesignable
class ProgressButton: UIControl {
    @IBInspectable var title: String? {
        set {
            titleLabel.text = newValue?.localized()
        }
        get {
            return titleLabel.text
        }
    }
    let titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.textAlignment = .center
        label.font = UIFont.systemFont(ofSize: 14)
        return label
    }()
    var progress: Float = 0 {
        didSet {
            updateProgressFrame()
        }
    }
    
    fileprivate let progressView = UIView()
    fileprivate var animating = false
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        progressView.backgroundColor = .lsTurquoiseBlue
        progressView.isUserInteractionEnabled = false
        addSubview(progressView)
        progress = 0
        
        titleLabel.isUserInteractionEnabled = false
        addSubview(titleLabel)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = bounds
        if animating == false {
            updateProgressFrame()
        }
    }
    
    func beginAnimation() {
        animating = true
        let frame: CGRect = {
            var frame = bounds
            frame.size.width *= 0.8
            frame.origin.x = -frame.width
            return frame
        }()
        progressView.frame = frame
        UIView.animate(withDuration: 2, delay: 0, options: [.repeat], animations: {
            self.progressView.frame = {
                var ff = frame
                ff.origin.x = self.bounds.width
                return ff
            }()
        }, completion: { _ in
            
//            self.progressView.frame = frame
        })
    }
    
    func endAnimation(progress: Float = 1) {
        progressView.layer.removeAllAnimations()
        UIView.animate(withDuration: .defaultAnimation, delay: 0, options: .curveEaseIn, animations: {
            self.progress = progress
        }, completion: { _ in
            self.animating = false
        })
    }
    
    fileprivate func updateProgressFrame() {
        progressView.frame = {
            var frame = bounds
            frame.size.width *= CGFloat(progress)
            return frame
        }()
    }
}
