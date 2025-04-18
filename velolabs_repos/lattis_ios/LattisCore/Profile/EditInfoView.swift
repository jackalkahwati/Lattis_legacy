//
//  EditInfoView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 10/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class EditInfoView: UIView {
    
    var info: EditInfo! {
        didSet {
            updateInfo()
        }
    }
    
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .medium, size: .small))
    let textField: TextFieldView
    
    init(_ textField: TextFieldView) {
        self.textField = textField
        super.init(frame: .zero)
        
//        addSubview(titleLabel)
        addSubview(textField)
        
        constrain(titleLabel, textField, self) { title, field, view in
//            title.top == view.top + .margin/2
//            title.left == view.left + .margin
//            title.right == view.right - .margin
            
//            field.bottom == view.bottom - .margin/2
//            field.left == view.left + .margin
//            field.right == view.right - .margin
            
//            view.height == 68
            field.edges == view.edges.inseted(horizontally: .margin)
        }
    }
    
    var value: String? {
        set {
            textField.text = newValue?.trimmingCharacters(in: .whitespacesAndNewlines)
        }
        get {
            return textField.text?.trimmingCharacters(in: .whitespacesAndNewlines)
        }
    }
    
    func triggerHandler() {
        //TODO: handle return/fail case
        guard let v = value else { return }
        info.handler(v)
    }
    
    fileprivate func updateInfo() {
//        titleLabel.text = info.title
        
        textField.text = info.value
        if let place = info.placeholder {
            textField.field.attributedPlaceholder = .init(string: place, attributes: [.font: UIFont.theme(weight: .medium, size: .body), .foregroundColor: UIColor.lightGray])
        } else {
            textField.field.attributedPlaceholder = nil
        }
//        textField.field.keyboardType = info.keyboardType
//        textField.field.textContentType = info.contentType
//        textField.field.isSecureTextEntry = info.isSecure
//        textField.field.autocapitalizationType = info.capitalizationType
//        if info.isSecure && textField.isUserInteractionEnabled {
//            let button = UIButton(type: .custom)
//            button.frame = .init(x: 0, y: 0, width: 30, height: 20)
//            button.setImage(.named("icon_pass_show"), for: .normal)
//            button.setImage(.named("icon_pass_hide"), for: .selected)
//            button.tintColor = .neonBlue
//            button.addTarget(self, action: #selector(swithcSecureState(_:)), for: .touchUpInside)
//            textField.rightView = button
//            textField.rightViewMode = .always
//        } else {
//            textField.rightViewMode = .never
//            textField.rightView = nil
//        }
    }
    
    @discardableResult
    override func becomeFirstResponder() -> Bool {
        return textField.field.becomeFirstResponder()
    }
    
    @discardableResult
    override func resignFirstResponder() -> Bool {
        return textField.field.resignFirstResponder()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc
    fileprivate func swithcSecureState(_ sender: UIButton) {
        textField.field.isSecureTextEntry = !textField.field.isSecureTextEntry
        sender.isSelected = !sender.isSelected
    }
}
