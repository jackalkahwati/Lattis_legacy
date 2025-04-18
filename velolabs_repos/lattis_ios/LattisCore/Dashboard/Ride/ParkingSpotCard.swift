//
//  ParkingSpotCard.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class CardCollectionViewCell: UICollectionViewCell {
    let infoView = UIImageView(image: .named("icon_info"))
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        contentView.backgroundColor = .white
        contentView.addShadow()
        contentView.layer.cornerRadius = 5
        
        contentView.addSubview(infoView)
        infoView.contentMode = .center
        infoView.setContentHuggingPriority(.required, for: .horizontal)
        
        constrain(infoView, contentView) { info, content in
            info.right == content.right - .margin/2
            info.top == content.top + .margin/2
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class ParkingSpotCard: CardCollectionViewCell {
    
    fileprivate let nameLabel = UILabel()
    fileprivate let detailsLabel = UILabel()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        contentView.addSubview(nameLabel)
        nameLabel.font = .theme(weight: .bold, size: .title)
        nameLabel.textColor = .gray
        
        contentView.addSubview(detailsLabel)
        detailsLabel.font = .theme(weight: .medium, size: .body)
        detailsLabel.textColor = .gray
        detailsLabel.numberOfLines = 0
        
        constrain(nameLabel, detailsLabel, infoView, contentView) { name, details, info, content in
            name.left == content.left + .margin/2
            name.right == info.left - .margin/2
            name.top == info.top + .margin/2
            
            details.left == name.left
            details.right == content.right - .margin/2
            details.top == name.bottom + .margin/2
            details.bottom == content.bottom - .margin/2
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var parking: Parking.Spot! {
        didSet {
            nameLabel.text = parking.name
            detailsLabel.text = parking.details
        }
    }
}
