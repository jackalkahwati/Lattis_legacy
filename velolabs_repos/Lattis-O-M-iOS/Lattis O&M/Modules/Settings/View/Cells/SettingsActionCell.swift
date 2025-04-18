//
//  SettingsActionCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 26/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class SettingsActionCell: SettingsCell {
    @IBOutlet weak var titleLabel: UILabel!
    
    override var model: CellRepresentable? {
        didSet {
            let mod = model as? Model
            self.titleLabel.text = mod?.title
            self.accessoryType = (mod != nil && mod!.disclosure) ? .disclosureIndicator : .none
        }
    }
}

extension SettingsActionCell {
    struct Model: CellRepresentable {
        var reuseIdentifire: String { return "action" }
        var title: String?
        var action: () -> ()
        var disclosure: Bool
        init(title: String?, action: @escaping () -> (), disclosure: Bool = false) {
            self.title = title
            self.action = action
            self.disclosure = disclosure
        }
    }
}
