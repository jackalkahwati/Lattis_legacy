//
//  LockControl.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Cartography
import JTMaterialSpinner

class SecuritySwitch: UIControl {
    var security: Device.Security = .undefined {
        didSet {
            UIView.animate(withDuration: 0.3) {
                switch self.security {
                case .locked:
                    self.lockedImage.alpha = 1
                    self.unlockedImage.alpha = 0
                    self.lockedLayout.priority = .defaultHigh
                    self.unlockedLayout.priority = .defaultLow
                    self.stopLoading()
                    self.backgroundColor = .red
                    self.unlockedImage.tintColor = .black
                    self.lockedImage.tintColor = .red
                case .unlocked:
                    self.lockedImage.alpha = 0
                    self.unlockedImage.alpha = 1
                    self.lockedLayout.priority = .defaultLow
                    self.unlockedLayout.priority = .defaultHigh
                    self.stopLoading()
                    self.backgroundColor = .black
                    self.unlockedImage.tintColor = .black
                    self.lockedImage.tintColor = .red
                case .undefined:
                    self.backgroundColor = .lightGray
                    self.unlockedImage.tintColor = .lightGray
                    self.lockedImage.tintColor = .lightGray
                    self.stopLoading()
                case .progress:
                    self.startLoading()
                }
                self.spinner.circleLayer.strokeColor = self.backgroundColor!.cgColor
                self.layoutIfNeeded()
            }
        }
    }
    let focusView = UIView()
    fileprivate let spinner = JTMaterialSpinner()
    fileprivate let lockedImage = UIImageView(image: .named("icon_lock_locked"))
    fileprivate let unlockedImage = UIImageView(image: .named("icon_lock_unlocked"))
    fileprivate var unlockedLayout: NSLayoutConstraint!
    fileprivate var lockedLayout: NSLayoutConstraint!
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        backgroundColor = .lightGray
        
        addSubview(focusView)
        focusView.isUserInteractionEnabled = false
        lockedImage.isUserInteractionEnabled = false
        unlockedImage.isUserInteractionEnabled = false
        focusView.backgroundColor = .white
        layer.cornerRadius = 22
        focusView.layer.cornerRadius = 19
        focusView.addShadow()
        
        addSubview(lockedImage)
        addSubview(unlockedImage)
        unlockedImage.tintColor = .lightGray
        lockedImage.tintColor = .lightGray
        lockedImage.contentMode = .scaleAspectFit
        unlockedImage.contentMode = .scaleAspectFit
        lockedImage.alpha = 0
        
        focusView.addSubview(spinner)
        spinner.circleLayer.lineWidth = 2
        
        constrain(lockedImage, unlockedImage, focusView, spinner, self) { locked, unlocked, focus, spin, view in
            locked.height == 20
            locked.width == 20
            unlocked.height == 20
            unlocked.width == 20
            
            locked.centerY == view.centerY
            unlocked.centerY == view.centerY
            
            locked.centerX == view.right - self.layer.cornerRadius
            unlocked.centerX == view.left + self.layer.cornerRadius
            
            view.height == 44
            view.width == 88
            
            self.unlockedLayout = focus.left == view.left + 4 ~ .defaultHigh
            self.lockedLayout = focus.right == view.right - 4 ~ .defaultLow
            focus.centerY == view.centerY
            focus.width == 38
            focus.height == 38
            
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
