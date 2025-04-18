//
//  OnboardingSharePage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/17/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

protocol OnboardingSharePageDelegate: class {
    func connect(code: String)
}

class OnboardingSharePage: ViewController, LockOnboardingPage {
    func set(delegate: Any?) {
        self.delegate = delegate as? OnboardingSharePageDelegate
    }
    weak var delegate: OnboardingSharePageDelegate?
    
    fileprivate let titleLabel = UILabel()
    fileprivate let connectButton = ValidationButton()
    fileprivate let codeInput = TitledInputControl(title: "hint_enter_code".localized(), hint: "code_hint".localized())
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .white
        view.addSubview(titleLabel)
        view.addSubview(codeInput)
        view.addSubview(connectButton)
        smallPositiveStyle(connectButton)
        configureLabel(titleLabel)
        titleLabel.text = "share_submit_sms_hint".localized()
        connectButton.setTitle("share_connect_now".localized(), for: .normal)
        connectButton.isValid = false
        codeInput.textField.keyboardType = .numberPad
        
        let margin: CGFloat = 20
        constrain(titleLabel, connectButton, codeInput, view) { title, connect, code, view in
            title.top == view.safeAreaLayoutGuide.top + margin
            title.left == view.left + margin
            title.right == view.right - margin
            
            code.top == title.bottom + margin
            code.left == title.left
            code.right == title.right
            
            connect.top == code.bottom + margin
            connect.centerX == view.centerX
        }
        
        codeInput.textField.delegate = self
        connectButton.addTarget(self, action: #selector(connect(_:)), for: .touchUpInside)
    }
    
    @objc fileprivate func connect(_ sender: Any) {
        delegate?.connect(code: codeInput.text!)
        view.endEditing(true)
    }
}

extension OnboardingSharePage: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let newValue = (textField.text! as NSString).replacingCharacters(in: range, with: string)
        connectButton.isValid = newValue.count == .codeLimit
        return newValue.count <= .codeLimit
    }
}

fileprivate let configureLabel: (UILabel) -> () = { label in
    label.font = .elTitleLight
    label.textAlignment = .center
    label.textColor = .black
    label.numberOfLines = 0
}

extension Int {
    static let codeLimit = 6
    static let passwordLimit: (min: Int, max: Int) = (8,20)
}
