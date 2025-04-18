//
//  ProfileConfirmationViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 03/03/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import PhoneNumberKit
import RestService

class ProfileConfirmationViewController: SLBaseViewController {
    @IBOutlet weak var sendButton: UIButton!
    @IBOutlet weak var resendButton: UIButton!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var codeField: UITextField!
    
    var nextStep: (ProfileConfirmationViewController) -> Void = { _ in }
    
    fileprivate(set) var phoneNumber: String!
    fileprivate var state: State! { didSet { updateState() } }
    
    class func instantiate(with phoneNumber: String? = nil) -> ProfileConfirmationViewController {
        let controller = UIStoryboard(name: "Profile", bundle: nil).instantiateViewController(withIdentifier: "confirmation") as! ProfileConfirmationViewController
        if let number = phoneNumber {
            controller.phoneNumber = number
        }
        return controller
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "MY PROFILE".localized()
        addBackButton()
        codeField.delegate = self
        codeField.attributedPlaceholder = NSAttributedString(string: "Enter code".localized(), attributes: [NSForegroundColorAttributeName: UIColor(white: 1, alpha: 0.7), NSFontAttributeName: UIFont.systemFont(ofSize: 18)])
        
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Submit".localized(), style: .done, target: self, action: #selector(submitCode))
        navigationItem.rightBarButtonItem?.isEnabled = false
        
        state = phoneNumber == nil ? .warning : .phone
        if state == .phone {
            resendCode(self)
        }
    }
    
    private func updateState() {
        if state == .warning {
            sendButton.isHidden = false
            fetchPhoneNumber()
            titleLabel.text = "In order to verify your account, we'll need to send 6 digit reset code to the registered phone number".localized()
        } else {
            let kit = PhoneNumberKit()
            guard let phone = try? kit.parse(phoneNumber) else { return }
            titleLabel.text = String(format: "We’ve sent a 6 digit reset code to \n%@.\nPlease enter it now".localized(), kit.format(phone, toType: .international))
        }
        
        UIView.animate(withDuration: 0.3, animations: { 
            self.codeField.alpha = self.state != .warning ? 1 : 0
            self.sendButton.alpha = self.state != .warning ? 0 : 1
            self.resendButton.alpha = self.state != .warning ? 1 : 0
            self.view.layoutIfNeeded()
        }, completion: { _ in
            self.sendButton.isHidden = self.state != .warning
        })
    }
    
    private func fetchPhoneNumber() {
        guard let phoneNumber = SLDatabaseManager.shared().getCurrentUser()?.phoneNumber else { return }
        self.phoneNumber = phoneNumber
    }
    
    @IBAction func sendCodeAction(_ sender: Any) {
        state = .password
        resendCode(sender)
    }
    
    @IBAction func resendCode(_ sender: Any) {
        view.endEditing(true)
        codeField.text = nil
        navigationItem.rightBarButtonItem?.isEnabled = false
        presentLoadingViewWithMessage(message: "Sending verification code...".localized())
        if state == .phone {
            Oval.users.getUpdateCode(for: phoneNumber, success: { [weak self] in
                self?.dismissLoadingViewWithCompletion(completion: { 
                    self?.codeField.becomeFirstResponder()
                })
                }, fail: { [weak self] error in
                    self?.dismissLoadingViewWithCompletion(completion: {
                        self?.handle(error: error)
                    })
            })
        } else {
            Oval.users.getUpdatePasswordCode(success: { [weak self] in
                self?.dismissLoadingViewWithCompletion(completion: {
                    self?.codeField.becomeFirstResponder()
                })
            }, fail: { [weak self] error in
                self?.dismissLoadingViewWithCompletion(completion: {
                    self?.handle(error: error)
                })
            })
        }
    }
    
    @objc private func submitCode() {
        view.endEditing(true)
        
        presentLoadingViewWithMessage(message: "Checking code...".localized())
        if state == .phone {
            navigationItem.rightBarButtonItem?.isEnabled = false
            navigationItem.leftBarButtonItem?.isEnabled = false
            Oval.users.update(phoneNumber: phoneNumber, with: codeField.text!, success: {  [weak self] in
                self?.dismissLoadingViewWithCompletion(completion: {
                    self?.nextStep(self!)
                    self?.navigationItem.leftBarButtonItem?.isEnabled = true
                    self?.navigationItem.rightBarButtonItem?.isEnabled = true
                })
                }, fail: { [weak self] error in
                    self?.dismissLoadingViewWithCompletion(completion: {
                        self?.handle(error: error)
                    })
            })
        } else {
            nextStep(self)
        }
    }
    
    private func handle(error: Error) {
        navigationItem.leftBarButtonItem?.isEnabled = true
        let header = "CODE VERIFICATION ERROR".localized()
        let info = "The verification code you have entered is incorrect.".localized()
        presentWarningViewControllerWithTexts(texts: [.Header: header, .Info: info, .CancelButton: "OK".localized()], cancelClosure: {})
    }
    
    fileprivate func validate(text: String) -> Bool {
        let maxLen = 6
        navigationItem.rightBarButtonItem?.isEnabled = text.characters.count >= maxLen
        return text.characters.count <= maxLen
    }
    
    override func warningVCTakeActionButtonPressed(wvc: SLWarningViewController) {
        _ = navigationController?.popToRootViewController(animated: true)
    }
}

extension ProfileConfirmationViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let newValue = (textField.text! as NSString).replacingCharacters(in: range, with: string)
        return validate(text: newValue)
    }
}

extension ProfileConfirmationViewController {
    enum State {
        case warning, phone, password
    }
}

