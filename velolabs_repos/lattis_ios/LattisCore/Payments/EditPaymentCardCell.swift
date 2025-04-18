//
//  EditPaymentCardCell.swift
//  LattisCore
//
//  Created by Roger Molas on 8/30/22.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

class EditPaymentCardCell: UITableViewCell {
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .medium, size: .body))
    fileprivate let iconView = UIImageView()
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(iconView)
        contentView.addSubview(titleLabel)
        iconView.contentMode = .scaleAspectFit
        iconView.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        constrain(iconView, titleLabel, contentView) { icon, title, content in
            icon.left == content.left + .margin
            icon.top >= content.top + .margin/4
            icon.bottom <= content.bottom - .margin/4
            icon.centerY == title.centerY
            
            title.left == icon.right + .margin/2
            title.centerY == content.centerY
            title.right == content.safeAreaLayoutGuide.right - .margin/2
        }
    }
    
    var payment: Payment! {
        didSet {
            titleLabel.text = payment.title
            iconView.image = payment.icon
        }
    }
}
