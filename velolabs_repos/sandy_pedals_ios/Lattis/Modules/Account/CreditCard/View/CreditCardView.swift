//
//  CreditCardView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 6/30/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Cartography

class CreditCardView: UIView {
    @IBOutlet weak var numberField: UITextField!
    @IBOutlet weak var dateField: FloatLabelTextField!
    @IBOutlet weak var cvvField: FloatLabelTextField!
    @IBOutlet var saveButton: UIButton!
    
    fileprivate weak var activeField: UITextField?
    
    let cardIconView: UIImageView = {
        let view = UIImageView(image: #imageLiteral(resourceName: "icon_credit_card"))
        view.contentMode = .left
        view.frame = CGRect(x: 0, y: 0, width: 48, height: 30)
        return view
    }()
    
    let scanButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(#imageLiteral(resourceName: "icon_card_photo"), for: .normal)
        button.frame = CGRect(x: 0, y: 0, width: 25, height: 20)
        return button
    }()
    
    let countryIconView: UIImageView = {
        let view = UIImageView(image: #imageLiteral(resourceName: "icon_flag_us"))
        view.contentMode = .left
        view.frame = CGRect(x: 0, y: 0, width: 50, height: 30)
        return view
    }()
    
    let disclosureIconView: UIImageView = {
        let view = UIImageView(image: #imageLiteral(resourceName: "icon_disclosure"))
        view.contentMode = .right
        view.frame = CGRect(x: 0, y: 0, width: 20, height: 30)
        return view
    }()
    
    override func awakeFromNib() {
        super.awakeFromNib()
//        numberField.rightViewMode = .always
//        numberField.rightView = scanButton
        
        numberField.leftViewMode = .always
        numberField.leftView = cardIconView
        
        saveButton.backgroundColor = .lsCoolGreyTwo
        saveButton.isEnabled = false
        
        dateField.placeholder = "credit_card_date".localized()
        cvvField.placeholder = "credit_card_cvv".localized()
    }
    
    var canSave: Bool = false {
        didSet {
            saveButton.backgroundColor = canSave ? .lsTurquoiseBlue : .lsCoolGreyTwo
            saveButton.isEnabled = canSave
        }
    }
    func keyboardWillShow(_ textField: UITextField) {
        if saveButton.superview == self {
            saveButton.removeFromSuperview()
        }
        textField.inputAccessoryView = saveButton
        activeField?.inputAccessoryView = nil
        activeField = textField
        layoutIfNeeded()
    }
}
