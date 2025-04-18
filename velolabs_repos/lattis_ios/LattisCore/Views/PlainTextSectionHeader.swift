//
//  PlainTextSectionHeader.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 10/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class PlainTextSectionHeader: UIView {
    fileprivate let titleLabel = UILabel()
    
    init(_ text: String) {
        super.init(frame: .zero)
        
        backgroundColor = .white
        addSubview(titleLabel)
        titleLabel.font = .theme(weight: .medium, size: .body)
        titleLabel.textColor = .black
        titleLabel.text = text
        
        constrain(titleLabel, self) { title, view in
            title.left == view.left + .margin
            title.right == view.right - .margin
            title.centerY == view.centerY
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class TextWithValueSectionHeader: UIView {
    
    fileprivate let titleLabel = UILabel()
    fileprivate let valueLabel = UILabel()
    
    init(title: String, value: String) {
        super.init(frame: .zero)
        
        backgroundColor = .white
        addSubview(titleLabel)
        titleLabel.font = .theme(weight: .bold, size: .body)
        titleLabel.textColor = .gray
        titleLabel.text = title
        
        addSubview(valueLabel)
        valueLabel.font = .theme(weight: .bold, size: .title)
        valueLabel.textColor = .darkGray
        valueLabel.text = value
        
        
        constrain(titleLabel, valueLabel, self) { title, value, view in
            title.left == view.left + .margin
            title.centerY == view.centerY
            
            value.right == view.right - .margin
            value.centerY == view.centerY
        }
//        addShadow()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
