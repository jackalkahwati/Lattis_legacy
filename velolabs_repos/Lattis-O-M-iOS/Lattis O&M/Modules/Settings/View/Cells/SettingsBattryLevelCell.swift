//
//  SettingsBattryLevelCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08.01.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import UIKit
import LattisSDK

class SettingsBattryLevelCell: SettingsCell {

    @IBOutlet weak var valueLabel: UILabel!
    
    override var model: CellRepresentable? {
        didSet {
            guard let peripheral = (model as? Model)?.peripheral else { return }
            peripheral.subscribe(self)
        }
    }
    
    fileprivate func update(metadata: Peripheral.Metadata) {
        if metadata.isCharging {
            valueLabel.text = "⚡USB"
        } else {
            valueLabel.text = "\(Int(metadata.batteryLevel*100))%"
        }
    }
}

extension SettingsBattryLevelCell: EllipseDelegate {
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {}
    
    func ellipse(_ ellipse: Peripheral, didUpdate value: Peripheral.Value) {
        switch value {
        case .metadata(let meta):
            update(metadata: meta)
        default:
            break
        }
    }
}

extension SettingsBattryLevelCell {
    struct Model: CellRepresentable {
        let peripheral: Peripheral?
        var reuseIdentifire: String { "battryLevel" }
    }
}
