//
//  OnboardingChoosePage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

protocol OnboardingChoosePageDelegate: class {
    func onboardOwn()
    func onboardShared()
}

class OnboardingChoosePage: ViewController, LockOnboardingPage {
    weak var delegate: OnboardingChoosePageDelegate?
    
    fileprivate let titleLabel = UILabel()
    fileprivate let subtitleLabel = UILabel()
    fileprivate let ownButton = UIButton(type: .custom)
    fileprivate let sharedButton = UIButton(type: .custom)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        configureUI()
    }
    
    fileprivate func configureUI() {
        view.backgroundColor = .white
        view.addSubview(titleLabel)
        view.addSubview(ownButton)
        view.addSubview(subtitleLabel)
        view.addSubview(sharedButton)
        
        configureLabel(titleLabel)
        configureLabel(subtitleLabel)
        smallPositiveStyle(ownButton)
        smallNegativeStyle(sharedButton)
        ownButton.setTitle("addlock_home_button_onboard".localized().lowercased().capitalized, for: .normal)
        sharedButton.setTitle("addlock_home_button_share".localized().lowercased().capitalized, for: .normal)
        titleLabel.text = "addlock_home_description_2".localized()
        subtitleLabel.text = "addlock_home_description_3".localized()
        
        let margin: CGFloat = 20
        constrain(titleLabel, subtitleLabel, ownButton, sharedButton, view) { title, subtitle, own, shared, view in
            title.top == view.safeAreaLayoutGuide.top + margin
            title.left == view.left + margin
            title.right == view.right - margin
            
            own.top == title.bottom + margin
            own.centerX == view.centerX
            
            subtitle.left == title.left
            subtitle.right == title.right
            subtitle.top == own.bottom + margin*4
            
            shared.top == subtitle.bottom + margin
            shared.centerX == view.centerX
        }
        
        ownButton.addTarget(self, action: #selector(ownAction(_:)), for: .touchUpInside)
        sharedButton.addTarget(self, action: #selector(sharedAction(_:)), for: .touchUpInside)
    }
    
    func set(delegate: Any?) {
        self.delegate = delegate as? OnboardingChoosePageDelegate
    }
    
    @objc fileprivate func ownAction(_ sender: Any) {
        delegate?.onboardOwn()
    }
    
    @objc fileprivate func sharedAction(_ sender: Any) {
        delegate?.onboardShared()
    }
}

fileprivate let configureLabel: (UILabel) -> () = { label in
    label.font = .elTitleLight
    label.textAlignment = .center
    label.textColor = .black
    label.numberOfLines = 0
}
