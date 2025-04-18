//
//  SLTextBoxFieldWithButton.swift
//  Skylock
//
//  Created by Andre Green on 7/29/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

protocol SLBoxTextFieldWithButtonDelegate:class {
    func showButtonToggledToShow(textField: SLBoxTextFieldWithButton, shouldShow: Bool)
}

class SLBoxTextFieldWithButton: SLBoxTextField {
    weak var textBoxDelegate: SLBoxTextFieldWithButtonDelegate?
    
    lazy var showButton:UIButton = {
        let width:CGFloat = 40.0
        let height:CGFloat = 30.0
        let frame = CGRect(
            x: self.bounds.size.width - width - 10.0,
            y: 0.5*(self.bounds.size.height - height),
            width: width,
            height: height
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.backgroundColor = UIColor.clear
        button.setTitle(NSLocalizedString("SHOW", comment: ""), for: .normal)
        button.setTitle(NSLocalizedString("HIDE", comment: ""), for: .selected)
        button.setTitleColor(UIColor(red: 87, green: 216, blue: 255), for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 10.0)
        button.addTarget(self, action: #selector(showButtonPressed), for: .touchDown)
        
        return button
    }()
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        self.rightViewMode = UITextFieldViewMode.always
        self.rightView = self.showButton
        self.rightView?.frame = self.rightView!.frame.offsetBy(dx: -5.0, dy: 0.0)
    }
    
    func showButtonPressed() {
        self.showButton.isSelected = !self.showButton.isSelected
        self.textBoxDelegate?.showButtonToggledToShow(textField: self, shouldShow: self.showButton.isSelected)
    }
    
    override func exitErrorMode() {
        super.exitErrorMode()
        self.rightView = self.showButton
    }
}
