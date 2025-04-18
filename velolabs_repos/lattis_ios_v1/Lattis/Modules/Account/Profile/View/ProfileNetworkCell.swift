//
//  ProfileNetworkCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import SDWebImage

class ProfileNetworkCell: ProfileCell {
    @IBOutlet weak var deleteButton: UIButton!
    @IBOutlet weak var companyLabel: UILabel!
    @IBOutlet weak var emailLabel: UILabel!
    @IBOutlet weak var iconView: UIImageView!
    
    override var model: ProfileCell.RowModel? {
        didSet {
            if let model = model, case let .network(network) = model {
                emailLabel.text = network.email
                companyLabel.text = network.name
                if let logo = network.logo {
                    iconView.sd_setImage(with: logo)
                } else {
                    iconView.image = nil
                }
            } else {
                emailLabel.text = nil
                companyLabel.text = nil
                iconView.image = nil
            }
        }
    }
}

