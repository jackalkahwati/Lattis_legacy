//
//  LockViewController.swift
//  BLEService
//
//  Created by Ravil Khusainov on 14/02/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import RestService

class LockViewController: UIViewController {
    @IBOutlet weak var devZLabel: UILabel!
    @IBOutlet weak var devYLabel: UILabel!
    @IBOutlet weak var devXLabel: UILabel!
    @IBOutlet weak var mavZLabel: UILabel!
    @IBOutlet weak var mavYLabel: UILabel!
    @IBOutlet weak var mavXLabel: UILabel!
    @IBOutlet weak var bytesLabel: UILabel!
    @IBOutlet weak var magZSlider: UISlider!
    @IBOutlet weak var magYSlider: UISlider!
    @IBOutlet weak var magXSlider: UISlider!
    @IBOutlet weak var magZThr: UILabel!
    @IBOutlet weak var magYThr: UILabel!
    @IBOutlet weak var magXThr: UILabel!
    @IBOutlet weak var magXCur: UILabel!
    @IBOutlet weak var magYCur: UILabel!
    @IBOutlet weak var magZCur: UILabel!
    @IBOutlet weak var crashThreshold: UILabel!
    @IBOutlet weak var theftThreshold: UILabel!
    @IBOutlet weak var lockSwitch: UISwitch!
    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var theftSwitch: UISwitch!
    @IBOutlet weak var crashSwitch: UISwitch!
    @IBOutlet weak var theftSlider: UISlider!
    @IBOutlet weak var crashSlider: UISlider!
    var lock: Peripheral!
    
    fileprivate var storage: Storage = UDStorage()
    
    override func viewDidLoad() {
        super.viewDidLoad()
    
        connect()
        
        storage.onTheftEnable = { enable in
            if enable {
                self.lock.accelerometerHandler.subscrybeTheft(handler: self)
                self.theftSlider.value = self.lock.accelerometerHandler.theftLimit.sensetivity
            } else {
                self.lock.accelerometerHandler.unsubscrybeTheft(handler: self)
            }
        }
        
        storage.onTheftSensetivityChange = { sensetivity in
            self.lock.accelerometerHandler.theftLimit.sensetivity = sensetivity
            self.theftThreshold.text = String(format: "%.0f", self.lock.accelerometerHandler.theftLimit.threshold)
            self.view.layoutIfNeeded()
        }
        
        storage.onCrashSensetivityChange = { sensetivity in
            self.lock.accelerometerHandler.crashLimit.sensetivity = sensetivity
            self.crashThreshold.text = String(format: "%.0f", self.lock.accelerometerHandler.crashLimit.threshold)
            self.view.layoutIfNeeded()
        }
        
        storage.onCrashEnable = { enable in
            if enable {
                self.lock.accelerometerHandler.subscrybeCrash(handler: self)
                self.theftSlider.value = self.lock.accelerometerHandler.crashLimit.sensetivity
            } else {
                self.lock.accelerometerHandler.unsubscrybeCrash(handler: self)
            }
        }
        
        theftSlider.isEnabled = storage.isTheftEnabled
        theftSwitch.isOn = storage.isTheftEnabled
        crashSlider.isEnabled = storage.isCrashEnabled
        crashSwitch.isOn = storage.isCrashEnabled
    }

    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        if storage.isTheftEnabled {
            lock.accelerometerHandler.subscrybeTheft(handler: self)
        }
        if storage.isCrashEnabled {
            lock.accelerometerHandler.subscrybeCrash(handler: self)
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        lock.accelerometerHandler.unsubscrybeTheft(handler: self)
        lock.accelerometerHandler.unsubscrybeCrash(handler: self)
    }
    
    deinit {
        BLEService.shared.disconnect(lock)
    }
    
    @IBAction func lockUnlockAction(_ sender: UISwitch) {
        do {
            let state: Peripheral.LockState = sender.isOn ? .locked : .unlocked
            try lock.set(lockState: state)
        } catch {
            print(error)
        }
    }
    
    @IBAction func update(_ sender: Any?) {
        let url = Bundle.main.url(forResource: "FW", withExtension: "BIN")!
        lock.updateFromBinary(url: url, progress: {print($0)})
    }
    
    func connect() {
        BLEService.shared.stopScan()
        BLEService.shared.connect(lock)
        lock.subscribe(delegate: self)
//        register {
//            self.sign(completion: { (message, key) in
//                self.lock.publicKey = key
//                self.lock.signedMessage = message
//                self.lock.userId = UserDefaults.standard.string(forKey: "userId")
//                
//            })
//        }
    }
    
    @IBAction func clean(_ sender: Any) {
        isReg = false
        signed = nil
    }
    

    @IBAction func deleteLock(_ sender: Any?) {
        Oval.locks.delete(lock: lock.macId, success: {
            self.clean(self)
            BLEService.shared.disconnect(self.lock)
            self.statusLabel.text = "Deleted"
        }) { (_) in
            
        }
    }
    
    func register(completion: @escaping () -> ()) {
        if isReg {
            return completion()
        }
        Oval.locks.registration(with: lock.macId, success: { (lock) in
            self.isReg = true
            completion()
        }) { (_) in
            self.isReg = true
            completion()
        }
    }
    
    func sign(completion: @escaping (String, String) -> ()) {
        if let signed = signed {
            return completion(signed.message, signed.key)
        }
        Oval.locks.signLock(with: lock.macId, success: { (signedM, publicKey) in
            self.signed = (signedM, publicKey)
            completion(signedM, publicKey)
        }) { (_) in
            
        }
    }
    
    var isReg: Bool {
        set {
            UserDefaults.standard.set(newValue, forKey: "\(self.lock.macId)-register")
            UserDefaults.standard.synchronize()
        }
        get {
            return UserDefaults.standard.bool(forKey: "\(self.lock.macId)-register")
        }
    }
    
    var signed:(message: String, key: String)? {
        set {
            UserDefaults.standard.set(newValue?.message, forKey: "\(self.lock.macId)-message")
            UserDefaults.standard.set(newValue?.key, forKey: "\(self.lock.macId)-key")
            UserDefaults.standard.synchronize()
        }
        get {
            guard let message = UserDefaults.standard.string(forKey: "\(self.lock.macId)-message"),
                let key = UserDefaults.standard.string(forKey: "\(self.lock.macId)-key") else {
                return nil
            }
            return (message, key)
        }
    }
    
    @IBAction func switchTheft(_ sender: UISwitch) {
        storage.isTheftEnabled = sender.isOn
        theftSlider.isEnabled = sender.isOn
    }
    
    @IBAction func switchCrash(_ sender: UISwitch) {
        storage.isCrashEnabled = sender.isOn
        crashSlider.isEnabled = sender.isOn
    }
    
    @IBAction func crashSensetivityChanged(_ sender: UISlider) {
        storage.crashSensetivity = sender.value
    }
    
    @IBAction func theftSensetivityChanged(_ sender: UISlider) {
        storage.theftSensetivity = sender.value
    }
    
    @IBAction func magXvalueChanged(_ sender: UISlider) {
        magXThr.text = "\(Int(sender.value))"
    }
    
    @IBAction func magYvalueChanged(_ sender: UISlider) {
        magYThr.text = "\(Int(sender.value))"
    }
    
    @IBAction func magZvalueChanged(_ sender: UISlider) {
        magZThr.text = "\(Int(sender.value))"
    }
}

extension LockViewController: TheftPresentable, CrashPresentable {
    func handleTheft(value: AccelerometerValue) {
        let alert = UIAlertController(title: "Theft detected", message: "\(value)", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: nil))
        present(alert, animated: true, completion: nil)
    }
    
    func handleCrash(value: AccelerometerValue) {
        let alert = UIAlertController(title: "Crash detected", message: "\(value)", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: nil))
        present(alert, animated: true, completion: nil)
    }
}

extension LockViewController: PeripheralDelegate {
    func peripheral(_ peripheral: Peripheral, didUpdate magnitude: Coordinate) {
        let coordinate = magnitude
        magXCur.text = "\(Int(coordinate.x))"
        magYCur.text = "\(Int(coordinate.y))"
        magZCur.text = "\(Int(coordinate.z))"
        
        magXCur.backgroundColor = magXSlider.value < coordinate.x ? .green : .clear
        
        magYCur.backgroundColor = magYSlider.value < coordinate.y ? .green : .clear
        
        magZCur.backgroundColor = magZSlider.value < coordinate.z ? .green : .clear
    }
    
    func peripheral(_ peripheral: Peripheral, didUpdate metadata: Peripheral.Metadata) {
        print(metadata)
    }
    
    func peripheral(_ peripheral: Peripheral, got firmwareVersion: String) {
        print(firmwareVersion)
    }
    
    func peripheral(_ peripheral: Peripheral, didChangeLock state: Peripheral.LockState) {
        lockSwitch.isOn = state == .unlocked
    }
    
    func peripheral(_ peripheral: Peripheral, didChangeConnection state: Peripheral.Connection) {
        var text = "Connecting"
        switch state {
        case .paired:
            text = "Connected"
        default:
            break
        }
        statusLabel.text = text
    }
    
    func peripheral(_ peripheral: Peripheral, didUpdate accelerometer: AccelerometerValue) {
        mavXLabel.text = "\(accelerometer.mav.x)"
        mavYLabel.text = "\(accelerometer.mav.y)"
        mavZLabel.text = "\(accelerometer.mav.z)"
        
        devXLabel.text = "\(accelerometer.deviation.x)"
        devYLabel.text = "\(accelerometer.deviation.y)"
        devZLabel.text = "\(accelerometer.deviation.z)"
    }
}

extension Oval.Locks: Network {
    public func sign(lockWith macId: String, success: @escaping (String, String, String) -> (), fail: @escaping (Error) -> ()) {
        func sign() {
            self.signLock(with: macId, success: { (message, key) in
                let userId = String(Oval.userId!)
                success(message, key, userId)
            }, fail: fail)
        }
        registration(with: macId, success: { (lock) in
            sign()
        }, fail: { error in
            guard let err = error as? Oval.Error, err == .conflict else { return fail(error) }
            sign()
        })
    }
}
