//
//  SeparatorView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class SeparatorView: UIView {
    override func awakeFromNib() {
        super.awakeFromNib()
        constraints(for: .height).first?.constant = 1.0/UIScreen.main.scale
    }
}
