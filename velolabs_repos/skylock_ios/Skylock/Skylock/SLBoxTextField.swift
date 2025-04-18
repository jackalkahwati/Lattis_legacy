//
//  SLBoxTextField.swift
//  Skylock
//
//  Created by Andre Green on 7/29/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

class SLBoxTextField: UITextField {
    private let insetPadding:CGFloat = 10.0
    
    var placeHolderText:String?
    
    let errorColor:UIColor = UIColor(red: 245, green: 153, blue: 174)
    
    let normalTextColor:UIColor = UIColor.white
    
    private var inErrorMode:Bool = false
    
    let textFont:UIFont = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 15.0)!
    
    lazy var errorLabel:UILabel = {
        let frame = CGRect(
            x: 0.5*self.bounds.size.width,
            y: 0.0,
            width: 0.5*self.bounds.size.width - self.insetPadding,
            height: self.bounds.size.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 9.0)
        label.textColor = self.errorColor
        label.textAlignment = .right
        
        return label
    }()
    
    init(frame: CGRect, placeHolder: String) {
        self.placeHolderText = placeHolder
        
        super.init(frame: frame)
        
        if let placeholder = self.placeHolderText {
            self.attributedPlaceholder = NSAttributedString(
                string: placeholder,
                attributes: [
                    NSForegroundColorAttributeName: UIColor(red: 160, green: 200, blue: 224),
                    NSFontAttributeName: self.textFont
                ]
            )
        }
        
        self.textColor = self.normalTextColor
        self.font = self.textFont
        self.layer.borderColor = UIColor.white.cgColor
        self.layer.borderWidth = 1.0
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
    }
    
    override func drawText(in rect: CGRect) {
        let textRect = UIEdgeInsetsInsetRect(rect, self.insets())
        
        super.drawText(in: textRect)
    }
    
    override func drawPlaceholder(in rect: CGRect) {
        let textRect = UIEdgeInsetsInsetRect(rect, self.insets())
        
        super.drawPlaceholder(in: textRect)
    }
    
    override func editingRect(forBounds bounds: CGRect) -> CGRect {
        let textRect = UIEdgeInsetsInsetRect(bounds, self.insets())

        return textRect
    }
    
    private func insets() -> UIEdgeInsets {
        return UIEdgeInsets(top: 0.0, left: self.insetPadding, bottom: 0.0, right: self.insetPadding)
    }
    
    func enterErrorModeWithMessage(message: String) {
        self.inErrorMode = true
        self.errorLabel.text = message
        var frame = CGRect(
            x: 0.5*self.bounds.size.width,
            y: 0.0,
            width: 0.5*self.bounds.size.width - self.insetPadding,
            height: self.bounds.size.height
        )
        if self.rightView != nil {
            frame = frame.offsetBy(dx: -self.rightView!.bounds.size.width, dy: 0.0)
        }
        self.errorLabel.frame = frame
        self.addSubview(self.errorLabel)
    }
    
    func exitErrorMode() {
        self.inErrorMode = false
        self.errorLabel.removeFromSuperview()
    }
    
    func isInErrorMode() -> Bool {
        return self.inErrorMode
    }
}
