//
//  SettingsFirmwareCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 26/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class SettingsFirmwareCell: SettingsInfoCell {
    @IBOutlet weak var updateButton: UIButton!
    override var model: CellRepresentable? {
        didSet {
            nameLabel.text = "settings_lock_firmware_version".localized()
            let model = self.model as? FWModel
            model?.getFWVersion({ [weak self] result in
                self?.valueLabel.text = result
                guard let version = result else { return }
                model?.checkFWUpdate(version, { [weak self] needUpdate in
                    self?.updateButton.isEnabled = needUpdate
                    self?.updateButton.alpha = needUpdate ? 1 : 0.5
                })
            })
        }
    }
    
    @IBAction func update(_ sender: Any) {
        let model = self.model as? FWModel
        model?.delegate?.updateFirmware()
        updateButton.isEnabled = false
        updateButton.alpha = 0.5
    }
}

extension SettingsFirmwareCell {
    struct FWModel: CellRepresentable {
        var reuseIdentifire: String { return "firmware" }
        let getFWVersion: (@escaping (String?) -> ()) -> ()
        let checkFWUpdate: (String, @escaping (Bool) -> ()) -> ()
        weak var delegate: SettingsCellDelegate?
    }
}
