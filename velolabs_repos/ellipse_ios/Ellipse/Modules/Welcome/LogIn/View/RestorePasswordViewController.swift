//
//  RestorePasswordViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/9/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography
import TPKeyboardAvoiding

class RestorePasswordViewController: ViewController {
    
    var phoneNumber: String!
    var interactor: LogInInteractorInput!
    
    fileprivate let scrollView = TPKeyboardAvoidingScrollView()
    fileprivate let headerLabel = UILabel()
    fileprivate let codeInput = TitledInputControl(title: "hint_enter_code".localized().uppercased(), hint: "code_hint".localized())
    fileprivate let passwordInput = TitledInputControl(title: "hint_password".localized().uppercased(), hint: "password_hint".localized())
    fileprivate let saveButton = UIButton(type: .custom)
    fileprivate let resendButton = UIButton(type: .custom)
    fileprivate let hideShowButton = UIButton(type: .custom)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "restore_pass_title".localized().capitalized
        view.backgroundColor = .white
        
        configureUI()
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    fileprivate func configureUI() {
        
        view.addSubview(scrollView)
        scrollView.addSubview(headerLabel)
        scrollView.addSubview(codeInput)
        scrollView.addSubview(passwordInput)
        scrollView.addSubview(saveButton)
        scrollView.addSubview(resendButton)
        
        hideShowStyle(hideShowButton)
        passwordInput.rightButton = hideShowButton
        hideShowButton.addTarget(self, action: #selector(showPassword(_:)), for: .touchUpInside)
        
        titleLigtStyle(headerLabel)
        headerLabel.text = "restore_pass_hint_sms".localizedFormat(phoneNumber)
        
        bigRoundCorners(saveButton)
        saveButton.backgroundColor = .elDarkSkyBlue
        saveButton.setTitleColor(.white, for: .normal)
        saveButton.setTitle("save_password".localized().uppercased(), for: .normal)
        saveButton.addTarget(self, action: #selector(savePassword), for: .touchUpInside)
        
        resendButton.setTitleColor(.elDarkSkyBlue, for: .normal)
        resendButton.titleLabel?.font = .elButtonBig
        resendButton.setTitle("label_resend_code".localized().uppercased(), for: .normal)
        resendButton.addTarget(self, action: #selector(resendCode), for: .touchUpInside)
        
        passwordInput.placeholder = "password_hint".localized()
        passwordInput.textField.isSecureTextEntry = true
        passwordInput.textField.textContentType = .password
        passwordInput.limit = 16
        
        codeInput.placeholder = "code_hint".localized()
        codeInput.textField.keyboardType = .numberPad
        codeInput.limit = 6
        if #available(iOS 12.0, *) {
            codeInput.textField.textContentType = .oneTimeCode
        } else {
            codeInput.textField.textContentType = .postalCode
        }
        
        let margin: CGFloat = 20
        constrain(scrollView, headerLabel, codeInput, passwordInput, saveButton, resendButton, view) { scroll, header, code, pass, save, resend, view in
            scroll.left == view.left
            scroll.right == view.right
            scroll.top == view.safeAreaLayoutGuide.top
            scroll.bottom == view.safeAreaLayoutGuide.bottom
            
            header.left == view.left + margin
            header.right == view.right - margin
            header.top == scroll.top + margin
            
            code.left == header.left
            code.right == header.right
            code.top == header.bottom + margin*2
            
            pass.left == header.left
            pass.right == header.right
            pass.top == code.bottom + margin*2
            
            save.left == header.left
            save.right == header.right
            save.top == pass.bottom + margin*2
            
            resend.centerX == view.centerX
            resend.top == save.bottom + margin/2
            resend.bottom == scroll.bottom - margin/2
        }
    }
    
    @objc fileprivate func savePassword() {
        guard let code = codeInput.textField.text, code.count == 6 else { return codeInput.isInvalid = true }
        guard let pass = passwordInput.text, pass.count >= 8, pass.count <= 16 else { return passwordInput.isInvalid = true }
        view.endEditing(true)
        interactor.save(pass: pass, code: code, phone: phoneNumber)
    }
    
    @objc fileprivate func resendCode() {
        view.endEditing(true)
        interactor.resendCode(to: phoneNumber)
    }
    
    @objc fileprivate func showPassword(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        sender.sizeToFit()
        passwordInput.textField.isSecureTextEntry = !sender.isSelected
    }
}

