//
//  LockControl.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Cartography
import JTMaterialSpinner

enum LockState {
    case locked
    case unlocked
    case progress
}

class LockSwitch: UIControl {
    var status: LockState = .locked {
        didSet {
            UIView.animate(withDuration: 0.3) {
                switch self.status {
                case .locked:
                    self.lockedImage.alpha = 1
                    self.unlockedImage.alpha = 0.6
                    self.topLayout.priority = .defaultHigh
                    self.bottomLayout.priority = .defaultLow
                    self.stopLoading()
                case .unlocked:
                    self.lockedImage.alpha = 0.6
                    self.unlockedImage.alpha = 1
                    self.topLayout.priority = .defaultLow
                    self.bottomLayout.priority = .defaultHigh
                    self.stopLoading()
                case .progress:
                    self.startLoading()
                }
                self.layoutIfNeeded()
            }
        }
    }
    let focusView = UIView()
    fileprivate let spinner = JTMaterialSpinner()
    fileprivate let lockedImage = UIImageView(image: UIImage(named: "icon_lock_locked"))
    fileprivate let unlockedImage = UIImageView(image: UIImage(named: "icon_lock_unlocked"))
    fileprivate var topLayout: NSLayoutConstraint!
    fileprivate var bottomLayout: NSLayoutConstraint!
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        backgroundColor = UIColor(white: 0.7, alpha: 0.3)
        
        addSubview(focusView)
        focusView.isUserInteractionEnabled = false
        lockedImage.isUserInteractionEnabled = false
        unlockedImage.isUserInteractionEnabled = false
        focusView.backgroundColor = .neonBlue
        layer.cornerRadius = 22
        focusView.layer.cornerRadius = 20
        focusView.addShadow()
        
        addSubview(lockedImage)
        addSubview(unlockedImage)
        lockedImage.contentMode = .scaleAspectFit
        unlockedImage.contentMode = .scaleAspectFit
        unlockedImage.alpha = 0.6
        
        focusView.addSubview(spinner)
        spinner.circleLayer.strokeColor = UIColor.white.cgColor
        spinner.circleLayer.lineWidth = 2
        
        constrain(lockedImage, unlockedImage, focusView, spinner, self) { locked, unlocked, focus, spin, view in
            locked.height == 20
            locked.width == 20
            unlocked.height == 20
            unlocked.width == 20
            
            locked.centerY == view.centerY
            unlocked.centerY == view.centerY
            
            locked.centerX == view.left + 21
            unlocked.centerX == view.right - 21
            
            view.height == 44
            view.width == 88
            
            self.topLayout = focus.left == view.left + 2 ~ .defaultHigh
            self.bottomLayout = focus.right == view.right - 2 ~ .defaultLow
            focus.centerY == view.centerY
            focus.width == 40
            focus.height == 40
            
            spin.edges == focus.edges.inseted(by: 2)
        }
    }
    
    func startLoading() {
        spinner.beginRefreshing()
        isUserInteractionEnabled = false
    }
    
    func stopLoading() {
        spinner.endRefreshing()
        isUserInteractionEnabled = true
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
