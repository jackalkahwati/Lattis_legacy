//
//  TextFieldView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 04.03.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography


final class TextFieldView: UIView {
    let field = UITextField()
    var icon: UIImage { didSet { layout() } }
    fileprivate let action: (UITextField, UIButton) -> ()
    
    init(_ icon: UIImage, actionButton: UIButton? = nil, action: @escaping (UITextField, UIButton) -> () = {_, _ in}) {
        self.icon = icon
        self.action = action
        super.init(frame: .zero)
        field.leftViewMode = .always
        field.tintColor = .black
        field.textColor = .black
        if let button = actionButton {
            field.rightViewMode = .always
            field.rightView = button
            button.addTarget(self, action: #selector(performAction(_:)), for: .touchUpInside)
        }
        let line = UIView()
        line.backgroundColor = UIColor(white: 0.8, alpha: 1)
        addSubview(field)
        addSubview(line)
        constrain(field, line, self) { field, line, view in
            line.bottom == view.bottom
            line.height == 1
            line.left == view.left
            line.right == view.right
            view.height == 44
            field.left == view.left
            field.right == view.right
            field.top == view.top
            field.bottom == line.top
        }
        layout()
    }
    
    var text: String? {
        get {
            field.text?.trimmingCharacters(in: .whitespaces)
        }
        set {
            field.text = newValue
        }
    }
    
    var isFailed: Bool = false {
        didSet {
            guard isFailed != oldValue else { return }
            field.textColor = isFailed ? .warning : .black
            guard let imageView = field.leftView as? UIImageView, let color = field.textColor else { return }
            imageView.tintColor = color
        }
    }
    
    func add(button: UIButton) {
        field.rightViewMode = .always
        let size = button.sizeThatFits(.init(width: 300, height: 300))
        constrain(button) {
            $0.width == size.width + .margin/2
            $0.height == size.height
        }
        field.rightView = button
        button.contentHorizontalAlignment = .trailing
    }
    
    fileprivate func layout() {
        let view = UIImageView(image: icon.withRenderingMode(.alwaysTemplate))
        view.contentMode = .left
        view.tintColor = .black
        constrain(view) {
            $0.width == icon.size.width + .margin/2
            $0.height == 44
        }
        field.leftView = view
    }
    
    
    @objc
    fileprivate func performAction(_ sender: UIButton) {
        action(field, sender)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension TextFieldView {
    static var email: TextFieldView {
        let view = TextFieldView(.named("icon_email")!)
        view.field.keyboardType = .emailAddress
        view.field.textContentType = .emailAddress
        view.field.autocorrectionType = .no
        view.field.autocapitalizationType = .none
        view.field.returnKeyType = .done
        view.field.placeholder = "email".localized()
        return view
    }
    
    static var phone: TextFieldView {
        let view = TextFieldView(.named("icon_phone")!)
        view.field.keyboardType = .phonePad
        view.field.textContentType = .telephoneNumber
        view.field.autocorrectionType = .no
        view.field.autocapitalizationType = .none
        view.field.returnKeyType = .done
        return view
    }
    
    static func password(isNew: Bool = false) -> TextFieldView {
        let visible = UIButton(type: .custom)
        let icon: UIImage = .named("icon_pass_show")!
        constrain(visible) {
            $0.height == icon.size.height
            $0.width == icon.size.width + .margin/2
        }
        visible.contentHorizontalAlignment = .trailing
        visible.setImage(icon, for: .normal)
        visible.setImage(.named("icon_pass_show"), for: .selected)
        let view = TextFieldView(.named("icon_password")!, actionButton: visible) { field, button in
            button.isSelected = !button.isSelected
            field.isSecureTextEntry = !button.isSelected
        }
        view.field.autocapitalizationType = .none
        view.field.keyboardType = .emailAddress
        view.field.returnKeyType = .done
        view.field.autocorrectionType = .no
        if #available(iOS 12.0, *) {
            view.field.textContentType = isNew ? .newPassword : .password
        } else {
            view.field.textContentType = .password
        }
        view.field.isSecureTextEntry = true
        view.field.placeholder = isNew ? "enter_new_password".localized() : "password".localized()
        return view
    }
    
    static var firstName: TextFieldView {
        let view = TextFieldView(.named("icon_person")!)
        view.field.keyboardType = .alphabet
        view.field.textContentType = .givenName
        view.field.autocapitalizationType = .words
        view.field.returnKeyType = .done
        view.field.autocorrectionType = .no
        view.field.placeholder = "first_name".localized()
        return view
    }
    
    static var larstName: TextFieldView {
        let view = TextFieldView(.named("icon_person")!)
        view.field.keyboardType = .alphabet
        view.field.textContentType = .familyName
        view.field.autocapitalizationType = .words
        view.field.returnKeyType = .done
        view.field.autocorrectionType = .no
        view.field.placeholder = "last_name".localized()
        return view
    }
    
    static func confirmation(_ resendButton: UIButton) -> TextFieldView {
        let view = TextFieldView(.named("icon_confirmation_code")!)
        view.field.keyboardType = .numberPad
        if #available(iOS 12.0, *) {
            view.field.textContentType = .oneTimeCode
        } else {
            view.field.textContentType = .none
        }
        view.field.placeholder = "hint_enter_code".localized()
        view.field.rightViewMode = .always
        view.field.returnKeyType = .done
        let size = resendButton.sizeThatFits(.init(width: 300, height: 300))
        constrain(resendButton) {
            $0.width == size.width + .margin/2
            $0.height == size.height
        }
        view.field.rightView = resendButton
        resendButton.contentHorizontalAlignment = .trailing
        return view
    }
    
    static var code: TextFieldView {
        let view = TextFieldView(.named("icon_confirmation_code")!)
        view.field.keyboardType = .numberPad
        if #available(iOS 12.0, *) {
            view.field.textContentType = .oneTimeCode
        } else {
            view.field.textContentType = .none
        }
        view.field.returnKeyType = .done
        view.field.placeholder = "hint_enter_code".localized()
        return view
    }
    
    static var promoCode: TextFieldView {
        let view = TextFieldView(.named("icon_confirmation_code")!)
        view.field.keyboardType = .default
        view.field.autocapitalizationType = .allCharacters
        view.field.returnKeyType = .done
        view.field.placeholder = "hint_enter_code".localized()
        view.field.autocorrectionType = .no
        return view
    }
}
