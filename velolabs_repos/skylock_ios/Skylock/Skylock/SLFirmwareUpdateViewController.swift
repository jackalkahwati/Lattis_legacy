//
//  SLFirmwareUpdateViewController.swift
//  Ellipse
//
//  Created by Andre Green on 8/8/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import RestService

enum SLFirmwareUpdateStage {
    case fetchingInfo
    case available
    case notAvailable
    case inProgress
    case restartingLock
    case finished
}

class SLFirmwareUpdateViewController: SLBaseViewController {
    private enum FirmWareVersion {
        case release
        case revision
    }
    
    let xPadding:CGFloat = 25.0
    
    let buttonHeight:CGFloat = 55.0
    
    var stage:SLFirmwareUpdateStage = .fetchingInfo
    
    let currentFirmwareVersion:String
    
    var isForceUpdate = false
    
    let updateText:[SLFirmwareUpdateStage:String] = [
        .fetchingInfo: NSLocalizedString("Gathering update information", comment: ""),
        .available: NSLocalizedString("A firmware update is available for your Ellipse.", comment: ""),
        .notAvailable: NSLocalizedString("Your Ellipse's firmware is up to date...", comment: ""),
        .inProgress: NSLocalizedString("Firmware update in progress...", comment: ""),
        .restartingLock: NSLocalizedString("Restarting your Ellipse. One sec...", comment: ""),
        .finished: NSLocalizedString("All Done! Restarting your Ellipse...", comment: "")
    ]
    
    private var lockDisconnectedTimer:Timer?
    
    // This should be passed in in the initialzer once it is implemnted on the server
    var updateLog:[String]?
    
    override var preferredStatusBarStyle:UIStatusBarStyle {
        return .lightContent
    }
    
    lazy var updateLabel:UILabel = {
        let frame = CGRect(
            x: self.xPadding,
            y: 100.0,
            width: self.view.bounds.size.width - 2*self.xPadding,
            height: 17.0
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 14.0)
        label.text = self.updateText[self.stage]
        label.textColor = UIColor.white
        
        return label
    }()
    
    lazy var updateLogLabel:UILabel = {
        let frame = CGRect(x: self.xPadding, y: self.updateLabel.frame.maxY + 20, width: 0, height:0)
        let label:UILabel = UILabel(frame: frame)
        label.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 14.0)
        label.text = self.updateText[.available]
        label.textColor = .white
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var progressLabel:UILabel = {
        let frame = CGRect(
            x: self.xPadding,
            y: self.updateLogLabel.frame.maxY + 20.0,
            width: self.view.bounds.size.width - 2*self.xPadding,
            height: 17.0
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 11.0)
        label.text = NSLocalizedString("Progress", comment: "") + "..."
        label.textColor = UIColor.white
        label.isHidden = self.stage != .inProgress
        
        return label
    }()
    
    lazy var progressBar:SLFirmwareUpdateProgressBarView = {
        let frame = CGRect(
            x: self.xPadding,
            y: self.progressLabel.frame.maxY + 5.0,
            width: self.view.bounds.size.width - 2*self.xPadding,
            height: 10.0
        )
        
        let bar:SLFirmwareUpdateProgressBarView = SLFirmwareUpdateProgressBarView(frame: frame)
        bar.isHidden = true
        bar.backgroundColor = .white
        return bar
    }()
    
    lazy var updateLaterButton:UIButton = {
        let frame = CGRect(
            x: 0.0,
            y: self.view.bounds.size.height - self.buttonHeight,
            width: 0.5*self.view.bounds.size.width,
            height: self.buttonHeight
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.setTitle(NSLocalizedString("LATER", comment: ""), for: .normal)
        button.setTitleColor(UIColor(red: 188, green: 187, blue: 187), for: .normal)
        button.backgroundColor = UIColor(red: 231, green: 231, blue: 233)
        button.addTarget(self, action: #selector(updateLaterButtonPressed), for: .touchDown)
        button.isHidden = self.isForceUpdate
        return button
    }()
    
    lazy var updateNowButton:UIButton = {
        let button:UIButton = UIButton(type: .system)
        button.frame = {
            var frame = self.view.frame
            frame.size.width *= 0.5
            frame.size.height = self.buttonHeight
            frame.origin.x = self.isForceUpdate ? self.view.frame.midX - 0.5*frame.width : frame.width
            frame.origin.y = self.view.frame.maxY - frame.height - (self.isForceUpdate ? 10 : 0)
            return frame
        }()
        
        button.setTitle(NSLocalizedString("UPDATE NOW", comment: ""), for: .normal)
        button.setTitleColor(UIColor.white, for: .normal)
        button.backgroundColor = UIColor(red: 87, green: 216, blue: 255)
        button.addTarget(self, action: #selector(updateNowButtonPressed), for: .touchDown)
        
        return button
    }()
    
    init(firmwareVersionString: String) {
        self.currentFirmwareVersion = firmwareVersionString
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor(red: 60, green: 83, blue: 119)
        
        self.view.addSubview(self.updateLabel)
        self.view.addSubview(self.progressLabel)
        self.view.addSubview(self.progressBar)
        self.view.addSubview(self.updateLaterButton)
        self.view.addSubview(self.updateNowButton)
        self.view.addSubview(self.updateLogLabel)
        
        if isForceUpdate {
            navigationController?.isNavigationBarHidden = true
            updateChangelog()
        } else {
            Oval.locks.firmvareVersions(success: { [weak self] (versions) in
                let trimming = CharacterSet(charactersIn: "0")
                let ver = versions.map({ $0.trimmingCharacters(in: trimming) }).sorted(by: <)
                self?.checkLatest(versions: ver)
                }, fail: { error in
                    
            })
        }
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(updateFirmware(notification:)),
            name: NSNotification.Name(rawValue: kSLNotificationLockManagerFirmwareUpdateState),
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(firmwareUpdateComplete(notification:)),
            name: NSNotification.Name(rawValue: kSLNotificationLockManagerEndedFirmwareUpdate),
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(disconnectedLock(notification:)),
            name: NSNotification.Name(rawValue: kSLNotificationLockManagerDisconnectedLock),
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(lockPaired(notification:)),
            name: NSNotification.Name(rawValue: kSLNotificationLockPaired),
            object: nil
        )
        NotificationCenter.default.addObserver(self, selector: #selector(lockError(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerErrorConnectingLock), object: nil)
    }
    
    private func checkLatest(versions: [String]) {
        guard let last = versions.last else { return setFirmwareStage(stage: .notAvailable) }
        let clearLast = last.replacingOccurrences(of: ".", with: "")
        let clearCurrent = currentFirmwareVersion.replacingOccurrences(of: ".", with: "")
        if clearLast > clearCurrent {
            updateChangelog()
        } else {
            setFirmwareStage(stage: .notAvailable)
        }
    }
    
    private func updateChangelog() {
        Oval.locks.firmvareChangeLog(success: { [weak self] changelog in
            var log = "What’s new:\n".localized()
            changelog.forEach({ log += "\($0)\n" })
            log = log.trimmingCharacters(in: CharacterSet(charactersIn: "\n"))
            self?.setUpdateLogLabel(text: log)
            self?.setFirmwareStage(stage: .available)
            }, fail: { [weak self] error in
                self?.setFirmwareStage(stage: .notAvailable)
        })
    }
    
    private func parseFirmware(versionString: String) -> [FirmWareVersion: Int]? {
        let parts = versionString.components(separatedBy: ".")
        if parts.count <= 1 {
            return nil
        }
        
        if let release:Int = Int(parts[0]), let revision = Int(parts[1]) {
            return [.release: release, .revision: revision]
        }
        
        return nil
    }
    
    func parseFirmware(updateLogString: String) -> [String] {
        return updateLogString.components(separatedBy: "\n")
    }
    
    func setFirmwareStage(stage: SLFirmwareUpdateStage) {
        self.stage = stage
        self.updateViewsForStage()
    }
    
    func updateViewsForStage() {
        DispatchQueue.main.async {
            self.updateLabel.text = self.updateText[self.stage]
            
            switch self.stage {
            case .available:
                self.updateNowButton.isHidden = false
                self.updateNowButton.isEnabled = true
                self.updateLaterButton.isHidden = false
                self.updateLaterButton.isEnabled = true
                self.progressBar.isHidden = true
                self.progressLabel.isHidden = true
            case .notAvailable:
                self.updateNowButton.isHidden = false
                self.updateNowButton.isEnabled = false
                self.updateLaterButton.isHidden = false
                self.updateLaterButton.isEnabled = true
                self.progressBar.isHidden = true
                self.progressLabel.isHidden = true
            case .fetchingInfo:
                self.updateNowButton.isHidden = true
                self.updateNowButton.isEnabled = false
                self.updateLaterButton.isHidden = true
                self.updateLaterButton.isEnabled = false
                self.progressBar.isHidden = true
                self.progressLabel.isHidden = true
            case .inProgress, .restartingLock:
                self.updateNowButton.isHidden = true
                self.updateNowButton.isEnabled = false
                self.updateLaterButton.isHidden = true
                self.updateLaterButton.isEnabled = false
                self.progressBar.isHidden = false
                self.progressLabel.isHidden = false
            case .finished:
                self.updateNowButton.isHidden = false
                self.updateNowButton.isEnabled = false
                self.updateLaterButton.isHidden = false
                self.updateLaterButton.isEnabled = true
                self.progressBar.isHidden = true
                self.progressLabel.isHidden = true
            }
            if self.isForceUpdate {
                self.updateLaterButton.isHidden = true
                self.updateNowButton.isHidden = false
                self.updateNowButton.alpha = self.stage == .available ? 1 : 0.5
                self.updateNowButton.isEnabled = self.stage == .available
            }
        }
    }
    
    func setUpdateLogLabel(text: String) {
        DispatchQueue.main.async {
            let labelWidth = self.view.bounds.size.width - 2*self.xPadding
            let utility = SLUtilities()
            let font = UIFont.systemFont(ofSize: 22)
            let labelSize:CGSize = utility.sizeForLabel(
                font: font,
                text: text,
                maxWidth: labelWidth,
                maxHeight: CGFloat.greatestFiniteMagnitude,
                numberOfLines: 0
            )
            
            let frame = CGRect(
                x: self.xPadding,
                y: self.updateLabel.frame.maxY + 20.0,
                width: labelWidth,
                height: labelSize.height
            )
            
            self.updateLogLabel.frame = frame
            self.updateLogLabel.text = text
            
            self.progressLabel.frame = CGRect(
                x: self.xPadding,
                y: self.updateLogLabel.frame.maxY + 20.0,
                width: self.view.bounds.size.width - 2*self.xPadding,
                height: 17.0
            )
            
            self.progressBar.frame = CGRect(
                x: self.xPadding,
                y: self.progressLabel.frame.maxY + 5.0,
                width: self.view.bounds.size.width - 2*self.xPadding,
                height: 10.0
            )
        }
    }
    
    func updateLaterButtonPressed() {
        self.dismiss(animated: true, completion: nil)
    }
    
    func updateNowButtonPressed() {
        self.setFirmwareStage(stage: .inProgress)
        SLLockManager.sharedManager.updateFirmwareForCurrentLock { [weak self] (error) in
            let texts:[SLWarningViewControllerTextProperty:String?] = [
                .Header: NSLocalizedString("Something's Wrong!", comment: ""),
                .Info: NSLocalizedString("There has been an issue updating the firmware on your Ellipse.", comment: ""),
                .CancelButton: NSLocalizedString("OK", comment: ""),
                .ActionButton: nil
            ]
            
            self?.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: {
                if let navController = self?.navigationController {
                    navController.popViewController(animated: true)
                } else {
                    self?.dismiss(animated: true, completion: nil)
                }
            })
        }
    }
    
    func updateFirmware(notification: Notification) {
        guard let progress = notification.object as? NSNumber else {
            return
        }
        
        self.progressBar.updateBarWithRatio(ratio: progress.doubleValue)
    }
    
    func firmwareUpdateComplete(notification: Notification) {
        self.updateLabel.text = self.updateText[.finished]
        let texts:[SLWarningViewControllerTextProperty:String?] = [
            .Header: NSLocalizedString("SUCESS", comment: ""),
            .Info: NSLocalizedString("Firmware update is complete.", comment: ""),
            .CancelButton: NSLocalizedString("OK", comment: ""),
            .ActionButton: nil
        ]
        
        self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: {
            if let navController = self.navigationController {
                navController.popViewController(animated: true)
            } else {
                self.dismiss(animated: true, completion: nil)
            }
        })
    }
    
    func disconnectedLock(notification: Notification) {
        self.setFirmwareStage(stage: .restartingLock)
        self.lockDisconnectedTimer = Timer.scheduledTimer(
            timeInterval: 45.0,
            target: self,
            selector: #selector(lockDisconnectedTimeOut),
            userInfo: nil,
            repeats: true
        )
    }
    
    func lockPaired(notification: Notification) {
        self.lockDisconnectedTimer?.invalidate()
        self.lockDisconnectedTimer = nil
        
        let texts:[SLWarningViewControllerTextProperty:String?] = [
            .Header: "Firmware Updated!".localized(),
            .Info: "The firmware on your ellipse has been updated.".localized(),
            .CancelButton: NSLocalizedString("OK", comment: ""),
            .ActionButton: nil
        ]
        
        self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: {
            if let navController = self.navigationController, self.isForceUpdate == false {
                navController.popViewController(animated: true)
            } else {
                self.dismiss(animated: true, completion: nil)
            }
        })
    }
    
    func lockError(notification: Notification) {
        guard let info = notification.object as? [String: Any],
            let error = info["error"] as? SLLockManagerConnectionError,
            error == .invalidOffcet || error == .invalidWriteLength || error == .invalidParameter else { return }
        let texts:[SLWarningViewControllerTextProperty:String?] = [
            .Header: NSLocalizedString("Something's Wrong!", comment: ""),
            .Info: NSLocalizedString("There has been an issue updating the firmware on your Ellipse. Update process will be again.", comment: ""),
            .CancelButton: NSLocalizedString("OK", comment: "")
        ]
        
        self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: {
            self.progressBar.updateBarWithRatio(ratio: 0)
            self.updateNowButtonPressed()
        })
    }
    
    func lockDisconnectedTimeOut() {
        self.lockDisconnectedTimer?.invalidate()
        self.lockDisconnectedTimer = nil
        
        let texts:[SLWarningViewControllerTextProperty:String?] = [
            .Header: NSLocalizedString("Something's Wrong!", comment: ""),
            .Info: NSLocalizedString("There has been an issue updating the firmware on your Ellipse.", comment: ""),
            .CancelButton: NSLocalizedString("OK", comment: ""),
            .ActionButton: nil
        ]
        
        self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: {
            if let navController = self.navigationController {
                navController.popViewController(animated: true)
            } else {
                self.dismiss(animated: true, completion: nil)
            }
        })
    }
}
