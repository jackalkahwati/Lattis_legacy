//
//  ProfileEditProfileEditViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import LGSideMenuController

class ProfileEditViewController: ViewController {
    @IBOutlet weak var noteLabel: UILabel!
    @IBOutlet weak var textField: TextField!
    @IBOutlet weak var submitButton: UIButton!
    var interactor: ProfileEditInteractorInput!
    fileprivate var originalValue: String? {
        didSet {
            textField.cleanText = originalValue
        }
    }

    fileprivate var canSubmit: Bool = false {
        didSet {
            submitButton.isEnabled = canSubmit
            submitButton.backgroundColor = canSubmit ? .lsTurquoiseBlue : .lsSilver
        }
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        textField.delegate = self
        interactor.viewLoaded()
        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
    }
    
    @objc private func back() {
        _ = navigationController?.popViewController(animated: true)
    }
    
    @IBAction func submitAction(_ sender: Any) {
        view.endEditing(true)
        interactor.submit(value: textField.cleanText!)
    }
    
    @IBAction func valueChanged(_ sender: Any) {
        canSubmit = textField.isValid && textField.cleanText != originalValue
    }
}

extension ProfileEditViewController: ProfileEditInteractorOutput {
    func show(info: ProfileInfoModel) {
        textField.validation = info.type.validationType
        textField.keyboardType = info.keyboard
        guard let style = info.type.style else { return }
        title = style.title.localized()
        noteLabel.text = style.note.localized()
        textField.placeholder = style.placeholder.localized()
        originalValue = info.value
        submitButton.setTitle(style.button.localized(), for: .normal)
    }
}

extension ProfileEditViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string), let field = textField as? TextField else { return true }
        if let text = field.validation.replace(text: newText) {
            textField.text = text
            canSubmit = field.isValid && field.cleanText != originalValue
            return false
        }
        return field.validation.canCahnge(to: newText)
    }
}

struct ProfileEditStyle {
    let title: String
    let note: String
    let button: String
    let placeholder: String
}

extension ProfileInfoType {
    var style: ProfileEditStyle? {
        switch self {
        case .name:
            return ProfileEditStyle(title: "profile_change_name_title", note: "profile_change_name_note", button: "profile_change_name_button", placeholder: "profile_change_name_placeholder")
        case .lastName:
            return ProfileEditStyle(title: "profile_change_last_name_title", note: "profile_change_last_name_note", button: "profile_change_last_name_button", placeholder: "profile_change_last_name_placeholder")
        case .email:
            return ProfileEditStyle(title: "profile_change_email_title", note: "profile_change_email_note", button: "profile_change_email_button", placeholder: "general_email")
        case .phone:
            return ProfileEditStyle(title: "profile_change_phone_title", note: "profile_change_phone_note", button: "profile_change_phone_button", placeholder: "profile_change_phone_placeholder")
        case .privateNetworks:
            return ProfileEditStyle(title: "profile_change_networks_title", note: "profile_change_networks_note", button: "profile_change_networks_button", placeholder: "general_email")
        case .verification:
            return ProfileEditStyle(title: "profile_change_email_title", note: "profile_change_verification_note", button: "profile_change_verification_button", placeholder: "profile_change_verification_placeholder")
        default:
            return nil
        }
    }
    
    var validationType: ValidationType {
        switch self {
        case .verification:
            return .limit(6)
        case .email, .privateNetworks:
            return .email
        case .phone:
            return .phone
        default:
            return .notEmpty
        }
    }
}
