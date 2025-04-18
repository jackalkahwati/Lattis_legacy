//
//  SLInsetTextField.swift
//  Skylock
//
//  Created by Andre Green on 7/4/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

class SLInsetTextField: UITextField {
    let horizonalInset:CGFloat = 20.0
    
    override func textRect(forBounds bounds: CGRect) -> CGRect {
        return bounds.insetBy(dx: self.horizonalInset, dy: 0.0)
    }
    
    override func editingRect(forBounds bounds: CGRect) -> CGRect {
        return bounds.insetBy(dx: self.horizonalInset, dy: 0.0)
    }
    
    override func placeholderRect(forBounds bounds: CGRect) -> CGRect {
        return bounds.insetBy(dx: self.horizonalInset, dy: 0.0)
    }
}
