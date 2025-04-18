//
//  SLTouchPadView.swift
//  Skylock
//
//  Created by Andre Green on 9/3/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

import UIKit

enum SLTouchPadLocation {
    case top
    case right
    case bottom
    case left
}

protocol SLTouchPadViewDelegate:class {
    func touchPadViewLocationSelected(
        _ touchPadViewController: SLTouchPadView,
        location:SLTouchPadLocation
    )
}

class SLTouchPadView: UIView {
    let buttonDiameter: CGFloat = 40.0
    let largeButtonDiameter: CGFloat = 80.0
    let buttonGreenColor = UIColor.color(110, green: 223, blue: 158)
    let buttonGreyColor = UIColor.color(216, green: 216, blue: 216)
    let font = UIFont(name: "Helvetica Neue", size: 28)
    weak var delegate: SLTouchPadViewDelegate?
    
    lazy var selectionCircle: UIImageView = {
        let image = UIImage(named: "pin_tap_circle")!
        let imageView = UIImageView(image: image)
        imageView.frame = CGRect(x: 0, y: 0, width: image.size.width, height: image.size.height)
        imageView.alpha = 0
        return imageView
    }()
    
    lazy var topButton: UIButton = {
        let image:UIImage = UIImage(named: "pin_arrow_white_up")!
        let button = UIButton(type: .custom)
        button.addTarget(
            self,
            action: #selector(touchPadButtonPressed(_:)),
            for: UIControlEvents.touchDown
        )
        button.setImage(image, for: .normal)
        
        return button
    }()
    
    lazy var rightButton: UIButton = {
        let image:UIImage = UIImage(named: "pin_arrow_white_right")!
        let button = UIButton(type: .custom)
        button.addTarget(
            self,
            action: #selector(touchPadButtonPressed(_:)),
            for: UIControlEvents.touchDown
        )
        button.setImage(image, for: UIControlState())
        
        return button
    }()
    
    lazy var bottomButton: UIButton = {
        let image:UIImage = UIImage(named: "pin_arrow_white_down")!
        let button = UIButton(type: .custom)
        button.addTarget(
            self,
            action: #selector(touchPadButtonPressed(_:)),
            for: UIControlEvents.touchDown
        )
        button.setImage(image, for: UIControlState())
        
        return button
    }()
    
    lazy var leftButton: UIButton = {
        let image:UIImage = UIImage(named: "pin_arrow_white_left")!
        let button = UIButton(type: .custom)
        button.addTarget(
            self,
            action: #selector(touchPadButtonPressed(_:)),
            for: UIControlEvents.touchDown
        )
        button.setImage(image, for: UIControlState())
        
        return button
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(selectionCircle)
        addSubview(topButton)
        addSubview(rightButton)
        addSubview(bottomButton)
        addSubview(leftButton)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        let size = self.selectionCircle.image!.size
        topButton.frame = CGRect(
            x: 0.5*(self.bounds.size.width - size.width),
            y: 0,
            width: size.width,
            height: size.height
        )
        
        rightButton.frame = CGRect(
            x: bounds.size.width - size.width,
            y: bounds.midY - 0.5*size.height,
            width: size.width,
            height: size.height
        )
        
        bottomButton.frame = CGRect(
            x: 0.5*(bounds.size.width - size.width),
            y: bounds.size.height - size.height,
            width: size.width,
            height: size.height
        )
        
        leftButton.frame = CGRect(
            x: 0,
            y: bounds.midY - 0.5*size.width,
            width: size.width,
            height: size.height
        )
    }
    
    func touchPadButtonPressed(_ sender: UIButton) {        
        let location: SLTouchPadLocation
        switch sender {
        case self.topButton:
            location = .top
        case self.rightButton:
            location = .right
        case self.bottomButton:
            location = .bottom
        case self.leftButton:
            location = .left
        default:
            location = .top
        }
    
        self.delegate?.touchPadViewLocationSelected(self, location: location)
        showTap(to: sender.center)
    }
    
    private func showTap(to point: CGPoint) {
        selectionCircle.center = point
        UIView.animate(withDuration: 0.3, delay: 0, options: .curveEaseIn, animations: { 
            self.selectionCircle.alpha = 1
        }, completion: { _ in
            UIView.animate(withDuration: 0.5, delay: 0, options: .curveEaseIn, animations: {
                self.selectionCircle.alpha = 0
            }, completion: nil)
        })
    }
}
