//
//  LockDetailsPinCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/31/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class LockDetailsPinCell: LockDetailsBaseCell {

    fileprivate let subtitleLabel = UILabel()
    fileprivate let pinView = PinCodeView()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(subtitleLabel)
        contentView.addSubview(pinView)
        pinView.isUserInteractionEnabled = false
        subtitleLabel.font = .elRegular
        subtitleLabel.textColor = .elSlateGrey
        subtitleLabel.text = "None".localized()
        constrain(titleLabel, subtitleLabel, pinView, contentView) { title, subtitle, pin, view in
            title.top == view.top + .margin
            title.left == view.left + .margin
            title.right == view.right - .margin ~ .defaultLow
            
            title.bottom == subtitle.top - .margin/2
            
            subtitle.bottom == view.bottom - .margin/3
            subtitle.left == title.left
            subtitle.right == title.right
            
            pin.left == title.left
            pin.bottom == subtitle.bottom
            pin.right == title.right
            pin.height == 30
        }
        subtitleLabel.setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
    }
    
    override var info: LockDetails.Info! {
        didSet {
            guard case let .pin(code) = info! else { return }
            if !code.isEmpty {
                subtitleLabel.isHidden = true
                pinView.isHidden = false
                pinView.update(code: code)
            } else {
                subtitleLabel.isHidden = false
                pinView.isHidden = true
            }
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

