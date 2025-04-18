//
//  PaymentCardCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 16/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

class PaymentCardCell: UITableViewCell {
    
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .medium, size: .text))
    fileprivate let iconView = UIImageView()
    fileprivate let subtitleLabel = UILabel.label(font: .theme(weight: .medium, size: .text))
    fileprivate let checkbox = UIImageView(image: .named("icon_check"))
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(iconView)
        contentView.addSubview(titleLabel)
        contentView.addSubview(subtitleLabel)
        contentView.addSubview(checkbox)
        
        iconView.contentMode = .scaleAspectFit
        checkbox.contentMode = .center

        iconView.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        checkbox.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        subtitleLabel.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        checkbox.tintColor = .accent
        
        constrain(iconView, titleLabel, subtitleLabel, checkbox, contentView) { icon, title, subtitle, check, content in
            icon.left == content.left + .margin
            icon.top >= content.top + .margin/4
            icon.bottom <= content.bottom - .margin/4
            icon.centerY == title.centerY
            
            title.left == icon.right + .margin/2
            title.centerY == content.centerY
            title.right == subtitle.left - .margin/2
            
            check.right == content.right - .margin
            check.centerY == title.centerY
            
            subtitle.centerY == title.centerY
            subtitle.right == check.left - .margin
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var payment: Payment! {
        didSet {
            titleLabel.text = payment.title
            iconView.image = payment.icon
            checkbox.isHidden = !payment.isCurrent
            if case let .card(card) = payment! {
                subtitleLabel.text = card.date
            } else {
                subtitleLabel.text = nil
            }
        }
    }
}
