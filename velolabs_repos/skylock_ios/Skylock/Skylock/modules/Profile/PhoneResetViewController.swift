//
//  PhoneResetViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 31/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import Localize_Swift
import PhoneNumberKit
import RestService

class PhoneResetViewController: SLBaseViewController {
    // MARK: - Outlets
    @IBOutlet weak var phoneField: PhoneNumberTextField!
    
    let phoneNumberKit = PhoneNumberKit()
    var saveButton: UIBarButtonItem!
    static var storyboard: PhoneResetViewController {
        let sb = UIStoryboard(name: "Profile", bundle: nil)
        return sb.instantiateViewController(withIdentifier: "phoneReset") as! PhoneResetViewController
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        addBackButton()
        saveButton = UIBarButtonItem(title: "Save".localized(), style: .done, target: self, action: #selector(saveAction(_:)))
        title = "MY PROFILE".localized()
        phoneField.delegate = self
        phoneField.attributedPlaceholder = NSAttributedString(string: "Phone number".localized(), attributes: [NSForegroundColorAttributeName: UIColor(white: 1, alpha: 0.7)])
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        phoneField.becomeFirstResponder()
    }
    
    // MARK: - Actions
    @IBAction func saveAction(_ sender: Any) {
        
        guard let countryCode = Locale.current.regionCode,
            let phone = try? phoneNumberKit.parse(phoneField.text!, withRegion: countryCode, ignoreType: true) else {
            saveButton.isEnabled = false
            return
        }
        let phoneNumber = "+\(String(phone.countryCode))\(String(phone.nationalNumber))"
        let controller = ProfileConfirmationViewController.instantiate(with: phoneNumber)
        navigationController?.pushViewController(controller, animated: true)
        controller.nextStep = didResetPhoneNumber(confirmationController:)
    }
    
    private func didResetPhoneNumber(confirmationController: ProfileConfirmationViewController) {
        if let user = SLDatabaseManager.shared().getCurrentUser() {
            user.phoneNumber = confirmationController.phoneNumber
            SLDatabaseManager.shared().save(user, withCompletion: nil)
        }
        confirmationController.navigationItem.leftBarButtonItem?.isEnabled = false
        confirmationController.presentWarningViewControllerWithTexts(texts: [.Header: "Update Successful".localized(),
                                                                             .Info: "Your information has been successfully updated. Tap “OK” to be taken back to the Ellipse Settings".localized(),
                                                                             .CancelButton: "OK".localized()], cancelClosure: { [unowned confirmationController] in
                                                                                _ = confirmationController.navigationController?.popToRootViewController(animated: true)
        })
    }
    
    @IBAction func phoneChanged(_ sender: PhoneNumberTextField) {
        guard let region = Locale.current.regionCode,
            let phone = try? phoneNumberKit.parse(sender.text!, withRegion: region, ignoreType: true) else { return }
        let number = "+\(String(phone.countryCode))\(String(phone.nationalNumber))"
        sender.text = PartialFormatter().formatPartial(number)
    }
}

extension PhoneResetViewController : UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let newValue = (textField.text! as NSString).replacingCharacters(in: range, with: string)
        let type = ValidationValueType.phone
        var isValid = false
        if let region = Locale.current.regionCode,
            let _ = try? phoneNumberKit.parse(newValue, withRegion: region, ignoreType: true) {
            isValid = newValue.characters.count >= type.minValue
        }
        navigationItem.rightBarButtonItem = isValid ? saveButton : nil
        return newValue.characters.count <= type.maxValue
    }
}
