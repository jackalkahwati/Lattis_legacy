//
//  SettingsSwithcCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 11/03/2019.
//  Copyright © 2019 Lattis. All rights reserved.
//

import UIKit
import LattisSDK

class SettingsCapTouchCell: SettingsCell {
    @IBOutlet weak var switchControl: UISwitch!
    
    override var model: CellRepresentable? {
        didSet {
            guard let peripheral = (model as? Model)?.peripheral else { return }
            if peripheral.firmwareVersion == nil || peripheral.isCapTouchEnabled == nil {
                peripheral.subscribe(self)
            }
            updateUI(peripheral: peripheral)
        }
    }
    
    @IBAction func update(_ sender: UISwitch) {
        guard let peripheral = (model as? Model)?.peripheral else { return }
        peripheral.isCapTouchEnabled = sender.isOn
    }
    
    fileprivate func updateUI(peripheral: Peripheral) {
        let version = peripheral.firmwareVersion?.replacingOccurrences(of: ".", with: "") ?? "0"
        let enabled = peripheral.isCapTouchEnabled != nil && version >= capFixFW
        switchControl.isOn = peripheral.isCapTouchEnabled ?? false
        switchControl.isEnabled = enabled
        contentView.alpha = enabled ? 1 : 0.5
        if enabled &&
            !peripheral.isCapTouchDesabled &&
            switchControl.isOn {
            peripheral.isCapTouchEnabled = false
            peripheral.isCapTouchDesabled = true
            switchControl.isOn = false
        }
    }
}

extension SettingsCapTouchCell: EllipseDelegate {
    
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {
        
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
        
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate value: Peripheral.Value) {
        switch value {
        case .firmwareVersion, .capTouchEnabled:
            updateUI(peripheral: ellipse)
        default:
            break
        }
    }
}

extension SettingsCapTouchCell {
    struct Model: CellRepresentable {
        let reuseIdentifire: String = "capTouch"
        let peripheral: Peripheral?
    }
}

extension Peripheral {
    fileprivate var capTouchKey: String {
        return "Ellipse." + macId + ".isCapTouchDesabled"
    }
    
    var isCapTouchDesabled: Bool {
        set {
            UserDefaults.standard.set(newValue, forKey: capTouchKey)
        }
        get {
//            return false
            return UserDefaults.standard.bool(forKey: capTouchKey)
        }
    }
}
