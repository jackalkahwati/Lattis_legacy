//
//  ValidationButton.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/25/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class ValidationButton: Button {

    var isValid: Bool = false {
        didSet {
            updateView()
        }
    }
    
    private func updateView() {
        backgroundColor = isValid ? .elDarkSkyBlue : .elPinkishGrey
        isEnabled = isValid
        alpha = isValid ? 1 : 0.8
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        updateView()
    }
}
