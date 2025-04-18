//
//  SLAddContactButtonView.swift
//  Skylock
//
//  Created by Andre Green on 4/6/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import Foundation

protocol SLAddContactButtonViewDelegate:class {
    func addContactButtonViewTapped(addContactButtonView: SLAddContactButtonView)
}

class SLAddContactButtonView: UIView {
    private var imageData: NSData?
    private var name: String
    private var phoneNumber: String
    private let yPadding: CGFloat = 10.0
    private let xPadding: CGFloat = 10.0
    private let font = UIFont(name:"Helvetica", size:9)
    weak var delegate: SLAddContactButtonViewDelegate?
    private var tgr: UITapGestureRecognizer?
    
    private lazy var picView:UIImageView = {
        let defaultImage: UIImage = UIImage(named: "btn_add_new_contact")!
        let frame =  CGRect(
            x: 0.5*(self.bounds.size.width - defaultImage.size.width),
            y: 0.0,
            width: defaultImage.size.width,
            height: defaultImage.size.height
        )
        let imageView:UIImageView = UIImageView(frame: frame)
        if let imageData = self.imageData, let image:UIImage = UIImage(data: imageData as Data){
            imageView.image = image
        } else {
            imageView.image = defaultImage
        }
        
        let diameter = imageView.bounds.size.width < imageView.bounds.size.height ?
            imageView.bounds.size.width : imageView.bounds.size.height
        imageView.layer.cornerRadius = 0.5*diameter
        
        return imageView
    }()
    
    private lazy var nameLabel: UILabel = {
        let utility: SLUtilities = SLUtilities()
        
        let text = self.name
        let width = self.bounds.size.width
        let size = utility.sizeForLabel(
            font: self.font!,
            text: text,
            maxWidth: width,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        let frame = CGRect(
            x: 0.0,
            y: self.picView.frame.maxY + 5.0,
            width: width,
            height: size.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.text = text
        label.font = self.font
        label.textColor = UIColor(red: 97, green: 100, blue: 100)
        label.numberOfLines = 0
        label.textAlignment = NSTextAlignment.center
        
        return label
    }()
    
    private lazy var phoneNumberLabel: UILabel = {
        let utility: SLUtilities = SLUtilities()
        
        let text = self.phoneNumber
        let width = self.bounds.size.width
        let size = utility.sizeForLabel(
            font: self.font!,
            text: text,
            maxWidth: width,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        let frame = CGRect(
            x: 0.0,
            y: self.nameLabel.frame.maxY,
            width: width,
            height: 20.0
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.text = text
        label.font = self.font
        label.textColor = UIColor(red: 97, green: 100, blue: 100)
        label.numberOfLines = 0
        label.textAlignment = NSTextAlignment.center
        
        return label
    }()
    
    init(frame: CGRect, name: String?, phoneNumber: String?, imageData: NSData?) {
        self.name = name == nil ? NSLocalizedString("Add", comment: "") : name!
        self.phoneNumber = phoneNumber == nil ? "" : phoneNumber!
        self.imageData = imageData
        
        super.init(frame: frame)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        self.isUserInteractionEnabled = true
        
        if self.tgr == nil {
            self.tgr = UITapGestureRecognizer(target: self, action: #selector(viewTapped))
            self.tgr!.numberOfTapsRequired = 1
            
            self.addGestureRecognizer(self.tgr!)
        }
        
        self.addSubview(self.picView)
        self.addSubview(self.nameLabel)
        self.addSubview(self.phoneNumberLabel)
    }
    
    @objc private func viewTapped() {
        if let delegate = self.delegate {
            delegate.addContactButtonViewTapped(addContactButtonView: self)
        }
    }
    
    func updateName(newName: String) {
        self.nameLabel.text = newName
        self.nameLabel.setNeedsDisplay()
    }
    
    func updatePhoneNumber(newPhoneNumber: String) {
        self.phoneNumberLabel.text = newPhoneNumber
        self.phoneNumberLabel.setNeedsDisplay()
    }
    
    func setImage(hasContact: Bool) {
        let imageName:String
        if hasContact {
            imageName = "btn_remove_contact"
        } else {
            imageName = "btn_add_new_contact"
        }
        
        self.picView.image = UIImage(named: imageName)
        self.picView.setNeedsDisplay()
    }
}
