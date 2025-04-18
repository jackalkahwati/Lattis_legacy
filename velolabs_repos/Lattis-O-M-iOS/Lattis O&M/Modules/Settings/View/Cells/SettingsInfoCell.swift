//
//  SettingsInfoCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 26/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

protocol SettingsCellDelegate: class {
    var checkFWUpdate: (String, @escaping (Bool) -> ()) -> () {get set}
    var showAutoLockAlert: (@escaping (Bool) -> ()) -> () {get set}
    func assignBike()
    func updateFirmware()
    func unassignBike()
    func changeLabel()
    func switchCapTouch(isOn: Bool)
}

protocol CellRepresentable {
    var reuseIdentifire: String {get}
}

class SettingsCell: UITableViewCell {
    var model: CellRepresentable?
}

class SettingsInfoCell: SettingsCell {
    @IBOutlet weak var valueLabel: UILabel!
    @IBOutlet weak var nameLabel: UILabel!
    override var model: CellRepresentable? {
        didSet {
            guard let model = model as? Model else { return }
            valueLabel.text = model.value
            nameLabel.text = model.name
            model.callback?({ [weak self] result in
                self?.valueLabel.text = result
            })
        }
    }
}

extension SettingsInfoCell {
    struct Model: CellRepresentable {
        let name: String
        let value: String?
        let callback: ((@escaping (String?) -> ()) -> ())?
        var reuseIdentifire: String { return "info" }
        
        init(name: String, value: String? = nil, callback: ((@escaping (String?) -> ()) -> ())? = nil) {
            self.name = name
            self.value = value
            self.callback = callback
        }
    }
}
