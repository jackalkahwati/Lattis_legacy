//
//  SLTouchSliderViewController.swift
//  Ellipse
//
//  Created by Andre Green on 8/11/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLTouchSliderViewController: UIViewController {
    // This should be read from the current lock on initialization
    // The value will be between 0 and 1
    var sliderValue:Double = 0.0
    
    var sliderLayer:CAGradientLayer = CAGradientLayer()
    
    let sliderHeight:CGFloat = 20.0
    
    let xPadding:CGFloat = 15.0
    
    let labelFont:UIFont = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 10.0)!
        
    lazy var sliderBackgroundView:UIView = {
        let frame = CGRect(
            x: self.xPadding,
            y: self.view.bounds.size.height - self.sliderHeight,
            width: self.view.bounds.size.width - 2*self.xPadding,
            height: self.sliderHeight
        )
        
        let pgr:UIPanGestureRecognizer = UIPanGestureRecognizer(
            target: self,
            action: #selector(sliderViewDragged(pgr:))
        )
        pgr.minimumNumberOfTouches = 1
        
        let view:UIView = UIView(frame: frame)
        view.backgroundColor = UIColor(red: 226, green: 226, blue: 226)
        view.layer.cornerRadius = 0.5*self.sliderHeight
        view.clipsToBounds = true
        view.addGestureRecognizer(pgr)
        
        return view
    }()
    
    lazy var sliderView:UIView = {
        let frame = CGRect(
            x: 0.0,
            y: 0.0,
            width: CGFloat(self.sliderValue)*self.sliderBackgroundView.bounds.size.width,
            height: self.sliderBackgroundView.bounds.size.height
        )
        let view:UIView = UIView(frame: frame)
        
        let colors:[CGColor] = [
            UIColor(red: 60, green: 83, blue: 119).cgColor,
            UIColor(red: 87, green: 216, blue: 255).cgColor
        ]
        
        let locations = [0.0, 1.0]
        
        self.sliderLayer.frame = view.frame
        self.sliderLayer.colors = colors
        self.sliderLayer.locations = locations as [NSNumber]?
        
        view.layer.addSublayer(self.sliderLayer)
        return view
    }()
    
    lazy var lowLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let text = NSLocalizedString("Low", comment: "")
        let utility = SLUtilities()
        let labelSize:CGSize = utility.sizeForLabel(
            font: self.labelFont,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 1
        )
        
        let frame = CGRect(
            x: self.xPadding,
            y: self.sliderBackgroundView.frame.minY - 18.0,
            width: labelSize.width,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame:frame)
        label.text = text
        label.font = self.labelFont
        label.textColor = UIColor(white: 155.0/255.0, alpha: 1.0)
        label.textAlignment = .left
        
        return label
    }()
    
    lazy var mediumLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let text = NSLocalizedString("Medium", comment: "")
        let utility = SLUtilities()
        let labelSize:CGSize = utility.sizeForLabel(
            font: self.labelFont,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 1
        )
        
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - labelSize.width),
            y: self.lowLabel.frame.minY,
            width: labelSize.width,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame:frame)
        label.text = text
        label.font = self.labelFont
        label.textColor = UIColor(white: 155.0/255.0, alpha: 1.0)
        label.textAlignment = .center
        
        return label
    }()
    
    lazy var highLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let text = NSLocalizedString("High", comment: "")
        let utility = SLUtilities()
        let labelSize:CGSize = utility.sizeForLabel(
            font: self.labelFont,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 1
        )
        
        let frame = CGRect(
            x: self.view.bounds.size.width - labelSize.width - self.xPadding,
            y: self.lowLabel.frame.minY,
            width: labelSize.width,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame:frame)
        label.text = text
        label.font = self.labelFont
        label.textColor = UIColor(white: 155.0/255.0, alpha: 1.0)
        label.textAlignment = .right
        
        return label
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor.white
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if !self.view.subviews.contains(self.sliderBackgroundView) {
            self.view.addSubview(self.sliderBackgroundView)
        }
        
        if !self.sliderBackgroundView.subviews.contains(self.sliderView) {
            self.sliderBackgroundView.addSubview(self.sliderView)
        }
        
        if !self.view.subviews.contains(self.lowLabel) {
            self.view.addSubview(self.lowLabel)
        }
        
        if !self.view.subviews.contains(self.mediumLabel) {
            self.view.addSubview(self.mediumLabel)
        }
        
        if !self.view.subviews.contains(self.highLabel) {
            self.view.addSubview(self.highLabel)
        }
    }
    
    func sliderViewDragged(pgr: UIPanGestureRecognizer) {
        let location:CGPoint = pgr.location(in: self.sliderBackgroundView)
        self.sliderLayer.frame = CGRect(x: 0.0, y: 0.0, width: location.x, height: self.sliderHeight)
        self.sliderValue = Double(location.x/self.sliderBackgroundView.bounds.size.width)
    }
    
    func getSliderValue() -> Double {
        return self.sliderValue
    }
}
