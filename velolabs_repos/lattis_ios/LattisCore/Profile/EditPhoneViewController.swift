//
//  EditPhoneViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 01.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import FlagPhoneNumber
import OvalAPI

final class EditPhoneViewController: EditViewController {
    
    fileprivate let callback: (String) -> ()
    fileprivate let oldPhone: String?
    fileprivate let textView = FPNTextField()
    fileprivate var isPhoneNumberValid = false
    fileprivate let warningLabel = UILabel.label(text: "duplicated_phone_number".localized(), font: .theme(weight: .medium, size: .small), color: .warning, allignment: .center, lines: 0)
    
    init(phone: String?, callback: @escaping (String) -> ()) {
        self.oldPhone = phone
        self.callback = callback
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        textView.textColor = .black
        textView.delegate = self
        let reg = Locale.current.regionCode ?? "US"
        if let code = FPNCountryCode(rawValue: reg) {
            textView.setFlag(countryCode: code)
        }
        
        infoLabel.text = "phone_update_note".localized()
        contentView.insertArrangedSubview(textView, at: 1)
        contentView.insertArrangedSubview(warningLabel, at: 2)
        warningLabel.isHidden = true
        actionContainer.update(left: .plain(title: "cancel".localized(), style: .plain, handler: close), right: .plain(title: "save".localized(), handler: { [unowned self] in
            self.handleAction()
        }), priority: .right)
        textView.becomeFirstResponder()
    }
    
    @objc fileprivate func handleAction() {
        guard let phone = textView.getFormattedPhoneNumber(format: .E164),
        isPhoneNumberValid,
        phone != oldPhone
        else {
            warningLabel.isHidden = false
            return
        }
        callback(phone)
    }
    
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        if let e = error as? SessionError, e.code == .conflict {
            warningLabel.isHidden = false
            return
        }
        super.handle(error, from: viewController, retryHandler: retryHandler)
    }
}

extension EditPhoneViewController: FPNTextFieldDelegate {
    
    func fpnDidSelectCountry(name: String, dialCode: String, code: String) {}
    
    func fpnDidValidatePhoneNumber(textField: FPNTextField, isValid: Bool) {
        warningLabel.isHidden = true
        isPhoneNumberValid = isValid
    }

    func fpnDisplayCountryList() {}
}
