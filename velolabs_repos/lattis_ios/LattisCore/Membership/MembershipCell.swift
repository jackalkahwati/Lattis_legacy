//
//  MembershipCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 04.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Kingfisher
import Model

class MembershipCell: UITableViewCell {

    fileprivate let fleetLabel = UILabel.label(font: .theme(weight: .medium, size: .body))
    fileprivate let addresslLabel = UILabel.label(font: .theme(weight: .bold, size: .body), color: .lightGray)
    fileprivate let iconView = UIImageView()
    fileprivate let perkContainer = UIView()
    fileprivate let perkLabel = UILabel.label(font: .theme(weight: .book, size: .small), color: .white, allignment: .right)
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(iconView)
        iconView.contentMode = .scaleAspectFit
        addresslLabel.minimumScaleFactor = 0.5
        addresslLabel.adjustsFontSizeToFitWidth = true
        let fleetStack = UIStackView(arrangedSubviews: [fleetLabel, addresslLabel])
        fleetStack.axis = .vertical
        contentView.addSubview(fleetStack)
        
        perkContainer.addSubview(perkLabel)
        contentView.addSubview(perkContainer)
        
        perkContainer.backgroundColor = .accent
        perkContainer.layer.cornerRadius = 12
        
        constrain(iconView, fleetStack, perkContainer, perkLabel, contentView) { icon, fleet, container, perk, view in
            
            icon.left == view.left + .margin
            icon.height == 34
            icon.width == icon.height
            icon.top == view.top + .margin/2
            icon.bottom == view.bottom - .margin/2
            
            fleet.left == icon.right + .margin/2
            fleet.centerY ==  view.centerY
            fleet.right <= container.left - .margin/2
            
            container.right == view.right - .margin
            container.centerY == view.centerY
            container.height == 24
            
            perk.edges == container.edges.inseted(horizontally: .margin/2, vertically: 0)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var membership: Membership! {
        didSet {
            fleetLabel.text = membership.fleet.name
            addresslLabel.text = membership.fleet.address?.copmose([\.city, \.country])
            iconView.kf.setImage(with: membership.fleet.logo)
            if let price = membership.priceString {
                perkLabel.text = "%@/%@".localizedFormat(price, membership.frequency.priceCycle)
                perkContainer.isHidden = false
            } else {
                perkLabel.text = nil
                perkContainer.isHidden = true
            }
            accessoryType = .none
            accessoryView = nil
            tintColor = .black
        }
    }
    
    var subscription: Subscription? {
        didSet {
            membership = subscription?.membership
            perkContainer.isHidden = true
            
            accessoryType = .disclosureIndicator
            accessoryView = UIImageView(image: .named("icon_accessory_arrow"))
        }
    }
}


