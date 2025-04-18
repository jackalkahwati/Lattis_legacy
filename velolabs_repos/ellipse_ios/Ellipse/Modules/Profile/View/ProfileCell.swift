//
//  ProfileCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/9/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class ProfileCell: UITableViewCell {
    fileprivate let titleLabel = UILabel()
    fileprivate let valueLabel = UILabel()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        contentView.addSubview(titleLabel)
        contentView.addSubview(valueLabel)
        titleLabel.textColor = .black
        titleLabel.font = .elTitle
        valueLabel.font = .elRegular
        valueLabel.textColor = .elSlateGrey
        titleLabel.setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
        valueLabel.setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
        
        constrain(valueLabel, titleLabel, contentView) { subtitle, title, container in
            title.left == container.left + .margin
            title.right == container.right - .margin ~ .defaultLow
            title.top == container.top + .margin
            
            title.bottom == subtitle.top - .margin/2
            
            subtitle.right == container.right - .margin
            subtitle.left == container.left + .margin
            subtitle.bottom == container.bottom - .margin/3
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var item: Profile.Item! {
        didSet {
            titleLabel.text = item.title
            valueLabel.text = item.value
            if item.contentType == .password {
                valueLabel.text = String(repeating: "●", count: item.value?.count ?? 10)
            }
        }
    }
    
    var isFacebookUser: Bool = false {
        didSet {
            guard isFacebookUser else {
                accessoryView = UIImageView(image: UIImage(named: "icon_pen"))
                return
            }
            switch item! {
            case .phoneNmber:
                accessoryView = UIImageView(image: UIImage(named: "icon_pen"))
                selectionStyle = .default
            default:
                accessoryView = nil
                selectionStyle = .none
            }
        }
    }
}

extension Profile.Item {
    var title: String? {
        switch self {
        case .firstName(_):
            return "first_name".localized()
        case .lastName(_):
            return "last_name".localized()
        case .phoneNmber(_):
            return "phone_number".localized()
        case .email(_):
            return "hint_emailaddress".localized()
        case .password:
            return "hint_password".localized()
        }
    }
    
    var value: String? {
        switch self {
        case .firstName(let value), .lastName(let value), .email(let value):
            return value
        case .phoneNmber(let value):
            return value?.phoneNumberFormat
        case .password:
            return "password_hint".localized()
        }
    }
    
    var contentType: UITextContentType {
        switch self {
        case .firstName:
            return .givenName
        case .lastName:
            return .familyName
        case .email:
            return .emailAddress
        case .phoneNmber:
            return .telephoneNumber
        case .password:
            return .password
        }
    }
    
    var keyboardType: UIKeyboardType {
        switch self {
        case .email(_):
            return .emailAddress
        case .phoneNmber:
            return .phonePad
        default:
            return .default
        }
    }
    
    var capitalizationType: UITextAutocapitalizationType {
        switch self {
        case .firstName(_), .lastName(_):
            return .words
        default:
            return .none
        }
    }
    
    var correctionType: UITextAutocorrectionType {
        switch self {
        case .firstName(_), .lastName(_):
            return .yes
        default:
            return .no
        }
    }
}
