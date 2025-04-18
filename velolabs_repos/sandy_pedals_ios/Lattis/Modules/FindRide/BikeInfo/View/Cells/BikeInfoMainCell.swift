//
//  BikeInfoMainCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 27/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import SDWebImage

class BikeInfoMainCell: BikeInfoCell {
    @IBOutlet weak var bikeImageView: UIImageView!
    @IBOutlet weak var chargeLevelLabel: UILabel!
    @IBOutlet weak var typeLabel: UILabel!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var descriptionLabel: UILabel!
    
    override var model: TableCellPresentable? {
        didSet {
            if let model = model as? RowModel, case let .about(bike) = model {
                nameLabel.text = bike.name
                chargeLevelLabel.text = bike.bikeBatteryLevelString
                typeLabel.text = bike.bikeType.name
                descriptionLabel.text = bike.description
                if let pic = bike.pic {
                    bikeImageView.sd_setImage(with: pic, placeholderImage: #imageLiteral(resourceName: "placeholder-image"))
                } else {
                    bikeImageView.image = #imageLiteral(resourceName: "placeholder-image")
                }
            } else {
                nameLabel.text = nil
                chargeLevelLabel.text = nil
                typeLabel.text = nil
                descriptionLabel.text = nil
                bikeImageView.image = #imageLiteral(resourceName: "placeholder-image")
            }
        }
    }
}
