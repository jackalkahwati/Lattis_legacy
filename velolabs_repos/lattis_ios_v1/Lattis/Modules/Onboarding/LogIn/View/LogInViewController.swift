//
//  LogInLogInViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

class LogInViewController: ViewController {
    @IBOutlet weak var submitButton: UIButton!
    @IBOutlet weak var passField: TextField!
    @IBOutlet weak var emailField: TextField!
    var interactor: LogInInteractorInput!
    
    fileprivate let showButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle("general_btn_show".localized(), for: .normal)
        button.setTitle("general_btn_hide".localized(), for: .selected)
        button.titleLabel?.font = UIFont(.circularBook, size: 13)
        button.sizeToFit()
        button.setTitleColor(.lsTurquoiseBlue, for: .normal)
        return button
    }()
    
    fileprivate var canSubmit: Bool = false {
        didSet {
            submitButton.isEnabled = canSubmit
            submitButton.alpha = canSubmit ? 1 : 0.5
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        passField.delegate = self
        emailField.delegate = self
        
        passField.validation = .password
        emailField.validation = .email
        
        showButton.addTarget(self, action: #selector(showPass(_:)), for: .touchUpInside)
        passField.rightViewMode = .always
        passField.rightView = showButton
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    @objc private func showPass(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        passField.isSecureTextEntry = !passField.isSecureTextEntry
    }
    
    
    @IBAction func forgotPass(_ sender: Any) {
        interactor.forgotPassword()
    }
    
    @IBAction func logIn(_ sender: Any) {
        view.endEditing(true)
        interactor.logIn(user: .logIn(email: emailField.text!, password: passField.text!))
    }
    
    @IBAction func signUp(_ sender: Any) {
        interactor.openSignUp()
    }
    
    @IBAction func fieldChanged(_ sender: Any) {
        canSubmit = passField.isValid && emailField.isValid
    }
}

extension LogInViewController: LogInInteractorOutput {
    func show(email: String) {
        emailField.text = email
    }
}

extension LogInViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string), let field = textField as? TextField else { return true }
        return field.validation.canCahnge(to: newText)
    }
}
