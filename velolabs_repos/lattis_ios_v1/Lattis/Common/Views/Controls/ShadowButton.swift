//
//  ShadowButton.swift
//  Lattis
//
//  Created by Ravil Khusainov on 28/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import QuartzCore

@IBDesignable
class ShadowButton: UIControl {
    private let borderLayer = CALayer()
    internal let imageView = UIImageView()
    internal let titleLabel = UILabel()
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setUp()
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setUp()
    }
    
    internal func setUp() {
        layer.addSublayer(borderLayer)
        borderColor = .lsSilverTwo
        borderLayer.borderWidth = 0.5
        addSubview(imageView)
        imageView.contentMode = .center
        
        titleLabel.textAlignment = .center
        addSubview(titleLabel)
        
        updateShadow()
    }
    
    internal func updateShadow() {
        layer.shadowOpacity = 0.14
        layer.shadowColor = UIColor.black.cgColor
        layer.shadowRadius = 4
        layer.shadowOffset = CGSize(width: 1.5, height: 4.5)
        layer.masksToBounds = false
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        borderLayer.frame = bounds
        imageView.frame = bounds
        titleLabel.frame = bounds.insetBy(dx: 5, dy: 5)
    }
    
    override var borderColor: UIColor? {
        set {
            borderLayer.borderColor = newValue?.cgColor
        }
        get {
            return borderLayer.borderColor != nil ? UIColor(cgColor: borderLayer.borderColor!) : nil
        }
    }
    
    override var cornerRadius: CGFloat {
        didSet {
            layer.cornerRadius = cornerRadius
            borderLayer.cornerRadius = cornerRadius
            updateShadow()
        }
    }
    
    @IBInspectable var image: UIImage? {
        set {
            imageView.image = newValue
        }
        get {
            return imageView.image
        }
    }
    
    @IBInspectable var fontSize: CGFloat {
        set {
            titleLabel.font = UIFont(.circularMedium, size: newValue)
        }
        get {
            return titleLabel.font.pointSize
        }
    }
    
    @IBInspectable var title: String? {
        set {
            titleLabel.text = newValue?.localized()
            setNeedsLayout()
        }
        get {
            return titleLabel.text
        }
    }
    
    @IBInspectable var titleColor: UIColor {
        set {
            titleLabel.textColor = newValue
        }
        get {
            return titleLabel.textColor
        }
    }
}
