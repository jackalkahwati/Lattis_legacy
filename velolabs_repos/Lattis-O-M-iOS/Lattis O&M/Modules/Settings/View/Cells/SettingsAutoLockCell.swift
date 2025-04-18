//
//  SettingsAutoLockCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 04.12.2019.
//  Copyright © 2019 Lattis. All rights reserved.
//

import UIKit
import LattisSDK

class SettingsAutoLockCell: SettingsCell {
    @IBOutlet weak var switchControl: UISwitch!
    
    override var model: CellRepresentable? {
        didSet {
            guard let lock = ellipse else { return }
            update(peripheral: lock)
            lock.subscribe(self)
        }
    }
    
    fileprivate var ellipse: Peripheral? { (model as? Model)?.peripheral }
    
    @IBAction func update(_ sender: UISwitch) {
        if sender.isOn, let m = model as? Model {
            m.action() { [unowned self] on in
                sender.isOn = on
                self.ellipse?.isMagnetAutoLockEnabled = on
            }
        } else {
            ellipse?.isMagnetAutoLockEnabled = sender.isOn
        }
    }
    
    fileprivate func update(peripheral: Peripheral) {
        switchControl.isOn = peripheral.isMagnetAutoLockEnabled ?? false
    }
}

extension SettingsAutoLockCell: EllipseDelegate {
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {
        
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
        
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate value: Peripheral.Value) {
        switch value {
        case .magnetAutoLockEnabled:
            update(peripheral: ellipse)
        default:
            break
        }
    }
}

extension SettingsAutoLockCell {
    struct Model: CellRepresentable {
        let reuseIdentifire: String = "autoLock"
        let peripheral: Peripheral?
        let action: (@escaping (Bool) ->()) -> ()
    }
}
