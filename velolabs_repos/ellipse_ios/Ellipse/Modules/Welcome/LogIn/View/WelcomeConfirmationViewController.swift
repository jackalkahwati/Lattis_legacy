//
//  WelcomeConfirmationViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/9/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography
import TPKeyboardAvoiding

class WelcomeConfirmationViewController: ViewController {
    
    var phoneNumber: String!
    var interactor: LogInInteractorInput!
    
    fileprivate let scrollView = TPKeyboardAvoidingScrollView()
    fileprivate let headerLabel = UILabel()
    fileprivate let codeInput = TitledInputControl(title: "hint_enter_code".localized().uppercased(), hint: "code_hint".localized())
    fileprivate let resendButton = UIButton(type: .custom)
    fileprivate let verifyButton = UIButton(type: .custom)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "action_sign_in".localized().capitalized
        view.backgroundColor = .white
        addBackButton()
        
        configureUI()
    }
    
    fileprivate func configureUI() {
        
        view.addSubview(scrollView)
        scrollView.addSubview(headerLabel)
        scrollView.addSubview(codeInput)
        scrollView.addSubview(verifyButton)
        scrollView.addSubview(resendButton)
        
        titleLigtStyle(headerLabel)
        headerLabel.text = "welcome_confirmation_hint_sms".localizedFormat(phoneNumber)
        
        bigRoundCorners(verifyButton)
        verifyButton.backgroundColor = .elDarkSkyBlue
        verifyButton.setTitleColor(.white, for: .normal)
        verifyButton.setTitle("confirm".localized().lowercased().capitalized, for: .normal)
        verifyButton.addTarget(self, action: #selector(verifyAction), for: .touchUpInside)
        
        resendButton.titleLabel?.font = .elButtonBig
        resendButton.setTitle("label_resend_code".localized().lowercased().capitalized, for: .normal)
        resendButton.setTitleColor(.elDarkSkyBlue, for: .normal)
        resendButton.addTarget(self, action: #selector(resendAction), for: .touchUpInside)
        
        codeInput.placeholder = "code_hint".localized()
        codeInput.textField.keyboardType = .numberPad
        codeInput.limit = 6
        if #available(iOS 12.0, *) {
            codeInput.textField.textContentType = .oneTimeCode
        } else {
            codeInput.textField.textContentType = .postalCode
        }
        
        let margin: CGFloat = 40
        constrain(headerLabel, codeInput, verifyButton, resendButton, scrollView, view) { label, code, verify, resend, container, view in
            container.left == view.left
            container.right == view.right
            container.top == view.safeAreaLayoutGuide.top
            container.bottom == view.safeAreaLayoutGuide.bottom
            
            label.left == view.left + margin
            label.right == view.right - margin
            label.top == container.top + margin
            
            code.top == label.bottom + margin
            code.left == label.left
            code.right == label.right
            
            verify.left == label.left
            verify.right == label.right
            verify.top == code.bottom + margin
            
            resend.left == label.left
            resend.right == label.right
            resend.top == verify.bottom + margin/4
            resend.bottom == container.bottom - margin/4
        }
    }
    
    @objc fileprivate func verifyAction() {
        guard let code = codeInput.textField.text, code.count == 6 else { return codeInput.isInvalid = true }
        view.endEditing(true)
        interactor.confirm(code: code)
    }
    
    @objc fileprivate func resendAction() {
        view.endEditing(true)
        interactor.resendCode(to: phoneNumber)
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
}
