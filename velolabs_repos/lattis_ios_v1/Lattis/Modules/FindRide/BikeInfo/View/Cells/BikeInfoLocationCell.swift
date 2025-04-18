//
//  BikeInfoLocationCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 27/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class BikeInfoLocationCell: BikeInfoCell {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var addressLabel: UILabel!
    @IBOutlet weak var distanceLabel: UILabel!
    
    override var model: TableCellPresentable? {
        didSet {
            if let model = self.model as? RowModel, case let .location(title, address) = model {
                addressLabel.text = address
                titleLabel.text = title
            } else {
                addressLabel.text = nil
                titleLabel.text = nil
            }
        }
    }
}
