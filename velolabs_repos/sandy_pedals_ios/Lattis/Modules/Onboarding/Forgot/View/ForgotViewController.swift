//
//  ForgotForgotViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import Oval

class ForgotViewController: ViewController {
    @IBOutlet weak var submitButton: UIButton!
    @IBOutlet weak var emailField: TextField!
    var interactor: ForgotInteractorInput!

    override func viewDidLoad() {
        super.viewDidLoad()

        emailField.delegate = self
        emailField.validation = .email
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        emailField.becomeFirstResponder()
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    fileprivate var canSubmit: Bool = false {
        didSet {
            submitButton.isEnabled = canSubmit
            submitButton.alpha = canSubmit ? 1 : 0.5
        }
    }
    
    @IBAction func submit(_ sender: Any) {
        view.endEditing(true)
        interactor.submit(email: emailField.text!)
    }
    
    @IBAction func emailChanged(_ sender: Any) {
        canSubmit = emailField.isValid
    }
    
    @IBAction func back(_ sender: Any) {
        _ = navigationController?.popViewController(animated: true)
    }
    
    override func show(error: Error, file: String, line: Int) {
        Analytics.report(error, file: file, line: line)
        var title = "general_error_title".localized()
        var text = "general_error_text".localized()
        if let error = error as? SessionError {
            switch error.code {
            case .resourceNotFound:
                title = "login_error_wrong_email_title".localized()
                text = "login_error_wrong_email_text".localized()
            default:
                break
            }
        }
        warning(with: title, subtitle: text)
    }
}

extension ForgotViewController: ForgotInteractorOutput {
    func show(email: String) {}
    func showSuccess() {}
}

extension ForgotViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string), let field = textField as? TextField else { return true }
        return field.validation.canCahnge(to: newText)
    }
}
