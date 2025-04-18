//
//  TitledInputControl.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 21/12/2018.
//  Copyright © 2018 Lattis. All rights reserved.
//

import UIKit
import Cartography

class TitledInputControl: UIControl {
    var limit: Int = 0
    let textField = UITextField()
    fileprivate let titleLabel = UILabel()
    fileprivate let hintLabel = UILabel()
    fileprivate let bottomLine = UIView()
    
    init(title: String, hint: String? = nil) {
        titleLabel.text = title
        hintLabel.text = hint
        super.init(frame: .zero)
        textField.delegate = self
        configureUI()
    }
    
    var placeholder: String? {
        set {
            guard let str = newValue else { return }
            textField.attributedPlaceholder = NSAttributedString(string: str, attributes: [.font: UIFont.elRegular, .foregroundColor: UIColor.elPinkishGrey])
        }
        get {
            return textField.attributedPlaceholder?.string
        }
    }
    
    var text: String? {
        set {
            textField.text = newValue
        }
        get {
            return textField.text
        }
    }
    
    var title: String? {
        set {
            titleLabel.text = newValue
        }
        get {
            return titleLabel.text
        }
    }
    
    var isInvalid = false {
        didSet {
            if let _ = text {
                textField.textColor = isInvalid ? .elSalmon : .elSlateGrey
            }
            if isInvalid {
                textField.becomeFirstResponder()
            }
            hintLabel.isHidden = !isInvalid || textField.text!.isEmpty
        }
    }
    
    var rightButton: UIButton? {
        set {
            newValue?.sizeToFit()
            textField.rightViewMode = newValue == nil ? .never : .always
            textField.rightView = newValue
        }
        get {
            return textField.rightView as? UIButton
        }
    }
    
    override func becomeFirstResponder() -> Bool {
        return textField.becomeFirstResponder()
    }
    
    fileprivate func configureUI() {
        addSubview(titleLabel)
        addSubview(hintLabel)
        addSubview(textField)
        addSubview(bottomLine)
        
        titleLabel.textColor = .black
        titleLabel.font = .elTitle
        
        hintLabel.textColor = .elSalmon
        hintLabel.font = .elRegular
        hintLabel.isHidden = true
        hintLabel.adjustsFontSizeToFitWidth = true
        hintLabel.minimumScaleFactor = 0.5
        hintLabel.textAlignment = .right
        
        bottomLine.backgroundColor = .elLightBlueGrey
        
        constrain(titleLabel, textField, bottomLine, self, hintLabel) { title, field, line, me, hint in
            title.left == me.left
            title.top == me.top
            hint.right == me.right
            hint.left == title.right + 5
            hint.bottom == title.bottom
            
            field.top == title.bottom
            field.left == me.left
            field.right == me.right
            field.height == 44
            
            line.top == field.bottom
            line.bottom == me.bottom
            line.left == me.left
            line.right == me.right
            line.height == 0.5
        }
        
        textField.font = .elRegular
        textField.textColor = .elSlateGrey
        textField.tintColor = .elDarkSkyBlue
        textField.addTarget(self, action: #selector(textChanged), for: .editingChanged)
    }
    
    @objc fileprivate func textChanged() {
        isInvalid = false
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension TitledInputControl: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard limit > 0,
            let text = (textField.text as NSString?)?.replacingCharacters(in: range, with: string) else { return true}
        return text.count <= limit
    }
}
