//
//  Controls.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class Button: UIButton {
    override func awakeFromNib() {
        super.awakeFromNib()
        let ttl = title(for: .normal)
        setTitle(ttl?.localized(), for: .normal)
    }
}

class Label: UILabel {
    override func awakeFromNib() {
        super.awakeFromNib()
        text = text?.localized()
    }
}
