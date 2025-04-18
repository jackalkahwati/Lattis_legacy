//
//  FleetCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 31/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Kingfisher

class FleetCell: UITableViewCell {
    
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .book, size: .body))
    fileprivate let iconView = UIImageView()
    fileprivate let subtitleLabel = UILabel.label(font: .theme(weight: .medium, size: .small))
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(iconView)
        contentView.addSubview(subtitleLabel)
        
        iconView.contentMode = .scaleAspectFit
        
        constrain(titleLabel, iconView, subtitleLabel, contentView) { title, icon, subtitle, content in
            icon.left == content.left + .margin
            icon.top == content.top + .margin/2
            icon.bottom == content.bottom - .margin/2
            icon.width == icon.height
            
//            title.top == icon.top
            title.left == icon.right + .margin/2
            title.right == content.right - .margin
            title.centerY == content.centerY
            
            subtitle.left == title.left
            subtitle.right == title.right
            subtitle.bottom == icon.bottom
        }
    }
    
    var fleet: Fleet! {
        didSet {
            titleLabel.text = fleet.name
            iconView.kf.setImage(with: fleet.logo)
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
