//
//  LogInBaseViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 21/12/2018.
//  Copyright © 2018 Lattis. All rights reserved.
//

import UIKit
import TPKeyboardAvoiding
import Cartography
import PhoneNumberKit

class LogInBaseViewController: ViewController {
    
    var interactor: LogInInteractorInput!
    
    let facebookButton = UIButton(type: .custom)
    let scrollView = TPKeyboardAvoidingScrollView()
    let orLabel = UILabel()
    let phoneNumberKit = PhoneNumberKit()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .white
        navigationItem.rightBarButtonItem = UIBarButtonItem(image: UIImage(named: "button_close_big"), style: .plain, target: self, action: #selector(closeAction))
        
        configureButtons()
        configureScrollview()
    }
    
    open func configureButtons() {
        
        view.addSubview(facebookButton)
        view.addSubview(orLabel)
        facebookStyle(facebookButton)
        orLabel.text = "label_or".localized()
        orLabel.textColor = .black
        orLabel.textAlignment = .center
        orLabel.font = .elTitle
        let margin: CGFloat = 20
        constrain(facebookButton, orLabel, view) { facebook, label, superview in
            facebook.left == superview.left + margin ~ .defaultLow
            facebook.right == superview.right - margin ~ .defaultLow
            facebook.centerX == superview.centerX
            facebook.bottom == superview.safeAreaLayoutGuide.bottom - margin
            
            label.left == facebook.left
            label.right == facebook.right
            label.bottom == facebook.top - margin
        }
        
        facebookButton.addTarget(self, action: #selector(loginWithFacebook), for: .touchUpInside)
    }
    
    @objc fileprivate func loginWithFacebook() {
        interactor.facebookLogin()
    }
    
    open func configureScrollview() {}
    
    @objc fileprivate func closeAction() {
        view.endEditing(true)
        dismiss(animated: true, completion: nil)
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .default
    }
}

extension LogInBaseViewController: LogInInteractorOutput {
    
}
