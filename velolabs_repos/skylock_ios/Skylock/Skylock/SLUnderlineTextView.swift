//
//  SLUnderlineTextView.swift
//  Skylock
//
//  Created by Andre Green on 5/28/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLUnderlineTextView: UIView {
    let color:UIColor
    let lineHeight:CGFloat = 1.0
    let placeHolder:String
    
//    lazy var underlineView:UIView = {
//        let frame = CGRect(
//            x: 0,
//            y: self.bounds.size.height - self.lineHeight,
//            width: self.bounds.size.width,
//            height: self.lineHeight
//        )
//        let view:UIView = UIView(frame: frame)
//        view.backgroundColor = self.color
//        
//        return view
//    }()
    
    lazy var textField:UITextField = {
        let frame = CGRect(
            x: 0,
            y: 0,
            width: self.bounds.size.width,
            height: self.bounds.size.height - self.lineHeight
        )
        let field:UITextField = UITextField(frame: frame)
        let text = self.placeHolder
        field.attributedPlaceholder = NSAttributedString(
            string: self.placeHolder,
            attributes: [NSForegroundColorAttributeName: self.color]
        )
        field.textColor = self.color
        field.font = UIFont.systemFont(ofSize: 14)
        return field
    }()
    
    init(frame: CGRect, color: UIColor, placeHolder: String) {
        self.color = color
        self.placeHolder = placeHolder
        
        super.init(frame: frame)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        self.addSubview(self.textField)
        //self.addSubview(self.underlineView)
        self.layer.borderColor = UIColor.white.cgColor
        self.layer.borderWidth = 1.0
    }
    
    func setText(text: String) {
        self.textField.text = text
    }
}
