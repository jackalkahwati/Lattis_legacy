//
//  PasswordResetViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 02/02/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import RestService

class PasswordResetViewController: SLBaseViewController {
    // MARK: - Outlets
    @IBOutlet weak var passwordField: UITextField!
    @IBOutlet weak var hintLabel: UILabel!
    @IBOutlet weak var showHideButton: UIButton!
    
    var saveButton: UIBarButtonItem!
    fileprivate var willCleanupField = false
    
    static func instantiate(with code: String) -> PasswordResetViewController {
        let sb = UIStoryboard(name: "Profile", bundle: nil)
        let controller = sb.instantiateViewController(withIdentifier: "passwordReset") as! PasswordResetViewController
        controller.code = code
        return controller
    }
    
    fileprivate var code: String!
    
    var canSavePassword = false {
        didSet {
            navigationItem.rightBarButtonItem = canSavePassword ? saveButton : nil
            hintLabel.isHidden = canSavePassword
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        addBackButton()
        saveButton = UIBarButtonItem(title: "Save".localized(), style: .done, target: self, action: #selector(saveAction(_:)))
        title = "MY PROFILE".localized()
        passwordField.delegate = self
        passwordField.attributedPlaceholder = NSAttributedString(string: "Password".localized(), attributes: [NSForegroundColorAttributeName: UIColor(white: 1, alpha: 0.7)])
        
        showHideButton.setTitle("SHOW".localized(), for: .normal)
        showHideButton.setTitle("HIDE".localized(), for: .selected)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        passwordField.becomeFirstResponder()
    }
    
    
    // MARK: - Actions
    func saveAction(_ sender: Any) {
        view.endEditing(true)
//        let user = Oval.Users.Request(password: passwordField.text)
        self.presentLoadingViewWithMessage(message: "Saving new password.".localized())
        navigationItem.leftBarButtonItem?.isEnabled = false
        navigationItem.rightBarButtonItem?.isEnabled = false
        Oval.users.update(password: passwordField.text!, with: code, success: { [weak self] in
            self?.dismissLoadingViewWithCompletion(completion: { 
                self?.presentWarningViewControllerWithTexts(texts: [.Header: "Update Successful".localized(),
                                                                    .Info: "Your information has been successfully updated. Tap “OK” to be taken back to the Ellipse Settings".localized(),
                                                                    .CancelButton: "OK".localized()], cancelClosure: { 
                                                                        _ = self?.navigationController?.popToRootViewController(animated: true)
                })
            })
            }, fail: { [weak self] error in
                let header = "Server Error.".localized()
                let info = "Sorry, we were unable to change your password. Please try again.".localized()
                self?.dismissLoadingViewWithCompletion(completion: {
                    self?.presentWarningViewControllerWithTexts(texts: [.Header: header, .Info: info, .CancelButton: "OK".localized()], cancelClosure: {
                        self?.navigationItem.leftBarButtonItem?.isEnabled = true
                        self?.navigationItem.rightBarButtonItem?.isEnabled = true
                    })
                })
        })
//        Oval.users.update(user: user, success: { [weak self] (result) in
//            SLDatabaseManager.shared().save(ovalUser: result, setAsCurrent: true)
//            _ = self?.navigationController?.popViewController(animated: true)
//        }, fail: { [weak self] error in
//            let header = "Server Error.".localized()
//            let info = "Sorry, we were unable to change your password. Please try again.".localized()
//            self?.dismissLoadingViewWithCompletion(completion: {
//                self?.presentWarningViewControllerWithTexts(texts: [.Header: header, .Info: info, .CancelButton: "OK".localized()], cancelClosure: {
//                    self?.navigationItem.leftBarButtonItem?.isEnabled = true
//                    self?.navigationItem.rightBarButtonItem?.isEnabled = true
//                })
//            })
//        })
    }
    
    @IBAction func showHideAction(_ sender: Any) {
        passwordField.isSecureTextEntry = !passwordField.isSecureTextEntry
        willCleanupField = passwordField.isSecureTextEntry
        showHideButton.isSelected = passwordField.isSecureTextEntry == false
    }
}


extension PasswordResetViewController : UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        var newValue = (textField.text! as NSString).replacingCharacters(in: range, with: string)
        if willCleanupField {
            newValue = ""
            willCleanupField = false
        }
        let type = ValidationValueType.password
        canSavePassword = newValue.characters.count >= type.minValue
        return newValue.characters.count <= type.maxValue
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
    
    func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        passwordField.clearsOnBeginEditing = false
        return true
    }
}
