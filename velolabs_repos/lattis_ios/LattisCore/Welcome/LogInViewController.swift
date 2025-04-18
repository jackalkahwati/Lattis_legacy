//
//  LogInViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 24/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Wrappers
import Atributika
import SafariServices

class LogInViewController: UIViewController, WelcomeOptionController {
    
    weak var delegate: WelcomeDelegate?
    var keyboardPadding: CGFloat = -200
    fileprivate let titleLabel = UILabel()
    fileprivate let emailField: TextFieldView = .email
    fileprivate let passwordField: TextFieldView = .password()
    fileprivate let actionButton = ActionButton(.plain(title: "log_in".localized()))
    fileprivate let forgotButton = UIButton(type: .system)
    fileprivate let createButton = ActionButton(.plain(title: "create_an_account".localized(), style: .active))
    fileprivate var legalLabel: AttributedLabel!
    fileprivate let alertLabel = UILabel()
    fileprivate var contentView: UIStackView!
    
    fileprivate let network: UserAPI = AppRouter.shared.api()
    
    fileprivate var alertMessage: String?
    fileprivate weak var firstResponder: TextFieldView?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        titleLabel.text = "welcome".localized()
        titleLabel.font = .theme(weight: .bold, size: .giant)
        titleLabel.textAlignment = .center
        
        alertLabel.font = .theme(weight: .medium, size: .small)
        alertLabel.textColor = .warning
        alertLabel.textAlignment = .center
        alertLabel.numberOfLines = 0
        
        actionButton.addTarget(self, action: #selector(handleAction), for: .touchUpInside)
        createButton.addTarget(self, action: #selector(createAccount), for: .touchUpInside)
                
        legalLabel = .legal(self)
        
        contentView = UIStackView(arrangedSubviews: [titleLabel, emailField, passwordField, actionButton, forgotButton, createButton, legalLabel])
        contentView.axis = .vertical
        contentView.spacing = .margin
        contentView.setCustomSpacing(.margin*2, after: forgotButton)
        contentView.setCustomSpacing(.margin/2, after: createButton)
        forgotButton.setTitle("forgot_password".localized(), for: .normal)
        forgotButton.tintColor = .black
        forgotButton.addTarget(self, action: #selector(forgotAction), for: .touchUpInside)

        emailField.field.addTarget(self, action: #selector(textUpdated(_:)), for: .editingChanged)
        passwordField.field.addTarget(self, action: #selector(textUpdated(_:)), for: .editingChanged)
        
        emailField.text = welcomeEmail
        
        view.addSubview(contentView)
        
        constrain(contentView, view) { content, view in
            content.top == view.top + .margin
            content.bottom >= view.safeAreaLayoutGuide.bottom - .margin
            content.bottom == view.bottom - .margin*2 ~ .defaultLow
            content.left == view.left
            content.right == view.right
        }
        passwordField.field.delegate = self
        emailField.field.delegate = self
        actionButton.isActive = validate()
    }
    
    @objc fileprivate func textUpdated(_ textField: UITextField) {
        firstResponder?.isFailed = false
        hideAlert()
        welcomeEmail = emailField.text
        actionButton.isActive = validate()
    }
    
    fileprivate func validate() -> Bool {
        guard let email = emailField.text, email.isValidEmail else {
            alertMessage = "email_invalid".localized()
            firstResponder = emailField
            return false
        }
        emailField.isFailed = false
        guard let pass = passwordField.text, pass.count >= 8 else {
            alertMessage = "password_invalid".localized()
            firstResponder = passwordField
            return false
        }
        passwordField.isFailed = false
        return true
    }
    
    func passwordUpdated(email: String) {
        emailField.text = email
        _ = passwordField.becomeFirstResponder()
    }
    
    @objc fileprivate func createAccount() {
        delegate?.switchCard(controller: SignUpViewController())
    }
    
    fileprivate func showAlert() {
        alertLabel.text = alertMessage
        if alertMessage != nil {
            contentView.insertArrangedSubview(alertLabel, at: 3)
        }
        firstResponder?.field.becomeFirstResponder()
        firstResponder?.isFailed = true
//        #if DEBUG
//        emailField.text = "ravil@lattis.io"
//        passwordField.text = "ravillattis"
//        actionButton.isActive = validate()
//        #endif
    }
    
    fileprivate func hideAlert() {
        guard alertLabel.superview != nil else { return }
        contentView.removeArrangedSubview(alertLabel)
        alertLabel.removeFromSuperview()
    }
    
    @objc fileprivate func handleAction() {
        view.endEditing(true)
        guard let email = emailField.text, let pass = passwordField.text, actionButton.isActive else {
            return showAlert()
        }
        delegate?.signIn(user: .init(email: email, password: pass), with: "log_in_loader".localized())
    }
    
    @objc fileprivate func forgotAction() {
        view.endEditing(true)
        delegate?.switchCard(controller: ForgotPasswordViewController())
    }
    
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        if error.isInvalidEmailLogIn {
            alertMessage = "no_account_exists_message".localized()
            firstResponder = emailField
            return showAlert()
        }
        if error.isPasswordWrong {
            alertMessage = nil
            firstResponder = passwordField
            return showAlert()
        }
        super.handle(error, from: viewController, retryHandler: retryHandler)
    }
}

extension LogInViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string) else { return false }
        if textField == passwordField.field {
            return newText.count <= .passwordMax
        }
        return true
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
}
