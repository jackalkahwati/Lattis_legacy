//
//  LogInLogInViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Oval
import PhoneNumberKit

class LogInViewController: ViewController {
    @IBOutlet var loginView: LogInView!
    var interactor: LogInInteractorInput!

    fileprivate let passValidator = TextValidator.password
    fileprivate let emailValidator = TextValidator.email
    override func viewDidLoad() {
        super.viewDidLoad()

        navigationController?.isNavigationBarHidden = true
        loginView.passField.delegate = self
        loginView.phoneField.delegate = self
    }

    @IBAction func loginAction(_ sender: Any) {
        interactor.login(with: loginView.phoneField.text!, password: loginView.passField.text!)
        view.endEditing(true)
    }
    
    @IBAction func forgotPassword(_ sender: Any) {
        
    }
    
    @IBAction func close(_ sender: Any) {
        dismiss(animated: true, completion: nil)
    }
    
    @IBAction func textChanged(_ sender: UITextField) {
        loginView.submitIsEnabled = passValidator.isValid(text: loginView.passField.text!) && emailValidator.isValid(text: loginView.phoneField.text!)
    }
}

extension LogInViewController: LogInInteractorOutput {

}

extension LogInViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        view.endEditing(true)
        return false
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string) else { return true }
        return validator(for: textField).canCahnge(to: newText)
    }
}

private extension LogInViewController {
    func validator(for textField: UITextField) -> TextValidator {
        if textField == loginView.passField {
            return passValidator
        }
        if textField == loginView.phoneField {
            return emailValidator
        }
        return TextValidator.none
    }
}
