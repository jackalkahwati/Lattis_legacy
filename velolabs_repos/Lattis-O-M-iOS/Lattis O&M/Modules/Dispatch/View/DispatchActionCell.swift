//
//  DispatchActionCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 03/05/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class DispatchActionCell: UITableViewCell {

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var selectImageView: UIImageView!

    var state: BikeState? {
        didSet {
            titleLabel.text = state?.display
        }
    }
}
