//
//  SLOnboardingViewController.swift
//  Skylock
//
//  Created by Andre Green on 5/26/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLOnboardingViewController: UIViewController {
    let picName:String
    let titleText:String
    let text:String
    let yBottomBound:CGFloat
    let xPadding:CGFloat = 35.0
    
    init(picName: String, titleText: String, text: String, yBottomBound: CGFloat) {
        self.picName = picName
        self.titleText = titleText
        self.text = text
        self.yBottomBound = yBottomBound
        
        super.init(nibName: nil, bundle:nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    lazy var pictureView:UIImageView = {
        let pic:UIImage = UIImage(named: self.picName)!
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - pic.size.width),
            y: self.view.bounds.size.height - pic.size.height,
            width: pic.size.width,
            height: pic.size.height
        )
        
        let picView:UIImageView = UIImageView(frame: frame)
        picView.image = pic
        
        return picView
    }()
    
    lazy var titleLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont.systemFont(ofSize: 22)
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: self.titleText,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: self.xPadding,
            y: 60.0,
            width: labelWidth,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor.white
        label.text = self.titleText
        label.textAlignment = NSTextAlignment.center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var textLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont.systemFont(ofSize: 12)
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: self.text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: self.xPadding,
            y: self.titleLabel.frame.maxY + 20.0,
            width: labelWidth,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor.white
        label.text = self.text
        label.textAlignment = NSTextAlignment.center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let colors:[CGColor] = [
                UIColor.color(160, green: 200, blue: 224).cgColor,
                UIColor.color(62, green: 83, blue: 121).cgColor
            ]

        let locations = [0.0, 1.0]
        
        let gradientLayer:CAGradientLayer = CAGradientLayer()
        gradientLayer.frame = view.bounds
        gradientLayer.colors = colors
        gradientLayer.locations = locations as [NSNumber]?
        
        self.view.layer.addSublayer(gradientLayer)
        
        self.view.addSubview(self.pictureView)
        self.view.addSubview(self.titleLabel)
        self.view.addSubview(self.textLabel)
    }
}
