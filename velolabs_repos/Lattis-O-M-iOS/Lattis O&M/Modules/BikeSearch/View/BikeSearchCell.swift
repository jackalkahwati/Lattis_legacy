//
//  BikeSearchCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 01/05/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class BikeSearchCell: UITableViewCell {
    @IBOutlet weak var iconView: UIImageView!
    @IBOutlet weak var nameLabel: UILabel!

    var bike: Bike? {
        didSet {
            nameLabel.text = bike?.name
        }
    }
}
