//
//  LockSettingsViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 17/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//
// !!!NOT USED!!!

import UIKit
import LattisSDK

class LockSettingsViewController: ViewController {
    @IBOutlet weak var settingsView: LockSettingsView!
    var interactor: LockSettingsInteractorInput!
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "LOCK SETTINGS".localized()
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close(_:)))
        navigationController?.isNavigationBarHidden = false
        settingsView.slider.addTarget(self, action: #selector(lockStateChanged(_:)), for: .valueChanged)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
//        lock.peripheral?.subscribe(self, to: update(state: ))
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
//        lock.peripheral?.unsubsribe(self)
    }
    
    @objc private func close(_ sender: Any) {
        navigationController?.dismiss(animated: true, completion: nil)
//        navigationController?.popViewController(animated: true)
    }
    
    @objc private func lockStateChanged(_ sender: LockSlider) {
        if case .processing(let state) = sender.lockState {
            // FIXME:
            DispatchQueue.main.asyncAfter(deadline: .now() + 2, execute: {
                self.settingsView.lockState = state
            })
        }
    }
    
    private func update(state: LattisSDK.Ellipse.Security) {
        switch state {
        case .locked:
            settingsView.slider.lockState = .locked
        case .unlocked:
            settingsView.slider.lockState = .unlocked
        default:
            break
        }
    }
    
    @IBAction func hintAction(_ sender: Any) {
        settingsView.switchHint()
    }
}

extension LockSettingsViewController: LockSettingsInteractorOutput {
    
}
