//
//  SLOposingLabelsTableViewCell.swift
//  Skylock
//
//  Created by Andre Green on 6/9/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

protocol SLOpposingLabelsTableViewCellDelegate:class {
    func opposingLabelsCellTextFieldBecameFirstResponder(cell: SLOpposingLabelsTableViewCell)
    func opposingLablesCellTextFieldChangeEventOccured(cell: SLOpposingLabelsTableViewCell)
}

class SLOpposingLabelsTableViewCell: UITableViewCell, UITextFieldDelegate {
    let xPadding:CGFloat = 15.0
    
    let labelHeight:CGFloat = 14.0
    
    let labelFont:UIFont = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)!
    
    weak var delegate:SLOpposingLabelsTableViewCellDelegate?
    
    var isEditable:Bool = true
    
    var showArrow:Bool = false
    
    lazy var leftLabel:UILabel = {
        let frame = CGRect(
            x: self.xPadding,
            y: 0.0,
            width: 0.5*self.contentView.bounds.size.width - self.xPadding,
            height: self.contentView.bounds.size.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.font = UIFont(name: SLFont.OpenSansSemiBold.rawValue, size: 14)
        label.text = ""
        label.adjustsFontSizeToFitWidth = true
        return label
    }()
    
    
    func condenseWhiteSpace(string: String) -> String {
        return string.characters
            .split { $0 == " " }
            .map { String($0) }
            .joined(separator: " ")
    }
    
    lazy var rightField:UITextField = {
        let frame = CGRect(
            x: 0.5*self.bounds.size.width,
            y: 0.0,
            width: 0.5*self.bounds.size.width - self.xPadding,
            height: self.contentView.bounds.size.height
        )
        
        let field:UITextField = UITextField(frame: frame)
        field.font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14)
        field.text = ""
        field.textAlignment = .right
        field.delegate = self
        return field
    }()

    lazy var arrowView:UIImageView = {
        let image:UIImage = UIImage(named: "lock_settings_right_arrow")!
        let imageView = UIImageView(image: image)
        
        return imageView
    }()
    
    override func layoutSubviews() {
        super.layoutSubviews()
        if self.showArrow {
            self.accessoryView = self.arrowView
        }
        
        self.leftLabel.frame = CGRect(
            x: self.xPadding,
            y: 0.0,
            width: leftLabel.sizeThatFits(contentView.bounds.size).width,
            height: self.contentView.bounds.size.height
        )
        
        let width = self.showArrow ?
            contentView.bounds.width - leftLabel.frame.maxX - self.arrowView.bounds.size.width - self.xPadding*2 - 20.0 :
            contentView.bounds.width - leftLabel.frame.maxX - self.xPadding*2
        
        self.rightField.frame = CGRect(
            x: leftLabel.frame.maxX + xPadding,
            y: 0.0,
            width: width,
            height: self.contentView.bounds.size.height
        )
        
        self.contentView.addSubview(self.leftLabel)
        self.contentView.addSubview(self.rightField)
    }
    
    override func prepareForReuse() {
        super.prepareForReuse()
        self.accessoryView = nil
    }
    
//    override func setSelected(selected: Bool, animated: Bool) {
//        let view:UIView = UIView(frame: self.bounds)
//        //view.backgroundColor = UIColor.clearColor()
//        view.backgroundColor = UIColor.redColor()
//        self.selectedBackgroundView = view
//    }
    
    func setProperties(
        leftLabelText:String,
        rightLabelText:String?,
        leftLabelTextColor:UIColor,
        rightLabelTextColor:UIColor,
        shouldEnableTextField: Bool
        )
    {
        self.leftLabel.text = leftLabelText
        self.leftLabel.textColor = leftLabelTextColor
        self.rightField.text = rightLabelText
        self.rightField.textColor = rightLabelTextColor
        self.rightField.isEnabled = shouldEnableTextField
    }
    
    func haveFieldResignFirstReponder() {
        self.rightField.resignFirstResponder()
    }
    
    func isTextFieldFirstResponder() -> Bool {
        return self.rightField.isFirstResponder
    }
    
    func setTextFieldEnabled(shouldEnable: Bool) {
        self.rightField.isEnabled = shouldEnable
    }
    
    func haveFieldBecomeFirstResponder() {
        DispatchQueue.main.async {
            self.rightField.becomeFirstResponder()
        }
    }
    
    // MARK: UITextFieldDelegate methods
    func textFieldDidBeginEditing(_ textField: UITextField) {
        self.delegate?.opposingLabelsCellTextFieldBecameFirstResponder(cell: self)
    }
    
    func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        return self.isEditable
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        textField.resignFirstResponder()
        self.delegate?.opposingLablesCellTextFieldChangeEventOccured(cell: self)
    }
}
