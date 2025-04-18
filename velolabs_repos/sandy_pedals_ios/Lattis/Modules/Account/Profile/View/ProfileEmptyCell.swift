//
//  ProfileEmptyCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class ProfileEmptyCell: ProfileCell {
    @IBOutlet weak var titleLabel: UILabel!

    override var model: ProfileCell.RowModel? {
        didSet {
            if let model = model, case let .empty(title) = model {
                titleLabel.text = title
            } else {
                titleLabel.text = nil
            }
        }
    }
}
