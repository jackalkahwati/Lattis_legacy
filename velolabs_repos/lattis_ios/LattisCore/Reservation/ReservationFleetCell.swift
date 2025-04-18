//
//  ReservationFleetCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 18.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Kingfisher
import Cartography
import Model

class ReservationFleetCell: UITableViewCell {
    
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .medium, size: .body))
    fileprivate let addressLabel = UILabel.label(font: .theme(weight: .book, size: .body), color: .lightGray)
    fileprivate let iconView = UIImageView()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(iconView)
        let stackView = UIStackView(arrangedSubviews: [titleLabel, addressLabel])
        stackView.axis = .vertical
        contentView.addSubview(stackView)
        tintColor = .black
        accessoryView = UIImageView(image: .named("icon_accessory_arrow"))
        
        constrain(iconView, stackView, contentView) { icon, stack, content in
            icon.left == content.left + .margin
            icon.centerY == content.centerY
            icon.height == 34
            icon.width == icon.height
            
            stack.bottom == content.bottom - .margin
            stack.top == content.top + .margin
            stack.left == icon.right + .margin/2
            stack.right == content.right - .margin
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var fleet: Model.Fleet! {
        didSet {
            titleLabel.text = fleet.name
            iconView.kf.setImage(with: fleet.logo)
            addressLabel.text = fleet.address?.copmose([\.city, \.country])
        }
    }
}
