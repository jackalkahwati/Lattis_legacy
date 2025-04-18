//
//  ProfilePasswordProfilePasswordViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import Oval

class ProfilePasswordViewController: ViewController {
    @IBOutlet weak var updateButton: UIButton!
    @IBOutlet weak var repeatField: TextField!
    @IBOutlet weak var newField: TextField!
    @IBOutlet weak var currentField: TextField!
    var interactor: ProfilePasswordInteractorInput!
    
    fileprivate var canUpdate: Bool = false {
        didSet {
            updateButton.isEnabled = canUpdate
            updateButton.backgroundColor = canUpdate ? .lsTurquoiseBlue : .lsSilver
        }
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        title = "profile_change_password_title".localized()
        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
        
        currentField.validation = .password
        newField.validation = .password
        repeatField.validation = .password
        
        currentField.delegate = self
        newField.delegate = self
        repeatField.delegate = self
    }
    
    @objc private func back() {
        _ = navigationController?.popViewController(animated: true)
    }
    
    @IBAction func updatePass(_ sender: Any) {
        view.endEditing(true)
        interactor.submit(password: currentField.text!, newPass: newField.text!)
    }
    
    @IBAction func valueChanged(_ sender: Any) {
        canUpdate = currentField.isValid && newField.isValid && newField.text == repeatField.text
    }
}

extension ProfilePasswordViewController: ProfilePasswordInteractorOutput {
    func success() {
        let alert = ErrorAlertView.alert(title: "profile_password_update_success_title".localized(), subtitle: "profile_password_update_success_text".localized(), button: "general_btn_ok".localized())
        alert.action = {
            _ = self.navigationController?.popToRootViewController(animated: true)
        }
        stopLoading() { alert.show() }
    }
    
    override func show(error: Error, file: String, line: Int) {
        if let error = error as? SessionError, case .unauthorized = error.code {
            Analytics.report(error, file: file, line: line)
            warning(with: "profile_password_wrong_title".localized(), subtitle: "profile_password_wrong_text".localized())
        } else {
            super.show(error: error, file: file, line: line)
        }
    }
}

extension ProfilePasswordViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string), let field = textField as? TextField else { return true }
        return field.validation.canCahnge(to: newText)
    }
}
