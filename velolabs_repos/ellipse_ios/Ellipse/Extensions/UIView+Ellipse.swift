//
//  UIView+Ellipse.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/9/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

extension UIView {
    @IBInspectable var cornerRadius: CGFloat {
        set {
            layer.cornerRadius = newValue
            layer.masksToBounds = newValue > 0
        }
        get {
            return layer.cornerRadius
        }
    }
    
    @IBInspectable var borderWidth: CGFloat {
        set {
            layer.borderWidth = newValue
        }
        get {
            return layer.borderWidth
        }
    }
    
    @IBInspectable var borderColor: UIColor? {
        set {
            layer.borderColor = newValue?.cgColor
        }
        get {
            return layer.borderColor != nil ? UIColor(cgColor: layer.borderColor!) : nil
        }
    }
    
    class func nib(name: String? = nil, bundle: Bundle = .main) -> UIView? {
        return bundle.loadNibNamed(name ?? String(describing: self), owner: nil, options: nil)?.first as? UIView
    }
}
