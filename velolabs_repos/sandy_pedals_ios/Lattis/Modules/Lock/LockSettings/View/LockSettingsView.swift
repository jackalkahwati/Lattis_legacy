//
//  LockSettingsView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 17/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class LockSettingsView: UIView {
    @IBOutlet weak var hintCloseButton: ShadowButton!
    @IBOutlet weak var slider: LockSlider!
    @IBOutlet weak var hintContainer: UIView!
    @IBOutlet weak var hintButton: ShadowButton!
    @IBOutlet weak var hintButtonLayout: NSLayoutConstraint!
    @IBOutlet weak var unlockHintLabel: UILabel!

    func switchHint() {
        isHintShown = !isHintShown
    }
    
    var lockState: LockSlider.LockState {
        set {
            slider.lockState = newValue
            UIView.animate(withDuration: .defaultAnimation) { 
                self.unlockHintLabel.alpha = self.slider.isLocked ? 0 : 1
            }
        }
        get {
            return slider.lockState
        }
    }
    
    private var isHintShown: Bool = false {
        didSet {
            if isHintShown {
                showHint()
            } else {
                hideHint()
            }
        }
    }
    
    private func showHint() {
        hintContainer.alpha = 0
        hintContainer.isHidden = false
        hintCloseButton.alpha = 0
        hintCloseButton.isHidden = false
        UIView.animate(withDuration: 0.3, delay: 0, options: .curveEaseIn, animations: { 
            self.hintContainer.alpha = 1
            self.hintButtonLayout.constant = 147
            self.layoutIfNeeded()
            self.hintButton.alpha = 0
            self.hintCloseButton.alpha = 1
        }, completion: { _ in
            self.hintButton.isHidden = true
        })
    }
    
    private func hideHint() {
        hintButton.alpha = 0
        hintButton.isHidden = false
        UIView.animate(withDuration: 0.3, delay: 0, options: .curveEaseIn, animations: {
            self.hintContainer.alpha = 0
            self.hintButtonLayout.constant = 83
            self.layoutIfNeeded()
            self.hintButton.alpha = 1
            self.hintCloseButton.alpha = 0
        }, completion: { _ in
            self.hintContainer.isHidden = true
            self.hintCloseButton.isHidden = true
        })
    }
}
