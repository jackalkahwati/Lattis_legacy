//
//  VerificationVerificationViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

class VerificationViewController: ViewController {
    @IBOutlet weak var submitButton: UIButton!
    @IBOutlet weak var codeField: TextField!
    var interactor: VerificationInteractorInput!
    
    fileprivate var canSubmit: Bool = false {
        didSet {
            submitButton.isEnabled = canSubmit
            submitButton.alpha = canSubmit ? 1 : 0.5
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        codeField.validation = .limit(6)
        codeField.delegate = self
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    @IBAction func submit(_ sender: Any) {
        view.endEditing(true)
        interactor.verify(with: codeField.text!)
    }
    
    @IBAction func codeChanged(_ sender: Any) {
        canSubmit = codeField.isValid
    }
    
    @IBAction func back(_ sender: Any) {
        _ = navigationController?.popViewController(animated: true)
    }
}

extension VerificationViewController: VerificationInteractorOutput {

}

extension VerificationViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string), let field = textField as? TextField else { return true }
        return field.validation.canCahnge(to: newText)
    }
}
