//
//  SignUpViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/9/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography
import TPKeyboardAvoiding
import PhoneNumberKit

final class SignUpViewController: LogInBaseViewController {
    
    fileprivate let emailInput = TitledInputControl(title: "email".localized().lowercased().capitalized)
    fileprivate let passwordInput = TitledInputControl(title: "password".localized().lowercased().capitalized, hint: "password_hint".localized())
    fileprivate let phoneInput = TitledInputControl(title: "mobile_number".localized().lowercased().capitalized)
    fileprivate let submitButton = UIButton(type: .custom)
    fileprivate let smsNoteLabel = UILabel()
    fileprivate let hideShowButton = UIButton(type: .custom)
    
    fileprivate var cleanPhoneNumber: String? {
        let region = Locale.current.regionCode ?? "US"
        guard let phone = try? phoneNumberKit.parse(phoneInput.text!, withRegion: region, ignoreType: true) else { return nil }
        return "+\(String(phone.countryCode))\(String(phone.nationalNumber))"
    }
    
    fileprivate var credentials: User.Credentials? {
        guard let phone = cleanPhoneNumber else { return nil }
        var cred = User.Credentials(phone: phone, password: passwordInput.text)
        cred.email = emailInput.text
        cred.isSigningUp = true
        return cred
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "action_sign_in".localized().capitalized
    }
    
    override func configureScrollview() {
        view.addSubview(scrollView)
        view.sendSubviewToBack(scrollView)
        let margin: CGFloat = 20
        constrain(scrollView, view, orLabel) { scroll, container, label in
            scroll.left == container.left
            scroll.right == container.right
            scroll.top == container.safeAreaLayoutGuide.top
            scroll.bottom == label.top - margin/4
        }
        
        scrollView.addSubview(phoneInput)
        scrollView.addSubview(passwordInput)
        scrollView.addSubview(emailInput)
        scrollView.addSubview(submitButton)
        scrollView.addSubview(smsNoteLabel)
        
        hideShowStyle(hideShowButton)
        passwordInput.rightButton = hideShowButton
        hideShowButton.addTarget(self, action: #selector(showPassword(_:)), for: .touchUpInside)
        
        bigRoundCorners(submitButton)
        submitButton.setTitle("text_verIfication_code".localized().lowercased().capitalized, for: .normal)
        submitButton.backgroundColor = .elDarkSkyBlue
        submitButton.addTarget(self, action: #selector(signUp), for: .touchUpInside)
        
        smsNoteLabel.text = "signup_hint_sms".localized()
        smsNoteLabel.textColor = .black
        smsNoteLabel.numberOfLines = 0
        smsNoteLabel.font = .elTitleLight
        smsNoteLabel.textAlignment = .center
        
        emailInput.placeholder = "example@domain.com"
        emailInput.textField.textContentType = .emailAddress
        emailInput.textField.keyboardType = .emailAddress
        emailInput.textField.autocapitalizationType = .none
        
        phoneInput.placeholder = "XXX-XXX-XXXX"
        phoneInput.textField.textContentType = .telephoneNumber
        phoneInput.textField.keyboardType = .phonePad
        phoneInput.textField.addTarget(self, action: #selector(phoneNumberChanged(_:)), for: .editingChanged)
        
        passwordInput.placeholder = "password_hint".localized()
        passwordInput.textField.isSecureTextEntry = true
        passwordInput.textField.textContentType = .password
        passwordInput.limit = 16
        
        constrain(phoneInput, passwordInput, emailInput, submitButton, facebookButton, smsNoteLabel, scrollView) { phone, pass, email, submit, facebook, sms, scroll in
            email.left == scroll.superview!.left + margin
            email.right == scroll.superview!.right - margin
            email.top == scroll.top + margin*2
            
            pass.left == email.left
            pass.right == email.right
            pass.top == email.bottom + margin*2
            
            phone.left == email.left
            phone.right == email.right
            phone.top == pass.bottom + margin*2
            
            submit.left == email.left ~ .defaultLow
            submit.right == email.right ~ .defaultLow
            submit.centerX == scroll.centerX
            submit.top == phone.bottom + margin*2
            
            sms.left == email.left
            sms.right == email.right
            sms.top == submit.bottom + margin
            sms.bottom == scroll.bottom - margin/2
            
            facebook.width == submit.width
        }
    }
    
    @objc fileprivate func phoneNumberChanged(_ sender: UITextField) {
        guard let number = cleanPhoneNumber else { return }
        sender.text = PartialFormatter().formatPartial(number)
    }
    
    @objc fileprivate func signUp() {
        guard let email = emailInput.text, email.isValidEmail else { return emailInput.isInvalid = true }
        guard let pass = passwordInput.text, pass.count >= 8, pass.count <= 16 else { return passwordInput.isInvalid = true }
        guard let _ = cleanPhoneNumber else { return phoneInput.isInvalid = true }
        view.endEditing(true)
        interactor.login(with: credentials)
    }
    
    @objc fileprivate func showPassword(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        sender.sizeToFit()
        passwordInput.textField.isSecureTextEntry = !sender.isSelected
    }
}

