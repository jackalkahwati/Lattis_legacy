//
//  LockDetailsSectionView.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/31/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class TableSectionView: UIView {
    
    let titleLabel = UILabel()
    init(_ title: String) {
        super.init(frame: .zero)
        
        addSubview(titleLabel)
        titleLabel.font = .elRegular
        titleLabel.textColor = .black
        backgroundColor = .elWhite
        titleLabel.text = title.lowercased().capitalized
        
        constrain(titleLabel, self) { label, view in
            label.edges == inset(view.edges, 0, .margin, 0, .margin)
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
