//
//  NewPasswordViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 24/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class NewPasswordViewController: ViewController {
    @IBOutlet weak var codeField: TextField!
    @IBOutlet weak var submitButton: UIButton!
    @IBOutlet weak var passField: TextField!
    @IBOutlet weak var emailLabel: UILabel!
    var interactor: ForgotInteractorInput!
    
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
        passField.validation = .password
        
        codeField.validation = .limit(6)
        codeField.delegate = self
        
        showButton.addTarget(self, action: #selector(showPass(_:)), for: .touchUpInside)
        passField.rightViewMode = .always
        passField.rightView = showButton
        
        interactor.viewLoaded()
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    @objc private func showPass(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        passField.isSecureTextEntry = !passField.isSecureTextEntry
    }
    
    fileprivate var canSubmit: Bool = false {
        didSet {
            submitButton.isEnabled = canSubmit
            submitButton.alpha = canSubmit ? 1 : 0.5
        }
    }
    
    @IBAction func passwordChanged(_ sender: Any) {
        canSubmit = passField.isValid && codeField.isValid
    }
    
    @IBAction func submit(_ sender: Any) {
        view.endEditing(true)
        interactor.submit(password: passField.text!, code: codeField.text!)
    }
    
    @IBAction func back(_ sender: Any) {
        _ = navigationController?.popViewController(animated: true)
    }
}

extension NewPasswordViewController: ForgotInteractorOutput {
    func show(email: String) {
        emailLabel.text = email
    }
    
    func showSuccess() {
        let alert = ErrorAlertView.alert(title: "login_forgot_success_title".localized(), subtitle: "login_forgot_success_text".localized(), button: "login_forgot_success_button".localized())
        alert.action = {
            self.navigationController?.popToRootViewController(animated: true)
        }
        stopLoading() {
            alert.show()
        }
    }
}

extension NewPasswordViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string), let field = textField as? TextField else { return true }
        return field.validation.canCahnge(to: newText)
    }
}
