//
//  SettingsEmptyBikeCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 26/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class SettingsEmptyBikeCell: SettingsCell {
    @IBAction func assing(_ sender: Any) {
        let model = self.model as? Model
        model?.delegate?.assignBike()
    }
}

extension SettingsEmptyBikeCell {
    struct Model: CellRepresentable {
        var reuseIdentifire: String { return "emptyBike" }
        weak var delegate: SettingsCellDelegate?
        init(delegate: SettingsCellDelegate) {
            self.delegate = delegate
        }
    }
}
