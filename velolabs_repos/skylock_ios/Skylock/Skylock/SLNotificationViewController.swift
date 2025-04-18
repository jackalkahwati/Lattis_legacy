//
//  SLNotificationViewController.swift
//  Skylock
//
//  Created by Andre Green on 7/23/16.
//  Copyright © 2016 Andre Green. All rights reserved.
// 
//  This class is meant to be overridden. A subclass needs to set
//  the position of the titleLabel and infoLabel.

import UIKit

protocol SLNotificationViewControllerDelegate:class {
    func cancelButtonPressed(nvc: SLNotificationViewController)
    func takeActionButtonPressed(nvc: SLNotificationViewController)
}

class SLNotificationViewController: UIViewController {
    let takeActionButtonTitle:String
    
    let cancelButtonTitle:String
    
    let titleText:String
    
    let infoText:String
    
    let padding:CGFloat = 15.0
    
    let buttonHeight:CGFloat = 61.0
    
    let utility:SLUtilities = SLUtilities()
    
    weak var delegate:SLNotificationViewControllerDelegate?
        
    lazy var takeActionButton:UIButton = {
        let frame:CGRect = CGRect(
            x: self.padding,
            y: self.padding + UIApplication.shared.statusBarFrame.size.height,
            width: self.view.bounds.size.width - 2.0*self.padding,
            height: self.buttonHeight
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.backgroundColor = self.utility.color(colorCode: .Color87_216_255)
        button.setTitle(self.takeActionButtonTitle, for: .normal)
        button.setTitleColor(self.utility.color(colorCode: .Color255_255_255), for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 18.0)
        button.addTarget(self, action: #selector(takeActionButtonPressed), for: .touchDown)
        
        return button
    }()
    
    lazy var cancelButton:UIButton = {
        let frame:CGRect = CGRect(
            x: self.padding,
            y: self.view.bounds.size.height - self.buttonHeight - self.padding,
            width: self.view.bounds.size.width - 2.0*self.padding,
            height: self.buttonHeight
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.backgroundColor = self.utility.color(colorCode: .Color255_255_255)
        button.setTitle(self.cancelButtonTitle, for: .normal)
        button.setTitleColor(self.utility.color(colorCode: .Color109_194_223), for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 18.0)
        button.addTarget(self, action: #selector(cancelButtonPressed), for: .touchDown)
        
        return button
    }()
    
    lazy var titleLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.padding
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 28.0)!
        let text = self.titleText
        let labelSize:CGSize = self.utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: self.padding,
            y: 0.0,
            width: labelWidth,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = self.utility.color(colorCode: .Color255_255_255)
        label.text = text
        label.textAlignment = .center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var infoLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.padding
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 12.0)!
        let text = self.infoText
        let labelSize:CGSize = self.utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: self.padding,
            y: 0.0,
            width: labelWidth,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = self.utility.color(colorCode: .Color255_255_255)
        label.text = text
        label.textAlignment = .center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    init(takeActionButtonTitle: String, cancelButtonTitle: String, titleText: String, infoText: String) {
        self.takeActionButtonTitle = takeActionButtonTitle
        self.cancelButtonTitle = cancelButtonTitle
        self.titleText = titleText
        self.infoText = infoText
        
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
    
        // In subclasses the label frames should be set here
        
        self.view.backgroundColor = self.utility.color(colorCode: .Color60_83_119)
        
        self.view.addSubview(self.takeActionButton)
        self.view.addSubview(self.cancelButton)
        self.view.addSubview(self.titleLabel)
        self.view.addSubview(self.infoLabel)
    }
    
    func takeActionButtonPressed() {
        self.delegate?.takeActionButtonPressed(nvc: self)
    }
    
    func cancelButtonPressed() {
        self.delegate?.cancelButtonPressed(nvc: self)
    }
}
