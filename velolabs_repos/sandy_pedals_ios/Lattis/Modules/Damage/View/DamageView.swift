//
//  DamageDamageView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

class DamageView: UIView {
    @IBOutlet weak var submitButton: UIButton!
    @IBOutlet weak var textLabel: UILabel!
    @IBOutlet weak var typeLabel: UILabel!
    @IBOutlet weak var typeField: UITextField!
    @IBOutlet weak var notesLabel: UILabel!
    @IBOutlet weak var notesTextLabel: UILabel!
    @IBOutlet weak var photoLabel: UILabel!
    @IBOutlet weak var noPhotoLabel: UILabel!
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var noPhotoBottomLayout: NSLayoutConstraint!
    @IBOutlet weak var imageBottomLayout: NSLayoutConstraint!
    let pickerView = UIPickerView()
    
    func show(image: UIImage) {
        noPhotoLabel.isHidden = true
        imageView.image = image
        noPhotoBottomLayout.priority = UILayoutPriority(rawValue: 800)
        imageBottomLayout.priority = UILayoutPriority(rawValue: 900)
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        typeField.inputView = pickerView
        pickerView.backgroundColor = .lsWhite
    }
    
    var isSubmitEnabled: Bool = false {
        didSet {
            let textColor: UIColor = isSubmitEnabled ? .white : .lsCoolGrey
            let bgColor: UIColor = isSubmitEnabled ? .lsTurquoiseBlue : .lsSilver
            submitButton.isEnabled = isSubmitEnabled
            submitButton.setTitleColor(textColor, for: .normal)
            submitButton.backgroundColor = bgColor
        }
    }
}
