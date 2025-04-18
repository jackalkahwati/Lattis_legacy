//
//  LogInViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 22/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import PhoneNumberKit
import Localize_Swift
import RestService

class LogInViewController: SLBaseViewController {
    // MARK: - Outlets
    @IBOutlet weak var emailField: UITextField?
    @IBOutlet weak var passwordField: UITextField!
    @IBOutlet weak var phoneField: PhoneNumberTextField!
    @IBOutlet weak var submitButton: UIButton!
    @IBOutlet weak var emailHint: UILabel?
    @IBOutlet weak var passwordHint: UILabel!
    @IBOutlet weak var phoneHint: UILabel!
    
    var phoneNumber: String?
    var interactor: LogInInteractorInput!
    fileprivate let phoneNumberKit = PhoneNumberKit()
    fileprivate var willCleanupField = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let attributes: [String: Any] = [NSForegroundColorAttributeName: UIColor.slLightBlueGrey]
        emailField?.attributedPlaceholder = NSAttributedString(string: "Email address".localized(), attributes: attributes)
        passwordField.attributedPlaceholder = NSAttributedString(string: "Password".localized(), attributes: attributes)
        phoneField.attributedPlaceholder = NSAttributedString(string: "Mobile number".localized(), attributes: attributes)
        
        emailField?.delegate = self
        passwordField.delegate = self
        phoneField.delegate = self
        
        if let phoneNumber = phoneNumber,
            let phone = try? phoneNumberKit.parse(phoneNumber) {
            let number = "+\(String(phone.countryCode))\(String(phone.nationalNumber))"
            phoneField.text = PartialFormatter().formatPartial(number)
        }
    }
    
    fileprivate var isSubmitEnabled: Bool = false {
        didSet {
            submitButton.backgroundColor = isSubmitEnabled ? .slRobinsEgg : .slPinkishGrey
            submitButton.isEnabled = isSubmitEnabled
            submitButton.alpha = isSubmitEnabled ? 1 : 0.8
        }
    }
    
    // MARK: - Actions
    @IBAction func submitAction(_ sender: Any) {
        let password = passwordField.text!
        let email = emailField?.text
        let isSignUp = emailField != nil
        let regId = UserDefaults.standard.string(forKey: SLUserDefaultsPushNotificationToken)
        guard let countryCode = Locale.current.regionCode else {
            return
        }
        guard let phone = try? phoneNumberKit.parse(phoneField.text!, withRegion: countryCode, ignoreType: true) else {
            return
        }
        let phoneNumber = "+\(String(phone.countryCode))\(String(phone.nationalNumber))"
        let user = Oval.Users.Request(usersId: phoneNumber, regId: regId, userType: .ellipse, phoneNumber: phoneNumber, password: password, isSigningUp: isSignUp, countryCode: countryCode.lowercased(), email: email)
        interactor.logIn(with: user)
    }
    
    @IBAction func closeAction(_ sender: Any) {
        _ = navigationController?.popViewController(animated: true)
    }
    
    @IBAction func forgotPassAction(_ sender: Any) {
        guard let countryCode = Locale.current.regionCode else {
            return
        }
        guard let phone = try? phoneNumberKit.parse(phoneField.text!, withRegion: countryCode, ignoreType: true) else {
            let header = "ERROR".localized()
            let info = "Please enter the phone number that's associated with your account.".localized()
            presentWarning(header: header, info: info)
            return
        }
        let phoneNumber = "+\(String(phone.countryCode))\(String(phone.nationalNumber))"
        presentLoader(with: "Sending verification code".localized())
        Oval.users.forgotPassword(phone: phoneNumber, success: { [weak self] in
            self?.dismissLoader {
                self?.confirm(phoneNumber: phoneNumber)
            }
        }, fail: { [weak self] error in
            self?.dismissLoader {
                let header = "CODE VERIFICATION ERROR".localized()
                let info = "The verification code you have entered is incorrect.".localized()
                self?.presentWarningViewControllerWithTexts(texts: [.Header: header, .Info: info, .CancelButton: "OK".localized()], cancelClosure: {})
            }
        })
    }
    
    private func confirm(phoneNumber: String) {
        LogInRouter(self).presentConfirmation(from: self).configure = { interactor in
            interactor.router.delegate = (self.interactor as? LogInInteractor)?.router.delegate
            if let controller = interactor.view as? SmsConfirmationViewController {
                controller.phoneNumber = phoneNumber
                controller.isPassword = true
            }
        }
    }
    
    @IBAction func loginWithFacebook(_ sender: Any) {
        interactor.loginWithFacebook(in: self)
    }
    
    @IBAction func showPassword(_ sender: UIButton) {
        let visible = passwordField.isSecureTextEntry == false
        passwordField.isSecureTextEntry = visible
        sender.setTitle(visible ? "SHOW".localized() : "HIDE".localized(), for: .normal)
        willCleanupField = passwordField.isSecureTextEntry
    }
    
    @IBAction func phoneChanged(_ sender: PhoneNumberTextField) {
        guard let region = Locale.current.regionCode,
            let phone = try? phoneNumberKit.parse(sender.text!, withRegion: region, ignoreType: true) else { return }
        let number = "+\(String(phone.countryCode))\(String(phone.nationalNumber))"
        sender.text = PartialFormatter().formatPartial(number)
    }
}

extension LogInViewController: LogInInteractorOutput {
    func changeValidation(state: Bool, for type: ValidationValueType) {
        switch type {
        case .phone:
            phoneHint.isHidden = state
        case .email:
            emailHint?.isHidden = state
        case .password:
            passwordHint.isHidden = state
        }
        var valueTypes: [ValidationValueType] = [.password, .phone]
        if emailField != nil {
            valueTypes.append(.email)
        }
        isSubmitEnabled = interactor.checkValidation(of: valueTypes)
    }
}

extension LogInViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        var newValue = (textField.text! as NSString).replacingCharacters(in: range, with: string)
        var type = ValidationValueType.password
        if textField == phoneField {
            type = .phone
        }
        if textField == emailField {
            type = .email
        }
        
        if willCleanupField {
            newValue = ""
            willCleanupField = false
        }
        return interactor.validate(text: newValue, with: type)
    }
}
