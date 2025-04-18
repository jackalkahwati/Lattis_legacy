//
//  SettingsViewController.swift
//  Lattis SDK Demo
//
//  Created by Ravil Khusainov on 8/30/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import UIKit

class SettingsViewController: UIViewController {
    @IBOutlet weak var segment: UISegmentedControl!
    @IBOutlet weak var tokenField: UITextView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        segment.selectedSegmentIndex = isProduction ? 1 : 0
//         55 (RK - BJJ) = ea481fa8f91118bedc914de2e806f0f4b8186ba0177e48fecd7b65f8f31a0059
        // 120 (Eco - Velo classic) = 31b853bec21c0c59da69064e6abd8429173a9d6c4c01a7d6241d6f3f5986938e
        // 23 (Velo Labs) = 0efd9af471e944fb204116c408061556782092cd9fbdb5b8f1147d22f5dc1580
//        135 (SDK - Spin) = f490a37ead94a5aa0e95318deeda6c6f948dc59a476f7f9c0629deae8d468e34
//        144 (SDK - Uber) = ee567e76107f2f6f048823e285fbb2bf5f40a953a46517dde2eb62932f196cd8
//        tokenField.text = "ea481fa8f91118bedc914de2e806f0f4b8186ba0177e48fecd7b65f8f31a0059"
        tokenField.text = apiToken
    }
    
    @IBAction func close(_ sender: Any) {
        dismiss(animated: true, completion: nil)
    }
    
    @IBAction func done(_ sender: Any) {
        isProduction = segment.selectedSegmentIndex == 1
        apiToken = tokenField.text ?? ""
        close(sender)
    }
}
