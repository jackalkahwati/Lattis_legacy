//
//  SLMapCalloutView.swift
//  Skylock
//
//  Created by Andre Green on 1/25/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

protocol SLMapCalloutViewDelegate:class {
    func calloutViewTapped(calloutView: SLMapCalloutView)
}

class SLMapCalloutView: UIView {
    let upperText:String
    let lowerText:String
    let selectedImageName:String
    let deselectedImageName:String
    var isSelected:Bool = false
    weak var delegate:SLMapCalloutViewDelegate?
    
    init(frame:CGRect, upperText:String, lowerText:String, selectedImageName:String, deselectedImageName:String) {
        self.upperText = upperText
        self.lowerText = lowerText
        self.selectedImageName = selectedImageName
        self.deselectedImageName = deselectedImageName
        
        super.init(frame: frame)
    }

    required init?(coder aDecoder: NSCoder) {
        self.upperText = ""
        self.lowerText = ""
        self.selectedImageName = ""
        self.deselectedImageName = ""
        
        super.init(coder: aDecoder)
    }
    
    lazy var imageView: UIImageView = {
        let image = UIImage(named: self.deselectedImageName)
        let frame = CGRect(
            x: 0.5*(self.bounds.size.width - image!.size.width),
            y: 10,
            width: image!.size.width,
            height: image!.size.height
        )
        
        let view:UIImageView = UIImageView(image: image)
        view.frame = frame
        
        return view
    }()
    
    lazy var upperLabel: UILabel = {
        let frame = CGRect(
            x: 0,
            y: self.imageView.frame.maxY,
            width: self.bounds.size.width,
            height: 16.0
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.text = self.upperText
        label.font = UIFont(name:"Helvetica", size:13)
        label.textAlignment = NSTextAlignment.center
        
        return label
    }()
    
    lazy var lowerLabel: UILabel = {
        let frame = CGRect(
            x: 0,
            y: self.upperLabel.frame.maxY,
            width: self.bounds.size.width,
            height: 12.0
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.text = self.lowerText
        label.font = UIFont(name:"Helvetica", size:10)
        label.textAlignment = NSTextAlignment.center
        
        return label
    }()
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let tgr:UITapGestureRecognizer = UITapGestureRecognizer(
            target: self,
            action: #selector(viewTapped(tgr:))
        )
        tgr.numberOfTapsRequired = 1
        
        self.addGestureRecognizer(tgr)
        
        self.addSubview(self.imageView)
        self.addSubview(self.upperLabel)
        self.addSubview(self.lowerLabel)
    }
    
    func viewTapped(tgr: UITapGestureRecognizer) {
        if let delegate = self.delegate {
            delegate.calloutViewTapped(calloutView: self)
        }
        
        self.isSelected = !self.isSelected
        self.setSelectedImage()
    }
    
    func setSelected(isSelected: Bool) {
        self.isSelected = isSelected
        self.setSelectedImage()
    }
    
    func setSelectedImage() {
        self.imageView.image = self.isSelected ?
        UIImage(named: self.selectedImageName) : UIImage(named: self.deselectedImageName)
    }
}
