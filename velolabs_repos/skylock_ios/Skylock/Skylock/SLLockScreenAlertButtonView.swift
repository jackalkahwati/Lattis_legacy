//
//  SLLockScreenAlertButtonView.swift
//  Skylock
//
//  Created by Andre Green on 6/23/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

enum SLLockScreenAlertButtonTextPlacement {
    case left
    case right
}

class SLLockScreenAlertButton: UIButton {
    let xSpacer:CGFloat = 5
    
    init(
        activeImageName: String,
        inactiveImageName:String,
        titleText: String,
        textColor:UIColor,
        textPlacement: SLLockScreenAlertButtonTextPlacement
        )
    {
        let activeImage = UIImage(named: activeImageName)!
        let inactiveImage = UIImage(named: inactiveImageName)!
        
        let utility = SLUtilities()
        let font = UIFont(name: SLFont.MonserratBold.rawValue, size: 9.0)!
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: titleText,
            maxWidth: CGFloat.greatestFiniteMagnitude,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: 0.0,
            y: 0.0,
            width: activeImage.size.width + self.xSpacer + labelSize.width,
            height: activeImage.size.height
        )
        
        super.init(frame: frame)

        self.setTitle(titleText, for: .normal)
        self.setTitleColor(textColor, for: .selected)
        self.setTitleColor(textColor.withAlphaComponent(0.4), for: .normal)
        self.titleLabel?.font = font
        self.contentVerticalAlignment = .center
        self.contentHorizontalAlignment = .center
        self.titleLabel?.numberOfLines = 0
        self.titleLabel?.lineBreakMode = NSLineBreakMode.byWordWrapping
        self.setImage(activeImage, for: .selected)
        self.setImage(inactiveImage, for: .normal)
//        self.imageEdgeInsets = UIEdgeInsets(
//            top: 0,
//            left: 0.5*(self.bounds.size.width - (self.imageView?.bounds.size.width)!),
//            bottom: 0,
//            right: 0
//        )
//        self.titleEdgeInsets = UIEdgeInsets(
//            top: (self.imageView?.bounds.size.height)! + self.ySpacer,
//            left: -0.5*frame.size.width - 10.0,
//            bottom: 0,
//            right: 0
//        )
//        self.contentEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 0)
        let imageInsetTop:CGFloat
        let imageInsetLeft:CGFloat
        let imageInsetBottom:CGFloat
        let imageInsetRight:CGFloat
        let titleInsetTop:CGFloat
        let titleInsetLeft:CGFloat
        let titleInsetBottom:CGFloat
        let titleInsetRight:CGFloat
        switch textPlacement {
        case .right:
            imageInsetTop = 0.0
            imageInsetLeft = 0.0
            imageInsetRight = 0.0
            imageInsetBottom = 0.0
            titleInsetTop = 0.0
            titleInsetLeft = 5.0
            titleInsetRight = 0.0
            titleInsetBottom = 0.0
            self.titleLabel?.textAlignment = .left
        case .left:
            imageInsetTop = 0.0
            imageInsetLeft = frame.size.width - activeImage.size.width
            imageInsetRight = 0.0
            imageInsetBottom = 0.0
            titleInsetTop = 0.0
            titleInsetLeft = -0.5*frame.size.width - 0.5*activeImage.size.width - 5.0
            titleInsetRight = 0.0
            titleInsetBottom = 0.0
            self.titleLabel?.textAlignment = .right
        }
        
        self.titleEdgeInsets = UIEdgeInsets(
            top: titleInsetTop,
            left: titleInsetLeft,
            bottom: titleInsetBottom,
            right: titleInsetRight
        )
        
        self.imageEdgeInsets = UIEdgeInsets(
            top: imageInsetTop,
            left: imageInsetLeft,
            bottom: imageInsetBottom,
            right: imageInsetRight
        )
        
        self.contentEdgeInsets = UIEdgeInsets(top: 0.0, left: 0.0, bottom: 0.0, right: 0.0)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
