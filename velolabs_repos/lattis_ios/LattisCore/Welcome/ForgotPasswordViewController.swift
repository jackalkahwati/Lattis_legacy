//
//  ForgotPasswordViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 09.03.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Localize_Swift
import Cartography

class ForgotPasswordViewController: UIViewController, WelcomeOptionController {
    weak var delegate: WelcomeDelegate?
    var keyboardPadding: CGFloat = -50
    fileprivate let titleLabel = UILabel()
    fileprivate let emailField: TextFieldView = .email
    fileprivate let codeField: TextFieldView = .code
    fileprivate let passwordField: TextFieldView = .password(isNew: true)
    fileprivate let actionButton = ActionButton(.plain(title: "send_verification_code".localized()))
    fileprivate let emailLabel = UILabel()
    fileprivate var email: String?
    fileprivate var code: String? {
        guard let c = codeField.text, !c.isEmpty else { return nil }
        return c
    }
    fileprivate var password: String? {
        guard let pass = passwordField.text, !pass.isEmpty else { return nil }
        return pass
    }
    
    fileprivate let network: UserAPI = AppRouter.shared.api()
    
    init(_ email: String? = nil) {
        self.email = email
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        titleLabel.text = "restore_password".localized()
        titleLabel.textAlignment = .center
        titleLabel.font = .theme(weight: .bold, size: .giant)
        
        let logInBiutton = UIButton(type: .system)
        logInBiutton.setTitle("log_in".localized(), for: .normal)
        logInBiutton.addTarget(self, action: #selector(logIn), for: .touchUpInside)
        
        emailLabel.font = .boldSystemFont(ofSize: 18)
        let changeButton = UIButton(type: .system)
        changeButton.setTitle("change".localized(), for: .normal)
        changeButton.addTarget(self, action: #selector(change), for: .touchUpInside)
        let emailContainer = UIStackView(arrangedSubviews: [emailLabel, changeButton])
        emailContainer.spacing = .margin/4
        
        let contentView: UIStackView
        if email == nil {
            emailField.text = welcomeEmail
            contentView = UIStackView(arrangedSubviews: [titleLabel, emailField, actionButton, logInBiutton])
        } else {
            actionButton.action = .plain(title: "submit".localized())
            emailLabel.text = email
            contentView = UIStackView(arrangedSubviews: [titleLabel, emailContainer, codeField, passwordField, actionButton, logInBiutton])
            let resendButton = UIButton(type: .system)
            resendButton.setTitle("resend".localized(), for: .normal)
            resendButton.addTarget(self, action: #selector(resend), for: .touchUpInside)
            codeField.add(button: resendButton)
        }
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
        
        actionButton.isActive = validate()
        
        actionButton.addTarget(self, action: #selector(actionHandler), for: .touchUpInside)
        emailField.field.addTarget(self, action: #selector(textUpdated(_:)), for: .editingChanged)
        codeField.field.addTarget(self, action: #selector(textUpdated(_:)), for: .editingChanged)
        passwordField.field.addTarget(self, action: #selector(textUpdated(_:)), for: .editingChanged)
        
        passwordField.field.delegate = self
        codeField.field.delegate = self
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if email == nil {
            emailField.field.becomeFirstResponder()
        } else {
            codeField.field.becomeFirstResponder()
        }
    }
    
    fileprivate func validate() -> Bool {
        if email != nil {
            guard let code = codeField.text,
                let pass = passwordField.text else { return false }
            return code.count == .codeLimit && pass.count <= .passwordMax && pass.count >= .passwordMin
        } else if let email = emailField.text {
            return email.isValidEmail
        }
        return false
    }
    
    @objc fileprivate func textUpdated(_ sender: UITextField) {
        actionButton.isActive = validate()
    }
    
    @objc fileprivate func logIn() {
        view.endEditing(true)
        delegate?.switchCard(controller: LogInViewController())
    }
    
    @objc fileprivate func actionHandler() {
        view.endEditing(true)
        guard actionButton.isActive, let email = email ?? emailField.text else { return }
        restorePassword(email: email, code: code, password: password)
    }
    
    @objc fileprivate func resend() {
        view.endEditing(true)
        guard let email = email else { return }
        restorePassword(email: email, code: nil, password: nil)
    }
    
    fileprivate func restorePassword(email: String, code: String?, password: String?) {
        startLoading("sending_confirmation_code".localized())
        network.restorePasswrd(email: email, code: code, password: password) { [weak self] (result) in
            switch result {
            case .success:
                self?.stopLoading {
                    if code == nil {
                        if self?.email == nil {
                            self?.delegate?.switchCard(controller: ForgotPasswordViewController(email))
                        } else { // Resend
                            self?.codeField.text = nil
                            self?.codeField.field.becomeFirstResponder()
                            self?.actionButton.isActive = self?.validate() ?? false
                        }
                    } else {
                        self?.successAlert()
                    }
                }
            case .failure(let error):
                self?.handle(error)
            }
        }
    }
    
    fileprivate func successAlert() {
        welcomeEmail = email
        let alert = AlertController(title: "password_updated_title".localized(), message: .plain("password_updated_message".localized()))
        alert.actions = [
            .plain(title: "log_in".localized()) { [unowned self] in self.logIn() }
        ]
        present(alert, animated: true, completion: nil)
    }
    
    @objc fileprivate func change() {
        view.endEditing(true)
        welcomeEmail = nil
        delegate?.switchCard(controller: ForgotPasswordViewController())
    }
}

extension ForgotPasswordViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string) else { return false }
        if textField == passwordField.field {
            return newText.count <= .passwordMax
        }
        if textField == codeField.field {
            return newText.count <= .codeLimit
        }
        return true
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
}
