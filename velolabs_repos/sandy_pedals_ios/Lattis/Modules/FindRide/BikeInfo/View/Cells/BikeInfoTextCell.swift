//
//  BikeInfoTextCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 7/20/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class BikeInfoTextCell: BikeInfoCell {
    @IBOutlet weak var titleLabel: UILabel!
    
    override var model: TableCellPresentable? {
        didSet {
            guard let model = model as? RowModel else { return }
            switch model {
            case .cost(let bike):
                titleLabel.attributedText = bike.infoCostText
            case .deposit(let bike):
                titleLabel.attributedText = bike.infoDepositText
            default:
                titleLabel.text = nil
            }
        }
    }
}
