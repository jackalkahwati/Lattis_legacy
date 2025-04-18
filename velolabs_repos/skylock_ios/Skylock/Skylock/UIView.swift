//
//  UIView.swift
//  Extensions
//
//  Created by Ravil Khusainov on 24/12/2016.
//  Copyright © 2016 RKH. All rights reserved.
//

import UIKit

public extension UIView {
    public func constrainEqual(attribute: NSLayoutAttribute, to: AnyObject, multiplier: CGFloat = 1, constant: CGFloat = 0) {
        _ = constrainEqual(attribute, to: to, attribute, multiplier: multiplier, constant: constant)
    }
    
    public func constrainEqual(_ attribute: NSLayoutAttribute, to: AnyObject?, _ toAttribute: NSLayoutAttribute, multiplier: CGFloat = 1, constant: CGFloat = 0) -> NSLayoutConstraint {
        let constraint = NSLayoutConstraint(item: self, attribute: attribute, relatedBy: .equal, toItem: to, attribute: toAttribute, multiplier: multiplier, constant: constant)
        NSLayoutConstraint.activate([constraint])
        return constraint
    }
    
    public func constrainEdges(to view: UIView) {
        _ = constrainEqual(.top, to: view, .top)
        _ = constrainEqual(.leading, to: view, .leading)
        _ = constrainEqual(.trailing, to: view, .trailing)
        _ = constrainEqual(.bottom, to: view, .bottom)
    }
    
    public class func nib(name: String? = nil, bundle: Bundle = .main) -> UIView? {
        return bundle.loadNibNamed(name ?? String(describing: self), owner: nil, options: nil)?.first as? UIView
    }
        
    public var snapshot: UIImage? {
        UIGraphicsBeginImageContext(frame.size)
        layer.render(in: UIGraphicsGetCurrentContext()!)
        let image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return image
    }

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
}

public extension TimeInterval {
    static let defaultAnimation: TimeInterval = 0.35
}
