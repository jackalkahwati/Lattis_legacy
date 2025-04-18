//
//  LockCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/26/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import SwipeCellKit
import Cartography

class LockCell: SwipeTableViewCell {
    fileprivate let titleLabel = UILabel()
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        contentView.addSubview(titleLabel)
        titleLabel.font = .elTitleLarge
        titleLabel.textColor = .black
        
        tintColor = .elDarkSkyBlue
        
        constrain(titleLabel, contentView) { title, view in
            title.edges == inset(view.edges, 0, .margin, 0, .margin)
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var lock: Ellipse.Lock! {
        didSet {
            titleLabel.text = lock?.name
            if lock.isConnected {
                accessoryView = UIImageView(image: UIImage(named: "lock_disclosure"))
            } else {
                accessoryView = nil
            }
        }
    }
}
