//
//  MenuButton.swift
//  Lattis
//
//  Created by Ravil Khusainov on 28/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class MenuButton: ShadowButton {

    
    override func setUp() {
        super.setUp()
        
        titleLabel.font = UIFont(.circularBook, size: 10)
        titleLabel.textColor = .white
        titleLabel.textAlignment = .center
        titleLabel.adjustsFontSizeToFitWidth = true
        titleLabel.numberOfLines = 2
    }
    
    override func updateShadow() {
        super.updateShadow()
        
        layer.shadowOpacity = 0.2
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = {
            var frame = bounds
            frame.size.height = titleLabel.sizeThatFits(frame.size).height
            return frame
        }()
        
        imageView.frame = {
            var frame = imageView.frame
            frame.size.height = imageView.image?.size.height ?? 0
            let height = frame.height + titleLabel.frame.height + 2
            frame.origin.y = (bounds.height - height)*0.5
            return frame
        }()
        
        titleLabel.frame = {
            var frame = titleLabel.frame
            frame.origin.y = imageView.frame.maxY + 2
            return frame
        }()
    }
}
