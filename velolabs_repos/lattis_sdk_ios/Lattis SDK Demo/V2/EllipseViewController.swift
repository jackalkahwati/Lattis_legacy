//
//  EllipseViewController.swift
//  Lattis SDK Demo
//
//  Created by Ravil Khusainov on 8/2/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import UIKit
import LattisSDK
import GradientCircularProgress

class EllipseViewController: UIViewController {
    @IBOutlet weak var firmwareLabel: UILabel!
    @IBOutlet weak var macLabel: UILabel!
    @IBOutlet weak var serialLabel: UILabel!
    @IBOutlet weak var securitySwitch: UISwitch!
    @IBOutlet weak var updateButton: UIButton!
    @IBOutlet weak var manualLockContainer: UIView!
    
    @IBOutlet weak var batteryLabel: UILabel!
    @IBOutlet weak var rssiLabel: UILabel!
    @IBOutlet weak var theftSwitch: UISwitch!
    @IBOutlet weak var crashSwitch: UISwitch!
    @IBOutlet weak var theftThLabel: UILabel!
    @IBOutlet weak var crashThLabel: UILabel!
    @IBOutlet weak var theftSlider: UISlider!
    @IBOutlet weak var crashSlider: UISlider!
    @IBOutlet weak var xMavLabel: UILabel!
    @IBOutlet weak var yMavLabel: UILabel!
    @IBOutlet weak var zMavLabel: UILabel!
    @IBOutlet weak var xDevLabel: UILabel!
    @IBOutlet weak var yDevLabel: UILabel!
    @IBOutlet weak var zDevLabel: UILabel!
    @IBOutlet weak var pinCodeLabel: UILabel!
    @IBOutlet weak var magLockSwitch: UISwitch!
    @IBOutlet weak var voltageLabel: UILabel!
    @IBOutlet weak var magXLabel: UILabel!
    @IBOutlet weak var magYLabel: UILabel!
    @IBOutlet weak var magZLabel: UILabel!
    
    var ellipse: Ellipse!
    fileprivate let network = EllipseManager.shared.network
    
    fileprivate var progressView: GradientCircularProgress?
    fileprivate var isAutoLockEnabled: Bool = false {
        didSet {
            manualLockContainer.alpha = isAutoLockEnabled ? 0.5 : 1
            manualLockContainer.isUserInteractionEnabled = isAutoLockEnabled == false
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        macLabel.text = ellipse.macId
        securitySwitch.isOn = ellipse.security == .unlocked
        ellipse.subscribe(self, theft: self, crash: self)
        getPin()
    }
    
    @IBAction func changeSecurityState(_ sender: UISwitch) {
        if sender.isOn {
            ellipse.unlock()
        } else {
            ellipse.lock()
        }
    }
    
    @IBAction func changeAutoLockState(_ sender: UISwitch) {
        if sender.isOn {
            ellipse.enableAutoLock()
            isAutoLockEnabled = true
        } else {
            ellipse.unlock()
        }
    }
    
    @IBAction func changeMagLockState(_ sender: UISwitch) {
        ellipse.isMagnetAutoLockEnabled = sender.isOn
    }
    
    @IBAction func changeBlinkState(_ sender: UISwitch) {
        ellipse.isLEDBlinking = sender.isOn
    }
    
    @IBAction func updateFirmware(_ sender: Any) {
        let picker = UIAlertController(title: "Source", message: nil, preferredStyle: .actionSheet)
        picker.addAction(UIAlertAction(title: "Internet", style: .default, handler: { _ in
            self.versions()
        }))
        picker.addAction(.init(title: "File", style: .default) { _ in
            let files = UIDocumentPickerViewController(documentTypes: ["public.data"], in: .import)
            files.delegate = self
            self.present(files, animated: true, completion: nil)
        })
        picker.addAction(.init(title: "Cancel", style: .cancel, handler: nil))
        present(picker, animated: true, completion: nil)
    }
    
    fileprivate func versions() {
        func show(versions: [String]) {
            let picker = UIAlertController(title: "Version:", message: nil, preferredStyle: .actionSheet)
            versions.forEach { (v) in
                picker.addAction(UIAlertAction(title: v, style: .default, handler: { _ in
                    self.getLog(version: v)
                }))
            }
            picker.addAction(.init(title: "Cancel", style: .cancel, handler: nil))
            present(picker, animated: true, completion: nil)
        }
        self.progress(text: "Checking")
        network.firmvareVersions { (result) in
            self.hideProgress()
            switch result {
            case .success(let versions):
                show(versions: versions)
            case .failure(let error):
                self.warn(error: error)
            }
        }
    }
    
    fileprivate func getLog(version: String) {
        progress(text: version)
        network.firmvareChangeLog(for: version) { [weak self] result in
            self?.hideProgress()
            switch result {
            case .success(let log):
                self?.show(log: log, version: version)
            case .failure(let error):
                self?.warn(error: error)
            }
        }
    }
    
    fileprivate func show(log: [String], version: String) {
        let str = log.joined(separator: "\n")
        let alert = UIAlertController(title: version, message: str, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Update", style: .default, handler: { (_) in
            self.progessRatio()
            self.network.firmvare(version: version) { [weak self] result in
                switch result {
                case .success(let fw):
                    self?.ellipse.update(firmware: fw)
                case .failure(let error):
                    self?.hideProgress()
                    self?.warn(error: error)
                }
            }
        }))
        alert.addAction(UIAlertAction(title: "Cancel", style: .destructive, handler: nil))
        present(alert, animated: true, completion: nil)
    }
    
    @IBAction func changePin(_ sender: Any) {
        let controller = storyboard?.instantiateViewController(withIdentifier: "pinvc") as! PinViewController
        controller.delegate = self
        controller.code = pinCodeLabel.text
        present(controller, animated: true, completion: nil)
    }
    
    @IBAction func theftSegmentChanged(_ sender: UISegmentedControl) {
        ellipse.accelerometer.theftLimit = .init(sender.selectedSegmentIndex)
    }
    
    @IBAction func crashSegmentChanged(_ sender: UISegmentedControl) {
        ellipse.accelerometer.crashLimit = .init(sender.selectedSegmentIndex)
    }
    
//    @IBAction func theftValueChanged(_ sender: UISlider) {
//        ellipse.accelerometer.theftLimit.sensetivity = sender.value
//        theftThLabel.text = "\(Int(ellipse.accelerometer.theftLimit.threshold))"
//    }
//    
//    @IBAction func crashValueChanged(_ sender: UISlider) {
//        ellipse.accelerometer.crashLimit.sensetivity = sender.value
//        crashThLabel.text = "\(Int(ellipse.accelerometer.crashLimit.threshold))"
//    }
    
    @IBAction func theftSwitched(_ sender: UISwitch) {
        if sender.isOn && crashSwitch.isOn {
            crashSwitch.isOn = false
            crashSlider.isEnabled = false
        }
        theftSlider.isEnabled = sender.isOn
    }
    
    @IBAction func crashSwitched(_ sender: UISwitch) {
        if sender.isOn && theftSwitch.isOn {
            theftSwitch.isOn = false
            theftSlider.isEnabled = false
        }
        crashSlider.isEnabled = sender.isOn
    }
    
    fileprivate func progress(text: String) {
        if progressView == nil {
            progressView = GradientCircularProgress()
            progressView?.show(message: text)
        } else {
            progressView?.updateMessage(message: text)
        }
    }
    
    fileprivate func progessRatio() {
        progressView = GradientCircularProgress()
        progressView?.showAtRatio()
    }
    
    fileprivate func hideProgress() {
        progressView?.dismiss()
        progressView = nil
    }
    
    fileprivate func getPin() {
        network.getPinCode(forLockWith: ellipse.macId) { [weak self] result in
            switch result {
            case .success(let pin):
                let chars = pin.compactMap(Pin.init).map({$0.char})
                self?.pinCodeLabel.text = String(chars)
            case .failure(let error):
                print(error)
            }
        }
    }
    
    fileprivate func popUp(title: String?, body: String?) {
        let alert = UIAlertController(title: title, message: body, preferredStyle: .alert)
        alert.addAction(.init(title: "OK", style: .cancel, handler: nil))
        present(alert, animated: true, completion: nil)
    }
}

extension EllipseViewController: EllipseDelegate {
    func ellipse(_ ellipse: Ellipse, didUpdate value: Ellipse.Value) {
        switch value {
        case .accelerometer(let value):
            xMavLabel.text = String(value.mav.x)
            yMavLabel.text = String(value.mav.y)
            zMavLabel.text = String(value.mav.z)
            xDevLabel.text = String(value.deviation.x)
            yDevLabel.text = String(value.deviation.y)
            zDevLabel.text = String(value.deviation.z)
        case .magnet(let value):
            magXLabel.text = String(value.x)
            magYLabel.text = String(value.y)
            magZLabel.text = String(value.z)
        case .firmwareVersion(let version):
            firmwareLabel.text = "Boot Loader - \(ellipse.bootloaderVersion!), FW - \(version)"
        case .metadata(let metadata):
            batteryLabel.text = String(Int(metadata.batteryLevel*100)) + "%"
            rssiLabel.text = String(Int(metadata.signalLevel*100)) + "%"
            voltageLabel.text = "\(metadata.voltage/1000)V"
        case .serialNumber(let serial):
            serialLabel.text = serial
        case .shackleInserted(let inserted):
            print(inserted, ellipse.magnet.z)
        case .magnetAutoLockEnabled(let enabled):
            magLockSwitch.isOn = enabled
        default:
            break
        }
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate connection: Ellipse.Connection) {
        switch connection {
        case .unpaired, .reconnecting:
            navigationController?.popViewController(animated: true)
        case .updating(let progress):
            progressView?.updateRatio(CGFloat(progress))
            if progress >= 1 {
                progressView?.dismiss {
                    self.progressView = nil
                    self.progress(text: "Reconnecting...")
                }
            }
        case .paired:
            hideProgress()
        default:
            break
        }
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate security: Ellipse.Security) {
        securitySwitch.isOn = ellipse.security == .unlocked
        isAutoLockEnabled = security == .auto
    }
}

extension EllipseViewController: TheftPresentable, CrashPresentable {
    func handleTheft(value: Accelerometer.Value, for ellipse: Ellipse) {
        guard theftSwitch.isOn else { return }
        popUp(title: "Theft detected", body: "Deviation: \(value.deviation)\nMav: \(value.mav)")
    }
    
    func handleCrash(value: Accelerometer.Value, for ellipse: Ellipse) {
        guard crashSwitch.isOn else { return }
        popUp(title: "Crash detected", body: "Deviation: \(value.deviation)\nMav: \(value.mav)")
    }
}

extension EllipseViewController: PinViewControllerDelegate {
    func save(pin: [Pin], completion: @escaping (Error?) -> ()) {
        progress(text: "Saving Pin code")
        network.save(pinCode: pin.map({$0.rawValue}), forLock: ellipse.macId) { [weak self] result in
            self?.hideProgress()
            switch result {
            case .success:
                do {
                    try self?.ellipse.set(pinCode: pin.compactMap(Ellipse.Pin.init))
                    self?.pinCodeLabel.text = String(pin.map({$0.char}))
                    completion(nil)
                } catch {
                    completion(error)
                }
            case .failure(let error):
                completion(error)
            }
        }
    }
}

extension EllipseViewController: UIDocumentPickerDelegate {
    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        do {
            try ellipse.updateWith(contentsOf: urls[0])
            progessRatio()
        } catch {
            fatalError(error.localizedDescription)
        }
    }
}
