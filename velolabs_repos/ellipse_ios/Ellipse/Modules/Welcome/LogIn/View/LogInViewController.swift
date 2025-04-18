//
//  LogInLogInViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 06/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import PhoneNumberKit
import Cartography
import TPKeyboardAvoiding

final class LogInViewController: LogInBaseViewController {
    fileprivate let phoneInput = TitledInputControl(title: "mobile_number".localized().lowercased().capitalized)
    fileprivate let passwordInput = TitledInputControl(title: "password".localized().lowercased().capitalized, hint: "password_hint".localized())
    fileprivate let logInButton = UIButton(type: .custom)
    fileprivate let forgotButton = UIButton(type: .custom)
    fileprivate let hideShowButton = UIButton(type: .custom)
    
    fileprivate var credentials: User.Credentials? {
        guard let phone = cleanPhoneNumber else { return nil }
        return .init(phone: phone, password: passwordInput.text)
    }
    
    fileprivate var cleanPhoneNumber: String? {
        let region = Locale.current.regionCode ?? "US"
        guard let phone = try? phoneNumberKit.parse(phoneInput.text!, withRegion: region, ignoreType: true) else { return nil }
        return "+\(String(phone.countryCode))\(String(phone.nationalNumber))"
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "action_login_in".localized().capitalized
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
        scrollView.addSubview(logInButton)
        scrollView.addSubview(forgotButton)
        
        hideShowStyle(hideShowButton)
        passwordInput.rightButton = hideShowButton
        hideShowButton.addTarget(self, action: #selector(showPassword(_:)), for: .touchUpInside)
        
        bigRoundCorners(logInButton)
        logInButton.setTitle("action_login_in_short".localized().lowercased().capitalized, for: .normal)
        logInButton.backgroundColor = .elWindowsBlue
        logInButton.addTarget(self, action: #selector(logIn), for: .touchUpInside)
        
        forgotButton.setTitle("label_forgot_password".localized().lowercased().capitalized, for: .normal)
        forgotButton.titleLabel?.font = .elButtonBig
        forgotButton.setTitleColor(.elDarkSkyBlue, for: .normal)
        forgotButton.addTarget(self, action: #selector(restorePassword), for: .touchUpInside)
        
        phoneInput.placeholder = "XXX-XXX-XXXX"
        phoneInput.textField.textContentType = .telephoneNumber
        phoneInput.textField.keyboardType = .phonePad
        phoneInput.textField.addTarget(self, action: #selector(phoneNumberChanged(_:)), for: .editingChanged)
        
        passwordInput.placeholder = "password_hint".localized()
        passwordInput.textField.isSecureTextEntry = true
        passwordInput.textField.textContentType = .password
        passwordInput.limit = 16
        
        constrain(phoneInput, passwordInput, logInButton, facebookButton, forgotButton, scrollView) { phone, pass, login, facebook, forgot, container in
            phone.left == container.superview!.left + margin
            phone.right == container.superview!.right - margin
            phone.top == container.top + margin*2
            
            pass.left == phone.left
            pass.right == phone.right
            pass.top == phone.bottom + margin*2
            
            login.left == phone.left ~ .defaultLow
            login.right == phone.right ~ .defaultLow
            login.centerX == container.centerX
            login.top == pass.bottom + margin*2
            
            forgot.left == phone.left
            forgot.right == phone.right
            forgot.top == login.bottom + margin/2
            forgot.bottom == container.bottom - margin/2
            
            login.width == facebook.width
        }
    }
    
    @objc fileprivate func phoneNumberChanged(_ sender: UITextField) {
        guard let number = cleanPhoneNumber else { return }
        sender.text = PartialFormatter().formatPartial(number)
    }
    
    @objc fileprivate func logIn() {
        guard let _ = cleanPhoneNumber else { return phoneInput.isInvalid = true }
        guard let pass = passwordInput.text, pass.count >= 8, pass.count <= 16 else { return passwordInput.isInvalid = true }
        view.endEditing(true)
        interactor.login(with: credentials)
    }
    
    @objc fileprivate func restorePassword() {
        view.endEditing(true)
        guard cleanPhoneNumber != nil else {
            let action = AlertView.Action(title: "ok") { [unowned self] (_) in
                self.phoneInput.textField.becomeFirstResponder()
            }
            AlertView.alert(title: "warning".localized(), text: "please_enter_valid_phone".localized(), actions: [action]).show()
            return
        }
        interactor.restorePassword(for: phoneInput.text)
    }
    
    @objc fileprivate func showPassword(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        sender.sizeToFit()
        passwordInput.textField.isSecureTextEntry = !sender.isSelected
    }
}
