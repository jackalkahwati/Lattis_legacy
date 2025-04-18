//
//  UIButton+Lattis.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

extension UIButton {
    func imageToRight() {
        transform = CGAffineTransform(scaleX: -1.0, y: 1.0)
        titleLabel?.transform = CGAffineTransform(scaleX: -1.0, y: 1.0)
        imageView?.transform = CGAffineTransform(scaleX: -1.0, y: 1.0)
        titleEdgeInsets = UIEdgeInsets(top: 0, left: 5, bottom: 0, right: 0)
    }
}
