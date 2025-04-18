//
//  EditField.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 16/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class EditField: UIView {
    let textField = UITextField()
    let titleLabel = UILabel()
    let iconView = UIImageView()
    
    fileprivate var viewConstrnain: NSLayoutConstraint!
    fileprivate var iconConstraint: NSLayoutConstraint!
    
    init(_ title: String, text: String? = nil, placeholder: String? = nil) {
        super.init(frame: .zero)
        
        titleLabel.text = title
        addSubview(titleLabel)
        addSubview(textField)
        addSubview(iconView)
        
        textField.autocorrectionType = .no
        textField.text = text
        textField.font = .theme(weight: .bold, size: .body)
        textField.textColor = .darkGray
        if let p = placeholder {
            textField.attributedPlaceholder = .init(string: p, attributes: [.font: UIFont.theme(weight: .medium, size: .body), .foregroundColor: UIColor.lightGray])
        }
        
        titleLabel.font = .theme(weight: .bold, size: .small)
        titleLabel.textColor = .black
        
        iconView.contentMode = .scaleAspectFit
        iconView.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        
        constrain(titleLabel, textField, iconView, self) { title, text, icon, view in
            title.left == view.left
            title.top == view.top
            title.right == view.right
            
            icon.centerY == text.centerY
            icon.left == view.left
            
            text.top == title.bottom
            self.viewConstrnain = text.left == view.left ~ .defaultHigh
            self.iconConstraint = text.left == icon.right + .margin/2 ~ .defaultLow
            text.right == view.right
            text.bottom == view.bottom
            view.height == 44
        }
    }
    
    var icon: UIImage? {
        set {
            iconView.image = newValue
            viewConstrnain.priority = newValue == nil ? .defaultHigh : .defaultLow
            iconConstraint.priority = newValue == nil ? .defaultLow : .defaultHigh
            layoutIfNeeded()
        }
        get {
            return iconView.image
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
