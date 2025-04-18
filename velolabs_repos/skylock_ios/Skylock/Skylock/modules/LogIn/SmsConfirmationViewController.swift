//
//  SmsConfirmationViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 22/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import Localize_Swift
import PhoneNumberKit
import RestService

class SmsConfirmationViewController: SLBaseViewController {
    // MARK: - Outlets
    @IBOutlet weak var codeField: UITextField!
    @IBOutlet weak var titleLabel: UILabel!
    
    var interactor: LogInInteractorInput!
    var phoneNumber: String!
    var isPassword = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        codeField.delegate = self
        let kit = PhoneNumberKit()
        guard let phone = try? kit.parse(phoneNumber) else { return }
        titleLabel.text = String(format: "We’ve sent a 6 digit reset code to \n%@.\nPlease enter it now".localized(), kit.format(phone, toType: .international))
        codeField.attributedPlaceholder = NSAttributedString(string: "Enter code".localized(), attributes: [NSForegroundColorAttributeName: UIColor.slLightBlueGrey, NSFontAttributeName: UIFont.systemFont(ofSize: 18)])
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        codeField.becomeFirstResponder()
    }

    // MARK: - Actions
    @IBAction func confirmAction(_ sender: Any) {
        interactor.confirm(code: codeField.text, for: isPassword ? phoneNumber : nil)
        view.endEditing(true)
    }
    @IBAction func closeAction(_ sender: Any) {
        _ = navigationController?.popViewController(animated: true)
    }
    
    @IBAction func resendCode(_ sender: Any) {
        if isPassword {
            presentLoader(with: "Sending verification code".localized())
            Oval.users.forgotPassword(phone: phoneNumber, success: { [weak self] in
                self?.dismissLoader {}
                }, fail: { error in
                    
            })
        } else {
            interactor.getConfirmationCode(for: phoneNumber, needToRoute: false)
        }
    }
    
    fileprivate func validate(text: String) -> Bool {
        let maxLen = 6
        
        return text.characters.count <= maxLen
    }
}

extension SmsConfirmationViewController: LogInInteractorOutput {
    func changeValidation(state: Bool, for type: ValidationValueType) {
        
    }
}

extension SmsConfirmationViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let newValue = (textField.text! as NSString).replacingCharacters(in: range, with: string)
        return validate(text: newValue)
    }
}
