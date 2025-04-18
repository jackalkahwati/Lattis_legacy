//
//  ChangePasswordViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 26/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import RestService

class ChangePasswordViewController: SLBaseViewController {
    // MARK: - Outlets
    @IBOutlet weak var passField: UITextField!
    @IBOutlet weak var closeButtonHeight: NSLayoutConstraint!
    @IBOutlet weak var passwordHint: UILabel!
    @IBOutlet weak var saveButton: UIButton!
    @IBOutlet weak var showHideButton: UIButton!

    var interactor: LogInInteractorInput!
    fileprivate var willCleanupField = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        addBackButton()
        title = "CHANGE PASSWORD".localized()
        closeButtonHeight.constant = (navigationController?.isNavigationBarHidden ?? true) ? 28 : 0
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Done".localized(), style: .done, target: self, action: #selector(doneAction))
        
        passField.attributedPlaceholder = NSAttributedString(string: "Password".localized(), attributes: [NSForegroundColorAttributeName: UIColor.slLightBlueGrey])
        passField.delegate = self
        
        showHideButton.setTitle("SHOW".localized(), for: .normal)
        showHideButton.setTitle("HIDE".localized(), for: .selected)
    }
    
    fileprivate var isSaveEnabled: Bool = false {
        didSet {
            saveButton.backgroundColor = isSaveEnabled ? .slRobinsEgg : .slPinkishGrey
            saveButton.isEnabled = isSaveEnabled
            saveButton.alpha = isSaveEnabled ? 1 : 0.8
        }
    }
    
    // MARK: - Actions
    @IBAction func closeAction(_ sender: Any) {
        _ = navigationController?.popViewController(animated: true)
    }
    
    
    @IBAction func saveAction(_ sender: Any) {
        interactor.update(password: passField.text!)
        view.endEditing(true)
    }
    func doneAction() {
        self.passField.resignFirstResponder()
    }
    
    @IBAction func showHideAction(_ sender: Any) {
        passField.isSecureTextEntry = !passField.isSecureTextEntry
        willCleanupField = passField.isSecureTextEntry
        showHideButton.isSelected = passField.isSecureTextEntry == false
    }
}

extension ChangePasswordViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        var newValue = (textField.text! as NSString).replacingCharacters(in: range, with: string)
        if willCleanupField {
            newValue = ""
            willCleanupField = false
        }
        return interactor.validate(text: newValue, with: .password)
    }
}

extension ChangePasswordViewController: LogInInteractorOutput {
    func changeValidation(state: Bool, for type: ValidationValueType) {
        passwordHint.isHidden = state
        isSaveEnabled = state
    }
    
    func passwordChangingSuccess() {
        presentWarningViewControllerWithTexts(texts: [.Header: "Success!".localized(),
                                                      .Info: "Password changed. You can login using your new password.",
                                                      .CancelButton: "OK".localized()], cancelClosure: {
                                                        _ = self.navigationController?.popViewController(animated: true)
        })
    }
}
