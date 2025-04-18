//
//  PasswordUpdateViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 10/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class PasswordUpdateViewController: EditViewController {
    typealias Handler = (String, String, PasswordUpdateViewController) -> ()
    
    fileprivate let oldPassFieldView = TextFieldView.password()
    fileprivate let passFieldView = TextFieldView.password(isNew: true)
    fileprivate let warningLabel = UILabel.label(font: .theme(weight: .medium, size: .small), color: .warning, allignment: .center, lines: 0)
    fileprivate let email: String
    fileprivate let handler: Handler
    fileprivate let info: EditInfo = .password(handler: {_ in})
    
    init(_ email: String, handler: @escaping Handler) {
        self.email = email
        self.handler = handler
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        contentView.insertArrangedSubview(oldPassFieldView, at: 1)
        contentView.insertArrangedSubview(passFieldView, at: 2)
        contentView.insertArrangedSubview(warningLabel, at: 3)
        actionContainer.update(
            left: .plain(title: "cancel".localized(), style: .plain, handler: close),
            right: .plain(title: "submit".localized(), handler: { [unowned self] in
                self.handleAction()
            }),
            priority: .right
        )
        
        oldPassFieldView.field.delegate = self
        passFieldView.field.delegate = self
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        _ = oldPassFieldView.field.becomeFirstResponder()
    }
    
    fileprivate func show(warning: String, field: TextFieldView) {
        field.isFailed = true
        warningLabel.text = warning
    }
    
    fileprivate func hideWarning() {
        oldPassFieldView.isFailed = false
        passFieldView.isFailed = false
        warningLabel.text = nil
    }
    
    fileprivate func handleAction() {
        guard let code = oldPassFieldView.text, let pass = passFieldView.text else { return }
        if !info.validate(code).1, let warning = info.warning {
            return show(warning: warning, field: oldPassFieldView)
        }
        if !info.validate(pass).1, let warning = info.warning {
            return show(warning: warning, field: passFieldView)
        }
        handler(code, pass, self)
    }
    
    override func startSaving() {
        super.startSaving()
        
        view.endEditing(true)
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let oldString = (textField.text as NSString?) else { return true }
        let newString = oldString.replacingCharacters(in: range, with: string)
        hideWarning()
        let (replace, _) = info.validate(newString)
        if let r = replace {
            textField.text = r
            return false
        }
        return true
    }
}
