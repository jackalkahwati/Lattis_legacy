//
//  SettingsSectionView.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 26/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class SettingsSectionView: UIView {
    let titleLabel: UILabel = {
        let label = UILabel()
        label.textAlignment = .center
        label.font = UIFont.systemFont(ofSize: 14)
        label.textColor = .lsWarmGrey
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(titleLabel)
        backgroundColor = .lsWhite
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        titleLabel.frame = bounds.insetBy(dx: 16, dy: 0)
    }
}
