//
//  SLLoginTextField.swift
//  Skylock
//
//  Created by Andre Green on 1/22/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLLoginTextField: UITextField {
    let xInset:CGFloat
    let placeHolderText: String
    
    init(frame: CGRect, xInset:CGFloat, placeHolderText:String) {
        self.xInset = xInset
        self.placeHolderText = placeHolderText
        super.init(frame: frame)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func textRect(forBounds bounds: CGRect) -> CGRect {
        return bounds.insetBy(dx: self.xInset, dy: 0)
    }
    
    override func editingRect(forBounds bounds: CGRect) -> CGRect {
        return bounds.insetBy(dx: self.xInset, dy: 0)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        self.placeholder = self.placeHolderText
        self.font = UIFont(name: "HelveticaNeue", size: 17)
        self.textColor = UIColor(red: 146, green: 148, blue: 151)
        self.layer.cornerRadius = 2.0
        self.clipsToBounds = true
        self.backgroundColor = UIColor.white
    }
}
