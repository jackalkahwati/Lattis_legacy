//
//  LogInLogInView.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import PhoneNumberKit

class LogInView: UIView {
    @IBOutlet weak var phoneField: UITextField!
    @IBOutlet weak var passField: UITextField!
    @IBOutlet weak var submitButton: UIButton!
	
    var submitIsEnabled: Bool = false {
        didSet {
            submitButton.isEnabled = submitIsEnabled
            submitButton.backgroundColor = submitIsEnabled ? .lsTurquoiseBlue : .lsCoolGreyTwo
        }
    }
}
