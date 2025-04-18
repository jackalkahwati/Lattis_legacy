//
//  LockDetailsFWCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/2/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class LockDetailsFWCell: LockDetailsBaseCell {
    var action: () -> () = {}
    fileprivate let subtitleLabel = UILabel()
    fileprivate let updateButton = UIButton(type: .custom)
    
    override var info: LockDetails.Info! {
        didSet {
            subtitleLabel.text = info.placeholder
        }
    }
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(subtitleLabel)
        contentView.addSubview(updateButton)
        
        subtitleLabel.font = .elRegular
        subtitleLabel.textColor = .elSlateGrey
        
        updateButton.backgroundColor = .elDarkSkyBlue
        updateButton.setTitle("update".localized(), for: .normal)
        updateButton.setTitleColor(.white, for: .normal)
        updateButton.layer.cornerRadius = 15
        updateButton.titleLabel?.font = .elSmal
        updateButton.contentEdgeInsets = .init(top: 0, left: 10, bottom: 0, right: 10)
        
        titleLabel.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        
        constrain(subtitleLabel, titleLabel, updateButton, contentView) { subtitle, title, button, container in
            title.left == container.left + .margin
            title.right == button.left - .margin/2
            title.top == container.top + .margin
            
            button.centerY == title.centerY
            button.right >= container.right - .margin ~ .defaultLow
            button.height == 30
            
            subtitle.top == title.bottom + .margin/2
            subtitle.right == container.right - .margin
            subtitle.left == title.left
            subtitle.bottom == container.bottom - .margin/3
        }
        subtitleLabel.setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
        updateButton.addTarget(self, action: #selector(update), for: .touchUpInside)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc fileprivate func update() {
        action()
    }
}
