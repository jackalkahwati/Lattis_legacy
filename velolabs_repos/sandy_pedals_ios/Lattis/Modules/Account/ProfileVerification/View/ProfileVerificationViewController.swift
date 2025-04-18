//
//  ProfileVerificationProfileVerificationViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

class ProfileVerificationViewController: ViewController {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var codeField: TextField!
    @IBOutlet weak var submitButton: UIButton!
    var interactor: ProfileVerificationInteractorInput!
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        title = "profile_verification_title".localized()
        
        codeField.validation = .limit(6)
        codeField.delegate = self
        
        interactor.viewLoaded()
        
        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
    }
    
    @objc private func back() {
        _ = navigationController?.popViewController(animated: true)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        codeField.becomeFirstResponder()
    }
    
    fileprivate var canSubmit: Bool = false {
        didSet {
            submitButton.isEnabled = canSubmit
            submitButton.backgroundColor = canSubmit ? .lsTurquoiseBlue : .lsSilver
        }
    }
    
    @IBAction func codeChanged(_ sender: Any) {
        canSubmit = codeField.isValid
    }
    
    @IBAction func submit(_ sender: Any) {
        view.endEditing(true)
        interactor.submit(code: codeField.text!)
    }
    
    @IBAction func resend(_ sender: Any) {
        view.endEditing(true)
        interactor.resendCode()
    }
}

extension ProfileVerificationViewController: ProfileVerificationInteractorOutput {
    func show(title: String) {
        titleLabel.text = title
    }
    
    func focusOnCode() {
        codeField.becomeFirstResponder()
        codeField.text = ""
    }
}

extension ProfileVerificationViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string), let field = textField as? TextField else { return true }
        return field.validation.canCahnge(to: newText)
    }
}
