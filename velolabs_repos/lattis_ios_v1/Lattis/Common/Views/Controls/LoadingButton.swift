//
//  LoadingButton.swift
//  Lattis
//
//  Created by Ravil Khusainov on 23/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import JTMaterialSpinner

class LoadingButton: ShadowButton {
    private let spinner = JTMaterialSpinner()
    
    override func setUp() {
        super.setUp()
        
        spinner.circleLayer.lineWidth = 3
        spinner.circleLayer.strokeColor = tintColor.cgColor
        spinner.animationDuration = 1.5
        spinner.isHidden = true
        addSubview(spinner)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        spinner.frame = bounds.insetBy(dx: 6, dy: 6)
    }
    
    func startLoading() {
        isUserInteractionEnabled = false
        spinner.isHidden = false
        spinner.alpha = 0
        spinner.beginRefreshing()
        UIView.animate(withDuration: .defaultAnimation) { 
            self.spinner.alpha = 1
            self.imageView.alpha = 0
            self.titleLabel.alpha = 0
        }
    }
    
    func stopLoading() {
        spinner.endRefreshing()
        UIView.animate(withDuration: .defaultAnimation, animations: { 
            self.spinner.alpha = 0
            self.imageView.alpha = 1
            self.titleLabel.alpha = 1
        }, completion: { _ in
            self.spinner.isHidden = true
            self.isUserInteractionEnabled = true
        })
    }
}
