//
//  LockDetailsCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/31/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class LockDetailsBaseCell: UITableViewCell {
    let titleLabel = UILabel()
    
    var info: LockDetails.Info! {
        didSet {
            titleLabel.text = info.title
            if info.accessory == .disclosureIndicator {
                accessoryView = UIImageView(image: UIImage(named: "icon_pen"))
            } else {
                accessoryView = nil
            }
            selectionStyle = info.accessory == .disclosureIndicator ? .default : .none
        }
    }
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(titleLabel)
        titleLabel.font = .elTitle
        titleLabel.textColor = .black
        titleLabel.setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class LockDetailsCell: LockDetailsBaseCell {
    fileprivate let subtitleLabel = UILabel()
    override var info: LockDetails.Info! {
        didSet {
            subtitleLabel.text = info.placeholder
        }
    }
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        subtitleLabel.setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
        
        contentView.addSubview(subtitleLabel)
        subtitleLabel.font = .elRegular
        subtitleLabel.textColor = .elSlateGrey
        constrain(subtitleLabel, titleLabel, contentView) { subtitle, title, container in
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
}

extension LockDetails.Info: EditableContent {
    var title: String? {
        switch self {
        case .name(_):
            return "lock_name".localized()
        case .owner(_):
            return "registered_owner".localized()
        case .serial(_):
            return "serial_number".localized()
        case .firmware(_, _):
            return "firmware".localized()
        case .sensetivity:
            return "lock_settings_sensetivity_title".localized()
        case .pin(_):
            return "pin_code".localized()
        }
    }
    
    var placeholder: String? {
        switch self {
        case .name(let name, _):
            return name
        case .owner(let owner):
            return owner
        case .serial(let serial):
            return serial
        case .firmware(let version, _):
            return version
        default:
            return nil
        }
    }
    
    var charLimit: Int {
        return 32
    }
    
    var keyboardType: UIKeyboardType {
        return .default
    }
    
    var contentType: UITextContentType {
        return .name
    }
    
    var capitalizationType: UITextAutocapitalizationType {
        return .words
    }
    
    var accessory: UITableViewCell.AccessoryType {
        switch self {
        case .owner, .serial, .firmware, .sensetivity:
            return .none
        case .name(_, let editable) where editable == false:
            return .none
        default:
            return .disclosureIndicator
        }
    }
}
