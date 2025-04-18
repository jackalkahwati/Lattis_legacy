//
//  PinCodeView.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/27/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class PinCodeView: UIView {
    fileprivate let imageWidth: CGFloat = 24
    fileprivate var pinStorage: [UIImageView] = []
    
    fileprivate(set) var pin: [Ellipse.Pin] = []
    
    func insert(_ touch: Ellipse.Pin...) {
        pin += touch
        touch.forEach({ self.add(touch: $0) })
        setNeedsLayout()
    }
    
    func update(code: [Ellipse.Pin]) {
        pinStorage.forEach{ $0.removeFromSuperview() }
        pinStorage.removeAll()
        pin = code
        pin.forEach({ self.add(touch: $0) })
        setNeedsLayout()
    }
    
    func pop() {
        _ = pin.popLast()
        let image = pinStorage.popLast()
        image?.removeFromSuperview()
        setNeedsLayout()
    }
    
    var contentWidth: CGFloat {
        return CGFloat(pin.count)*imageWidth
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        var positionX = CGFloat(0)
        for pin in pinStorage {
            pin.frame = CGRect(x: positionX, y: 0, width: imageWidth, height: bounds.height)
            positionX += imageWidth
        }
    }
    
    fileprivate func add(touch: Ellipse.Pin) {
        let image: UIImage
        switch touch {
        case .up:
            image = #imageLiteral(resourceName: "icon_pin_up")
        case .down:
            image = #imageLiteral(resourceName: "icon_pin_down")
        case .left:
            image = #imageLiteral(resourceName: "icon_pin_left")
        case .right:
            image = #imageLiteral(resourceName: "icon_pin_right")
        }
        let imageView = UIImageView(image: image)
        let positionX = imageWidth*CGFloat(pinStorage.count)
        imageView.frame = CGRect(x: positionX, y: 0, width: imageWidth, height: bounds.height)
        imageView.contentMode = .center
        pinStorage.append(imageView)
        addSubview(imageView)
    }
}
