//
//  SLNoEllipseConnectedView.swift
//  Skylock
//
//  Created by Andre Green on 6/29/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//


@objc class SLNoEllipseConnectedView: UIView {
    let xPadding:CGFloat = 20.0
    let yPadding:CGFloat = 10.0
    let text:String
    
    lazy var infoLabel:UILabel = {
        let labelWidth = self.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont.systemFont(ofSize: 14)
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: self.text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: self.xPadding,
            y: 0.5*(self.bounds.size.height - labelSize.height - 2.0*self.yPadding),
            width: labelWidth,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor(white: 155.0/255.0, alpha: 1.0)
        label.text = self.text
        label.textAlignment = NSTextAlignment.center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    @objc init(frame:CGRect, text:String) {
        self.text = text
        super.init(frame: frame)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        self.addSubview(self.infoLabel)
        self.backgroundColor = UIColor.white
    }
}
