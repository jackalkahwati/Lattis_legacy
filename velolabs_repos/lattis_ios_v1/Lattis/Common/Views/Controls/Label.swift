//
//  Label.swift
//  Lattis
//
//  Created by Ravil Khusainov on 3/17/18.
//  Copyright © 2018 Velo Labs. All rights reserved.
//

import UIKit

class Label: UILabel {
    override func awakeFromNib() {
        super.awakeFromNib()
        
        text = text?.localized()
    }
}

class TextView: UITextView {
    override func awakeFromNib() {
        super.awakeFromNib()
        
        text = text.localized()
    }
}
