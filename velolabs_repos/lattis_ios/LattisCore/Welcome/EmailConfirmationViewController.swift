//
//  EmailConfirmationViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 06.03.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Localize_Swift
import OvalAPI

class CenteredStackView: UIStackView {
    
     init(subviews: [UIView]) {
        let container = UIStackView(arrangedSubviews: subviews)
        container.spacing = .margin/4
        super.init(frame: .zero)
        self.axis = .vertical
        self.distribution = .fill
        self.alignment = .center
        self.addArrangedSubview(container)
    }
    
    required init(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class EmailConfirmationViewController: UIViewController, WelcomeOptionController {
    weak var delegate: WelcomeDelegate?
    var keyboardPadding: CGFloat = -50
    fileprivate let titleLabel = UILabel()
    fileprivate let infoLabel = UILabel()
    fileprivate let emailLabel = UILabel()
    fileprivate var codeField: TextFieldView!
    fileprivate let actionButton = ActionButton(.plain(title: "submit".localized()))
    fileprivate let alertLabel = UILabel.label(text: "error_confirmation_code".localized(), font: .theme(weight: .medium, size: .small), color: .warning, allignment: .center, lines: 0)
    
    fileprivate let network: UserAPI = AppRouter.shared.api()
    
    init(_ email: String) {
        super.init(nibName: nil, bundle: nil)
        emailLabel.text = email
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        emailLabel.font = .boldSystemFont(ofSize: 18)
        
        titleLabel.text = "verify_email".localized()
        titleLabel.font = .theme(weight: .bold, size: .giant)
        titleLabel.textAlignment = .center
        
        infoLabel.numberOfLines = 0
        infoLabel.text = "verification_code_description".localized()
        
        let changeButton = UIButton(type: .system)
        changeButton.setTitle("change".localized(), for: .normal)
        changeButton.addTarget(self, action: #selector(changeEmail), for: .touchUpInside)
        let emailContainer = CenteredStackView(subviews:[emailLabel, changeButton])
        
        let resendButton = UIButton(type: .system)
        resendButton.setTitle("resend".localized(), for: .normal)
        resendButton.addTarget(self, action: #selector(resend), for: .touchUpInside)
        codeField = .confirmation(resendButton)
        
        let signInLabel = UILabel()
        signInLabel.text = "already_have_an_account".localized()
        
        let signInButton = UIButton(type: .system)
        signInButton.setTitle("log_in".localized(), for: .normal)
        signInButton.addTarget(self, action: #selector(signIn), for: .touchUpInside)
        
        let signInContainer = CenteredStackView(subviews: [signInLabel, signInButton])
        
        let contentView = UIStackView(arrangedSubviews: [titleLabel, emailContainer, infoLabel, codeField, alertLabel, actionButton, signInContainer])
        contentView.axis = .vertical
        contentView.spacing = .margin
        view.addSubview(contentView)
        alertLabel.isHidden = true
        
        constrain(contentView, view) { content, view in
            content.top == view.top + .margin
            content.bottom >= view.safeAreaLayoutGuide.bottom - .margin
            content.bottom == view.bottom - .margin*2 ~ .defaultLow
            content.left == view.left
            content.right == view.right
        }
        
        codeField.field.delegate = self
        actionButton.addTarget(self, action: #selector(submit), for: .touchUpInside)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        codeField.field.becomeFirstResponder()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        infoLabel.preferredMaxLayoutWidth = view.bounds.width - .margin*2
    }
    
    fileprivate func validate(code: String? = nil) -> Bool {
        guard let c = code else { return false }
        return c.count == .codeLimit
    }
    
    @objc
    fileprivate func changeEmail() {
        delegate?.switchCard(controller: SignUpViewController())
    }
    
    @objc
    fileprivate func signIn() {
        delegate?.switchCard(controller: LogInViewController())
    }
    
    @objc
    fileprivate func submit() {
        guard let code = codeField.text, let email = emailLabel.text, actionButton.isActive else { return }
        if code.count == .codeLimit {
            verify(email: email, code: code)
        } else {
            codeField.isFailed = true
            alertLabel.isHidden = false
        }
    }
    
    @objc
    fileprivate func resend() {
        guard let email = emailLabel.text else { return }
        codeField.text = nil
        verify(email: email, code: nil)
    }
    
    fileprivate func verify(email: String, code: String?) {
        startLoading("verifying".localized())
        network.verify(email: email, code: code) { [weak self] (result) in
            switch result {
            case .success:
                self?.stopLoading {
                    if code != nil {
                        AppRouter.shared.openDashboard()
                    }
                }
            case .failure(let error):
                self?.handle(error)
            }
        }
    }
    
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        if let e = error as? SessionError, e.code == .unauthorized {
            return stopLoading {
                self.codeField.text = nil
                self.codeField.isFailed = true
                self.alertLabel.isHidden = false
            }
        }
        super.handle(error, from: viewController, retryHandler: retryHandler)
    }
}

extension EmailConfirmationViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let newString = (textField.text! as NSString).replacingCharacters(in: range, with: string)
        guard newString.count <= .codeLimit else { return false }
        codeField.isFailed = false
        alertLabel.isHidden = true
        actionButton.isActive = validate(code: newString)
        return true
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
}
