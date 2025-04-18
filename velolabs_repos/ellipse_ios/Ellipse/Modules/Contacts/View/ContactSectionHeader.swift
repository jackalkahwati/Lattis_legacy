//
//  ContactSectionHeader.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/8/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class ContactSectionHeader: UITableViewHeaderFooterView {
    let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .elButtonBig
        label.textColor = .black
        return label
    }()
    
    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        
        contentView.backgroundColor = .elWhite
        contentView.addSubview(titleLabel)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = contentView.bounds.insetBy(dx: .margin, dy: 0)
    }
}
