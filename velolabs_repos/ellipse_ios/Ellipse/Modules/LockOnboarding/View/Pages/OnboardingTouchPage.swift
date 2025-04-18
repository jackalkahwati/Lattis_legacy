//
//  OnboardingTouchPage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

protocol OnboardingTouchPageDelegate: class {
    func next()
}

class OnboardingTouchPage: ViewController, LockOnboardingPage {
    weak var delegate: OnboardingTouchPageDelegate?
    
    fileprivate let titleLabel = UILabel()
    fileprivate let imageView = UIImageView(image: UIImage(named: "onboarding_blink_guide"))
    fileprivate let acceptButton = UIButton(type: .custom)
    
    func set(delegate: Any?) {
        self.delegate = delegate as? OnboardingTouchPageDelegate
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .white
        view.addSubview(titleLabel)
        view.addSubview(imageView)
        view.addSubview(acceptButton)
        
        smallPositiveStyle(acceptButton)
        acceptButton.setTitle("ok_touch_pad_is_on".localized().lowercased().capitalized, for: .normal)
        
        configureLabel(titleLabel)
        titleLabel.text = "lock_onboarding_touch_text".localized()
        titleLabel.setContentHuggingPriority(.defaultHigh, for: .vertical)
        
        imageView.contentMode = .center
        
        let margin: CGFloat = 20
        constrain(titleLabel, imageView, acceptButton, view) { title, image, accept, view in
            title.left == view.left + margin
            title.right == view.right - margin
            title.top == view.safeAreaLayoutGuide.top + margin
            
            accept.bottom == view.safeAreaLayoutGuide.bottom - margin
            accept.centerX == view.centerX
            
            image.left == view.left
            image.right == view.right
            image.top == title.bottom + margin
            image.bottom == accept.top - margin
        }
        
        acceptButton.addTarget(self, action: #selector(okAction(_:)), for: .touchUpInside)
    }
    
    @objc fileprivate func okAction(_ sender: Any) {
        delegate?.next()
    }
}

fileprivate let configureLabel: (UILabel) -> () = { label in
    label.font = .elTitleLight
    label.textAlignment = .center
    label.textColor = .black
    label.numberOfLines = 0
}
