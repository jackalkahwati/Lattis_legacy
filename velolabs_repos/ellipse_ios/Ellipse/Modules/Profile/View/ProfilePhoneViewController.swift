//
//  ProfilePhoneViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/10/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import PhoneNumberKit

protocol ProfilePhoneDelegate: class {
    func sendCode(to phoneNumber: String, completion: @escaping (Error?) -> ())
    func confirm(phoneNumber: String, with code: String, completion: @escaping (Error?) -> ())
    func save(phoneNumber: String)
}

class ProfilePhoneViewController: ViewController {
    @IBOutlet weak var resendButton: Button!
    @IBOutlet weak var saveButton: PhoneValidationButton!
    @IBOutlet weak var codeHintLabel: Label!
    @IBOutlet weak var phoneHintLabel: Label!
    @IBOutlet weak var codeField: ValidatableTextField!
    @IBOutlet weak var phoneField: PhoneNumberTextField!
    
    weak var delegate: ProfilePhoneDelegate?
    var currentNumber: String! {
        didSet {
            validator = PhoneInputValidator(currentNumber)
        }
    }
    fileprivate var validator: PhoneInputValidator!
    fileprivate let phoneNumberKit = PhoneNumberKit()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "myprofile".localized()
        addBackButton()
        codeField.delegate = self
        phoneField.delegate = self
        codeField.inputType = .code
        codeField.localizePlaceholder()
        phoneField.placeholder = currentNumber
        
        validator.handle = { [unowned self] type, valid, isValid in
            switch type {
            case .code:
                self.codeHintLabel.isHidden = valid
            case .phone:
                self.phoneHintLabel.isHidden = valid
            default:
                break
            }
            self.saveButton.isValid = isValid
        }
        validator.handlePhone = { [unowned self] type, show in
            self.saveButton.pushType = type
            self.resendButton.isHidden = !show
            self.codeField.superview?.isUserInteractionEnabled = show
            self.codeField.superview?.alpha = show ? 1 : 0.5
        }
    }
    
    @IBAction fileprivate func savePhone(_ sender: PhoneValidationButton) {
        switch sender.pushType {
        case .code:
            startLoading(text: "sending_confirmation_code".localized())
            delegate?.sendCode(to: validator.currentPhone) { [weak self] error in
                if let error = error {
                    self?.show(error: error)
                } else {
                    self?.stopLoading(completion: nil)
                    self?.validator.saveLatest()
                }
            }
        case .save:
            startLoading(text: "saving_phone_number".localized())
            delegate?.confirm(phoneNumber: validator.currentPhone, with: codeField.text!) { [weak self] error in
                if let error = error {
                    self?.show(error: error)
                } else {
                    let action = AlertView.Action(title: "ok".localized(), handler: { (_) in
                        self?.delegate?.save(phoneNumber: self!.validator.currentPhone)
                    })
                    self?.stopLoading() {
                        AlertView.alert(title: self?.validator.currentPhone, text: "phone_saved".localized(), actions: [action]).show()
                    }
                }
            }
        }
        view.endEditing(true)
    }
    
    @IBAction func resendCode(_ sender: Any) {
        delegate?.sendCode(to: phoneField.text!.trimmedPhoneNumber) { [weak self] error in
            if let error = error {
                self?.show(error: error)
            } else {
                self?.stopLoading(completion: nil)
            }
        }
        view.endEditing(true)
    }
    
    @IBAction func phoneChanged(_ sender: PhoneNumberTextField) {
        guard let region = Locale.current.regionCode,
            let phone = try? phoneNumberKit.parse(sender.text!, withRegion: region, ignoreType: true) else { return }
        let number = "+\(String(phone.countryCode))\(String(phone.nationalNumber))"
        sender.text = PartialFormatter().formatPartial(number)
        _ = validator.validate(number, type: .phone)
    }
}

extension ProfilePhoneViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let field = textField as? Validatable, field.inputType == .code else { return true }
        let newValue = (textField.text! as NSString).replacingCharacters(in: range, with: string)
        return validator.validate(newValue, type: field.inputType)
    }
}

private class PhoneInputValidator: InputValidator {
    var handlePhone: (PhoneValidationButton.Title, Bool) -> () = {_, _ in}
    let originalPhone: String
    var currentPhone = ""
    var latestPhone = ""
    init(_ phoneNumber: String) {
        self.originalPhone = phoneNumber
        super.init([.phone])
    }
    
    override func validate(_ text: String, type: InputValidator.InputType) -> Bool {
        let should = super.validate(text, type: type)
        guard type == .phone else { return should }
        currentPhone = text.trimmedPhoneNumber
        if currentPhone == originalPhone {
            handlePhone(.code, false)
            handle?(.password, false, false)
        } else if currentPhone == latestPhone {
            handlePhone(.save, true)
            types = [.code, .phone]
            _ = super.validate("", type: .password)
        } else {
            handlePhone(.code, false)
            types = [.phone]
            _ = super.validate("", type: .password)
        }
        return should
    }
    
    func saveLatest() {
        latestPhone = currentPhone
        handlePhone(.save, true)
        types = [.code, .phone]
        _ = validate("", type: .password)
    }
}

class PhoneValidationButton: ValidationButton {
    enum Title: String {
        case code = "text_verIfication_code", save = "save_new_phone"
    }
    
    var pushType: Title = .code {
        didSet {
            self.setTitle(pushType.rawValue.localized(), for: .normal)
        }
    }
}

