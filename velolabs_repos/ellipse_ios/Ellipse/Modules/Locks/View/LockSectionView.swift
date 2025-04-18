//
//  LockSectionView.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/26/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class SectionView: UIView {
    fileprivate let titleLabel = UILabel()
    
    init(_ title: String) {
        super.init(frame: .zero)
        backgroundColor = .elWhite
        titleLabel.textColor = .elWarmGreyTwo
        addSubview(titleLabel)
        titleLabel.textAlignment = .center
        titleLabel.font = .elButtonSmall
        titleLabel.numberOfLines = 0
        titleLabel.text = title
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = bounds.insetBy(dx: 16, dy: 8)
    }
}

class LockSectionView: SectionView {
    let style: Locks.Section.Style
    init(_ style: Locks.Section.Style) {
        self.style = style
        super.init(style.rawValue.localized())
        
        switch style {
        case .current:
            titleLabel.textColor = .elDarkSkyBlue
        case .previous, .unreachable:
            titleLabel.textColor = .black
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
