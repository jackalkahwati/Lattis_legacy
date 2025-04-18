//
//  SignUpSignUpViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

class SignUpViewController: ViewController {
    @IBOutlet weak var submitButton: UIButton!
    @IBOutlet weak var passField: TextField!
    @IBOutlet weak var emailField: TextField!
    @IBOutlet weak var lastNameField: TextField!
    @IBOutlet weak var nameField: TextField!
    var interactor: SignUpInteractorInput!
    
    fileprivate var canSubmit: Bool = false {
        didSet {
            submitButton.isEnabled = canSubmit
            submitButton.alpha = canSubmit ? 1 : 0.5
        }
    }
    
    fileprivate let showButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle("general_btn_show".localized(), for: .normal)
        button.setTitle("general_btn_hide".localized(), for: .selected)
        button.titleLabel?.font = UIFont(.circularBook, size: 13)
        button.sizeToFit()
        button.setTitleColor(.lsTurquoiseBlue, for: .normal)
        return button
    }()

    override func viewDidLoad() {
        super.viewDidLoad()

        passField.delegate = self
        emailField.delegate = self
        lastNameField.delegate = self
        nameField.delegate = self
        
        passField.validation = .password
        emailField.validation = .email
        lastNameField.validation = .notEmpty
        nameField.validation = .notEmpty
        
        showButton.addTarget(self, action: #selector(showPass(_:)), for: .touchUpInside)
        passField.rightViewMode = .always
        passField.rightView = showButton
        
        canSubmit = true
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    @objc private func showPass(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        passField.isSecureTextEntry = !passField.isSecureTextEntry
    }
    
    @IBAction func createAccount(_ sender: Any) {
        guard validate() else { return }
        view.endEditing(true)
        interactor.signUp(user: .registration(email: emailField.text!, firstName: nameField.text, lastName: lastNameField.text, password: passField.text!))
        
    }
    
    @IBAction func signIn(_ sender: Any) {
        interactor.openLogIn()
    }
    
    @IBAction func fieldChanged(_ sender: Any) {
//        canSubmit = passField.isValid && emailField.isValid && nameField.isValid && lastNameField.isValid
    }
    
    fileprivate func validate() -> Bool {
        if !emailField.isValid {
            warning(with: "label_problem".localized(), subtitle: "label_enter_valid_email_address".localized())
            return false
        }
        if !passField.isValid {
            warning(with: "label_problem".localized(), subtitle: "password_must_be".localized())
            return false
        }
        return true
    }
}

extension SignUpViewController: SignUpInteractorOutput {}

extension SignUpViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string), let field = textField as? TextField else { return true }
        return field.validation.canCahnge(to: newText)
    }
}
