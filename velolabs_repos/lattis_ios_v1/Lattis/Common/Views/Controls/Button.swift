//
//  Button.swift
//  Lattis
//
//  Created by Ravil Khusainov on 3/17/18.
//  Copyright © 2018 Velo Labs. All rights reserved.
//

import UIKit

class Button: UIButton {
    override func awakeFromNib() {
        super.awakeFromNib()
        
        let states: [UIControl.State] = [.normal, .selected, .highlighted, .disabled]
        states.forEach { (state) in
            let title = self.title(for: state)
            self.setTitle(title?.localized(), for: state)
        }
    }
}
