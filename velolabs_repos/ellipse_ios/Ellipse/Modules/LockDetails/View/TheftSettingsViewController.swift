//
//  TheftSettingsViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/1/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

protocol TheftSettingsViewControllerDelegate: class {
    func save(sensetivity: Float)
    func getInitialValue() -> Float
}

class TheftSettingsViewController: ViewController {
    @IBOutlet weak var slider: UISlider!
    @IBOutlet weak var saveButton: ValidationButton!
    
    weak var delegate: TheftSettingsViewControllerDelegate?
    fileprivate var initialValue: Float = 0.5
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "theft_detection_settings".localized().uppercased()
        addBackButton()
        if let initial = delegate?.getInitialValue() {
            initialValue = initial
            slider.value = initial
        }
    }
    
    @IBAction func valueChanged(_ sender: Any) {
        saveButton.isValid = slider.value != initialValue
    }
    
    @IBAction func saveChanges(_ sender: Any) {
        delegate?.save(sensetivity: slider.value)
    }
}
