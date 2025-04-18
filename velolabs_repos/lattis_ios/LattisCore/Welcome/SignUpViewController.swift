//
//  SignUpViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 24/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Atributika
import SafariServices

class SignUpViewController: UIViewController, WelcomeOptionController {
    
    weak var delegate: WelcomeDelegate?
    var keyboardPadding: CGFloat = -130
    fileprivate let titleLabel = UILabel()
    fileprivate let actionButton = ActionButton(.plain(title: "submit".localized()))
    fileprivate let emailField: TextFieldView = .email
    fileprivate let passwordField: TextFieldView = .password(isNew: true)
    fileprivate let firstNameField: TextFieldView = .firstName
    fileprivate let lastNameField: TextFieldView = .larstName
    fileprivate let logInButton = UIButton(type: .system)
    fileprivate var legalLabel: AttributedLabel!
    fileprivate let alertLabel = UILabel()
    fileprivate var contentView: UIStackView!
    
    fileprivate let network: UserAPI = AppRouter.shared.api()
    
    fileprivate var alertMessage: String?
    fileprivate weak var firstResponder: TextFieldView?

    override func viewDidLoad() {
        super.viewDidLoad()
        
        titleLabel.text = "create_account".localized()
        titleLabel.font = .theme(weight: .bold, size: .giant)
        titleLabel.textAlignment = .center
        
        alertLabel.font = .theme(weight: .medium, size: .small)
        alertLabel.textColor = .warning
        alertLabel.textAlignment = .center
        alertLabel.numberOfLines = 0
        
        legalLabel = .legal(self)
        
        let infoLabel = UILabel()
        infoLabel.text = "already_have_an_account".localized()
        let login = CenteredStackView(subviews: [infoLabel, logInButton])
        contentView = UIStackView(arrangedSubviews: [titleLabel, firstNameField, lastNameField, emailField, passwordField, actionButton, login, legalLabel])
        contentView.axis = .vertical
        contentView.spacing = .margin
        view.addSubview(contentView)
        
        constrain(contentView, view) { content, view in
            content.top == view.top + .margin
            content.bottom >= view.safeAreaLayoutGuide.bottom - .margin
            content.bottom == view.bottom - .margin*2 ~ .defaultLow
            content.left == view.left
            content.right == view.right
        }
        
        logInButton.setTitle("log_in".localized(), for: .normal)
        
        actionButton.addTarget(self, action: #selector(handleAction), for: .touchUpInside)
        logInButton.addTarget(self, action: #selector(logIn), for: .touchUpInside)
        
        emailField.field.addTarget(self, action: #selector(textUpdated(_:)), for: .editingChanged)
        passwordField.field.addTarget(self, action: #selector(textUpdated(_:)), for: .editingChanged)
        firstNameField.field.addTarget(self, action: #selector(textUpdated(_:)), for: .editingChanged)
        lastNameField.field.addTarget(self, action: #selector(textUpdated(_:)), for: .editingChanged)
        
        emailField.text = welcomeEmail
        firstNameField.text = welcomeFirstName
        lastNameField.text = welcomeLastName
        
        passwordField.field.delegate = self
        emailField.field.delegate = self
        firstNameField.field.delegate = self
        lastNameField.field.delegate = self
        
        actionButton.isActive = validate()
    }
    
    @objc fileprivate func logIn() {
        delegate?.switchCard(controller: LogInViewController())
    }
    
    @objc fileprivate func textUpdated(_ textField: UITextField) {
        firstResponder?.isFailed = false
        hideAlert()
        welcomeEmail = emailField.text
        welcomeFirstName = firstNameField.text
        welcomeLastName = lastNameField.text
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
    
    fileprivate func hideAlert() {
        guard alertLabel.superview != nil else { return }
        contentView.removeArrangedSubview(alertLabel)
        alertLabel.removeFromSuperview()
    }
    
    fileprivate func showAlert() {
        alertLabel.text = alertMessage
        contentView.insertArrangedSubview(alertLabel, at: 5)
        firstResponder?.field.becomeFirstResponder()
        firstResponder?.isFailed = true
    }
    
    @objc fileprivate func handleAction() {
        view.endEditing(true)
        guard let email = emailField.text,
            let password = passwordField.text,
            let firstName = firstNameField.text,
            let lastName = lastNameField.text,
            actionButton.isActive else {
                return showAlert()
        }
        delegate?.signIn(user: .init(email: email, password: password, firstName: firstName, lastName: lastName), with: "log_in_loader".localized())
        Analytics.log(.signUp())
    }
    
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        if error.isPasswordWrong {
            alertMessage = "signup_account_exists_text".localized()
            firstResponder = emailField
            return showAlert()
        }
        super.handle(error, from: viewController, retryHandler: retryHandler)
    }
}

extension SignUpViewController: UITextFieldDelegate {
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
