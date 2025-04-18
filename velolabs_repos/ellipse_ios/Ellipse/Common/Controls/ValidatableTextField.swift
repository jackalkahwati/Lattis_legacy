//
//  ValidatableTextField.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/25/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class ValidatableTextField: UITextField, Validatable {
    var inputType: InputValidator.InputType = .none
}

extension UITextField {
    static var loginAttributes: [NSAttributedString.Key: Any] { return [NSAttributedString.Key.foregroundColor: UIColor.elLightBlueGrey] }
    func placeholderSet(attributes: [NSAttributedString.Key: Any]) {
        guard let holder = placeholder else { return }
        attributedPlaceholder = NSAttributedString(string: holder.localized(), attributes: attributes)
    }
    
    func localizePlaceholder() {
        placeholder = placeholder?.localized()
    }
}
