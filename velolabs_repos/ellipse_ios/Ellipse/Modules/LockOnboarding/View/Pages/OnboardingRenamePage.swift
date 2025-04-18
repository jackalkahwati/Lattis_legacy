//
//  OnboardingRenamePage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

protocol OnboardingRenamePageDelegate: LockOnboardingPageDelegate {
    func save(name: String)
    func getPlaceholder(completion: (String) -> ())
}

class OnboardingRenamePage: ViewController, LockOnboardingPage {
    
    var style: Style = .onboard
    weak var delegate: OnboardingRenamePageDelegate?
    func set(delegate: Any?) {
        self.delegate = delegate as? OnboardingRenamePageDelegate
        self.delegate?.hideCloseButton()
    }
    
    @objc fileprivate let titleLabel = UILabel()
    @objc fileprivate let nextButton = ValidationButton(type: .custom)
    @objc fileprivate let nameInput = TitledInputControl(title: "lock_name".localized())
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .default
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "lock_name".localized()
        
        view.backgroundColor = .white
        view.addSubview(titleLabel)
        view.addSubview(nameInput)
        view.addSubview(nextButton)
        
        nameInput.textField.delegate = self
        configureLabel(titleLabel)
        smallPositiveStyle(nextButton)
        titleLabel.text = "choose_a_name_for_your_ellipse".localized()
        
        switch style {
        case .edit(let name):
            addBackButton()
            nameInput.placeholder = name
            nextButton.setTitle("save_lock_name".localized(), for: .normal)
        case .onboard:
            nextButton.setTitle("next".localized(), for: .normal)
            delegate?.getPlaceholder { [weak self] placeholder in
                self?.nextButton.isValid = true
                self?.nameInput.placeholder = placeholder
            }
        }
        
        let margin: CGFloat = 20
        constrain(titleLabel, nameInput, nextButton, view) { title, name, next, view in
            title.top == view.safeAreaLayoutGuide.top + margin
            title.left == view.left + margin
            title.right == view.right - margin
            
            name.left == title.left
            name.right == title.right
            name.top == title.bottom + margin
            
            next.centerX == view.centerX
            next.top == name.bottom + margin
        }
        
        nextButton.addTarget(self, action: #selector(next(_:)), for: .touchUpInside)
    }
    
    @objc fileprivate func next(_ sender: Any) {
        view.endEditing(true)
        let name: String
        if let n = nameInput.text, n.isEmpty == false {
            name = n
        } else if let n = nameInput.placeholder, n.isEmpty == false {
            name = n
        } else {
            return
        }
        delegate?.save(name: name)
    }
}

extension OnboardingRenamePage: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let newValue = (textField.text! as NSString).replacingCharacters(in: range, with: string)
        switch style {
        case .edit(let name):
            nextButton.isValid = (newValue.count > 0 || textField.placeholder != nil) && newValue != name
        default:
            nextButton.isValid = newValue.count > 0 || textField.placeholder != nil
        }
        return newValue.count < 41
    }
}

extension OnboardingRenamePage {
    enum Style {
        case onboard
        case edit(String?)
    }
}


fileprivate let configureLabel: (UILabel) -> () = { label in
    label.font = .elTitleLight
    label.textAlignment = .center
    label.textColor = .black
    label.numberOfLines = 0
}
