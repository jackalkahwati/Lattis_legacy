//
//  LockNameResetViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 20/02/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import Localize_Swift

class LockNameResetViewController: SLBaseViewController {
    @IBOutlet weak var nameField: UITextField!
    
    static var storyboard: LockNameResetViewController {
        return UIStoryboard(name: "Locks", bundle: nil).instantiateViewController(withIdentifier: "lockName") as! LockNameResetViewController
    }
    
    var lock: SLLock!
    private let locksService = LocksService()
    private var saveButton: UIBarButtonItem!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        addBackButton()
        title = "LOCK NAME".localized()
        saveButton = UIBarButtonItem(title: "Save".localized(), style: .done, target: self, action: #selector(save(_:)))
        nameField.delegate = self
        nameField.attributedPlaceholder = NSAttributedString(string: lock.displayName, attributes: [NSForegroundColorAttributeName: UIColor(white: 1, alpha: 0.7)])
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        nameField.becomeFirstResponder()
    }
    
    @objc private func save(_ sender: Any) {
        navigationItem.leftBarButtonItem?.isEnabled = false
        navigationItem.rightBarButtonItem?.isEnabled = false
        presentLoadingViewWithMessage(message: "Changing lock name.".localized())
        nameField.resignFirstResponder()
        locksService.changeName(forLockWith: lock.lockId, name: self.nameField.text!, success: { [weak self] in
            guard let `self` = self else { return }
            NotificationCenter.default.post(
                name: NSNotification.Name(rawValue: kSLNotificationLockNameChanged),
                object: self.lock
            )
            _ = self.navigationController?.popViewController(animated: true)
        }, fail: { [weak self] (error) in
            guard let `self` = self else { return }
            let texts:[SLWarningViewControllerTextProperty:String?] = [
                .Header: NSLocalizedString("IMPORTANT", comment: ""),
                .Info: NSLocalizedString(
                    "The name of your Ellipse could not be updated right now.",
                    comment: ""
                ),
                .CancelButton: NSLocalizedString("OK", comment: ""),
                .ActionButton: nil
            ]
            self.dismissLoadingViewWithCompletion(completion: { 
                self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: {
                     _ = self.navigationController?.popViewController(animated: true)
                })
            })
        })
    }
    
    fileprivate func validate(name: String?) {
        navigationItem.rightBarButtonItem = name == nil || name!.isEmpty ? nil : saveButton
    }
}

extension LockNameResetViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let text = (textField.text as NSString?)?.replacingCharacters(in: range, with: string)
        validate(name: text)
        return true
    }
}
