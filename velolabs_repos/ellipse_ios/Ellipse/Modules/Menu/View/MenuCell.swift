//
//  MenuCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/13/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class MenuCell: UITableViewCell {
    fileprivate let titleLabel = UILabel()
    fileprivate let iconView = UIImageView()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        selectionStyle = .none
        
        contentView.addSubview(titleLabel)
        titleLabel.font = .elMenu
        titleLabel.textColor = .elSteel
        
        contentView.addSubview(iconView)
        iconView.contentMode = .center
        
        iconView.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        
        constrain(iconView, titleLabel, contentView) { icon, title, view in
            icon.left == view.left + .margin
            icon.top == view.top
            icon.bottom == view.bottom
            
            title.left == icon.right + .margin
            title.right == view.right - .margin
            title.centerY == view.centerY
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var item: MenuItem? {
        didSet {
            titleLabel.text = item?.rawValue.localized().lowercased().capitalized
            iconView.image = item?.icon
        }
    }
    
    var isCurrent: Bool = false {
        didSet {
            titleLabel.font = isCurrent ? .elMenuSelected : .elMenu
            titleLabel.textColor = isCurrent ? .black : .elSteel
//            contentView.backgroundColor = isCurrent ? UIColor(white: 0, alpha: 0.03) : .clear
        }
    }
}
