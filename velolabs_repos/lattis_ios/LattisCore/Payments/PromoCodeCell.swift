//
//  PromoCodeCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 04.02.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import UIKit
import Model
import Cartography

class PromoCodeCell: UITableViewCell {
    
    fileprivate let perkLabel = UILabel.label(font: .theme(weight: .book, size: .small), color: .white)
    fileprivate let fleetLabel = UILabel.label(font: .theme(weight: .medium, size: .body))
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = .none
        contentView.addSubview(fleetLabel)
        let bgView = UIView()
        bgView.backgroundColor = .black
        bgView.layer.cornerRadius = 12
        contentView.addSubview(bgView)
        bgView.addSubview(perkLabel)
        
        constrain(fleetLabel, perkLabel, bgView, contentView) { fleet, perk, bg, view in
            fleet.left == view.left + .margin
            bg.right == view.right - .margin
            fleet.centerY == view.centerY
            perk.centerY == bg.centerY
            bg.centerY == view.centerY
            bg.left == fleet.right + .margin
            
            perk.edges == bg.edges.inseted(horizontally: .margin/2)
            bg.height == 24
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var promotion: Promotion! {
        didSet {
            let percent = Int(promotion.amount)
            perkLabel.text = "perk_template_bike".localizedFormat("\(percent)")
            fleetLabel.text = promotion.fleet?.name ?? ""
        }
    }
}
