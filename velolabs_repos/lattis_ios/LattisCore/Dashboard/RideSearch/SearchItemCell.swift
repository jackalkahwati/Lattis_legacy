//
//  LocationSearchCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 11/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class SearchItemCell: UITableViewCell {
    
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .medium, size: .body))
    fileprivate let subtitleLabel = UILabel.label(font: .theme(weight: .book, size: .text), color: .lightGray)
    fileprivate let iconView = UIImageView(image: .named("icon_location_search_result"))
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        let container = UIView()
        contentView.addSubview(container)
        contentView.addSubview(iconView)
        iconView.contentMode = .scaleAspectFit
        iconView.tintColor = .black
        container.addSubview(titleLabel)
        container.addSubview(subtitleLabel)
        titleLabel.numberOfLines = 2
        subtitleLabel.numberOfLines = 2
        
        titleLabel.setContentHuggingPriority(.defaultHigh, for: .vertical)
        subtitleLabel.setContentHuggingPriority(.defaultHigh, for: .vertical)
        
        constrain(titleLabel, subtitleLabel, container, iconView, contentView) { title, subtitle, container, icon, content in
            title.left == container.left
            title.right == container.right
            title.top == container.top
            
            subtitle.top == title.bottom
            subtitle.left == container.left
            subtitle.right == container.right
            subtitle.bottom == container.bottom
            
            icon.width == 16
            icon.height == 20
            icon.left == content.left + .margin
            icon.top == container.top
            
            container.left == icon.right + .margin/2
            container.right == content.right - .margin
            
            container.top == content.top + .margin/2
            container.bottom == content.bottom - .margin/2
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var item: SearchItem! {
        didSet {
            titleLabel.text = item.title
            subtitleLabel.text = item.subtitle
            iconView.image = .named(item.iconName)
        }
    }
}


