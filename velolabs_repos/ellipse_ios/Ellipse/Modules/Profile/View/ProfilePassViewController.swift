//
//  ProfilePassViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/10/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

protocol ProfilePassDelegate: class {
    func resendCode(completion: @escaping (Error?) -> ())
    func confirm(password: String, with code: String, completion: @escaping (Error?) -> ())
    func paswordSaved()
}

class ProfilePassViewController: ViewController {
    @IBOutlet weak var savePassButton: ValidationButton!
    @IBOutlet weak var showPassButton: Button!
    @IBOutlet weak var passHintLabel: Label!
    @IBOutlet weak var codeHintLabel: Label!
    @IBOutlet weak var codeField: ValidatableTextField!
    @IBOutlet weak var passField: ValidatableTextField!
    
    weak var delegate: ProfilePassDelegate?
    fileprivate let validator = InputValidator([.code, .password])
    
    override func viewDidLoad() {
        super.viewDidLoad()

        title = "myprofile".localized()
        addBackButton()
        
        codeField.delegate = self
        passField.delegate = self
        codeField.inputType = .code
        passField.inputType = .password
        codeField.localizePlaceholder()
        passField.localizePlaceholder()
        
        validator.handle = { [unowned self] type, valid, isValid in
            switch type {
            case .code:
                self.codeHintLabel.isHidden = valid
            case .password:
                self.passHintLabel.isHidden = valid
            default:
                break
            }
            self.savePassButton.isValid = isValid
        }
        
        showPassButton.setTitle("hide".localized(), for: .selected)
    }
    
    @IBAction func savePass(_ sender: Any) {
        startLoading(text: "saving_password".localized())
        delegate?.confirm(password: passField.text!, with: codeField.text!) { [weak self] error in
            if let error = error {
                self?.show(error: error)
            } else {
                self?.stopLoading(completion: {
                    let action = AlertView.Action(title: "ok", handler: { _ in
                        self?.delegate?.paswordSaved()
                    })
                    AlertView.alert(title: "success".localized(), text: "password_saved".localized(), actions: [action]).show()
                })
            }
        }
        view.endEditing(true)
    }
    
    @IBAction func resendCode(_ sender: Any) {
        startLoading(text: "sending_confirmation_code".localized())
        delegate?.resendCode() { [weak self] error in
            if let error = error {
                self?.show(error: error)
            } else {
                self?.stopLoading(completion: nil)
            }
        }
        view.endEditing(true)
    }
    
    @IBAction func showPass(_ sender: Button) {
        sender.isSelected = !sender.isSelected
        passField.isSecureTextEntry = sender.isSelected == false
    }
}

extension ProfilePassViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let field = textField as? Validatable else { return true }
        let newValue = (textField.text! as NSString).replacingCharacters(in: range, with: string)
        return validator.validate(newValue, type: field.inputType)
    }
}
