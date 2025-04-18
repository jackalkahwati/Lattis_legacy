//
//  StaticAlertView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 9/26/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class StaticAlertView: UIView, AlertView {
    @IBOutlet weak var textLabel: UILabel!
    @IBOutlet weak var lockButton: LockButton!
    
    static func alert(with text: String) -> StaticAlertView {
        let view = StaticAlertView.nib() as! StaticAlertView
        view.textLabel.text = text
        return view
    }
}
