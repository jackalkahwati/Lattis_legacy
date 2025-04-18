//
//  DashboardDashboardViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LGSideMenuController
import Cartography

class DashboardViewController: ViewController {
    
    var interactor: DashboardInteractorInput!
    
    fileprivate let onboardContainer = UIView()
    fileprivate let onboardLabel = UILabel()
    fileprivate let onboardButton = UIButton(type: .custom)
    fileprivate let infoContainer = UIView()
    fileprivate let separatorLine = UIView()
    fileprivate let nameLabel = UILabel()
    fileprivate let buttonContainer = UIView()
    fileprivate let batteryIndicator = BatteryIndicator()
    fileprivate let rssiImageView = UIImageView()
    fileprivate let crashButton = ImageTextButton(image: UIImage(named: "dashboard_crash_off")!, text: "crash_off_label".localized())
    fileprivate let autoLockButton = ImageTextButton(image: UIImage(named: "dashboard_auto_lock_off")!, text: "auto_lock_off_label".localized())
    fileprivate let autoUnlockButton = ImageTextButton(image: UIImage(named: "dashboard_auto_unlock_off")!, text: "auto_unlock_off_label".localized())
    fileprivate let theftButton = ImageTextButton(image: UIImage(named: "dashboard_theft_off")!, text: "theft_off_label".localized())
    fileprivate let lockControl = LockControl()
    fileprivate var alert: AlertView?
    fileprivate var progress: ProgressView?

    override func viewDidLoad() {
        super.viewDidLoad()
        configureUI()
        
        interactor.viewDidLoad()
    }
    
    fileprivate func configureUI() {
        view.backgroundColor = .white
        navigationItem.largeTitleDisplayMode = .never
        addMenuButton()
        navigationItem.rightBarButtonItem = UIBarButtonItem(image: UIImage(named: "dashboard_gear"), style: .plain, target: self, action: #selector(openSettings))
        navigationItem.rightBarButtonItem?.isEnabled = false
        
        view.addSubview(infoContainer)
        infoContainer.isHidden = true
        infoContainer.addSubview(crashButton)
        infoContainer.addSubview(autoLockButton)
        infoContainer.addSubview(autoUnlockButton)
        infoContainer.addSubview(theftButton)
        infoContainer.addSubview(separatorLine)
        infoContainer.addSubview(nameLabel)
        infoContainer.addSubview(batteryIndicator)
        infoContainer.addSubview(rssiImageView)
        
        separatorLine.backgroundColor = .black
        nameLabel.textColor = .black
        nameLabel.font = .elTitleLight
        
        crashButton.setSelected(image: UIImage(named: "dashboard_crash_on")!, text: "crash_on_label".localized())
        autoUnlockButton.setSelected(image: UIImage(named: "dashboard_auto_unlock_on")!, text: "auto_unlock_on_label".localized())
        autoLockButton.setSelected(image: UIImage(named: "dashboard_auto_lock_on")!, text: "auto_lock_on_label".localized())
        theftButton.setSelected(image: UIImage(named: "dashboard_theft_on")!, text: "theft_on_label".localized())
        
        rssiImageView.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        
        let margin: CGFloat = 30
        constrain(infoContainer, crashButton, autoLockButton, autoUnlockButton, theftButton, view, separatorLine, nameLabel, batteryIndicator, rssiImageView) { container, crash, lock, unlock, theft, view, line, label, battery, rssi in
            let distance: CGFloat = 20
            container.height == 110
            container.bottom == view.safeAreaLayoutGuide.bottom - margin
            container.left == view.left + margin
            container.right == view.right - margin
            
            line.left == container.left
            line.right == container.right
            line.top == container.top + 24
            line.height == 0.5
            
            crash.left == container.left
            crash.top == line.top + 20
            crash.width == lock.width
            crash.bottom == container.bottom
            
            lock.left == crash.right + distance
            lock.top == line.top + 20
            lock.width == crash.width
            lock.bottom == container.bottom
            
            unlock.left == lock.right + distance
            unlock.top == line.top + 20
            unlock.width == crash.width
            unlock.bottom == container.bottom
            
            theft.left == unlock.right + distance
            theft.top == line.top + 20
            theft.width == crash.width
            theft.bottom == container.bottom
            theft.right == container.right
            
            battery.right == container.right
            battery.bottom == line.top - 10
            
            rssi.right == battery.left - 10
            rssi.bottom == battery.bottom
            
            label.left == container.left
            label.bottom == line.top - 5
            label.right == rssi.left
        }
        
        crashButton.addTarget(self, action: #selector(crashDetection(_:)), for: .touchUpInside)
        autoLockButton.addTarget(self, action: #selector(autoLock(_:)), for: .touchUpInside)
        autoUnlockButton.addTarget(self, action: #selector(autoUnlock(_:)), for: .touchUpInside)
        theftButton.addTarget(self, action: #selector(theftDetection(_:)), for: .touchUpInside)
        
        view.addSubview(buttonContainer)
        buttonContainer.addSubview(lockControl)
        
        constrain(buttonContainer, lockControl, infoContainer, view) { container, button, info, view in
            container.top == view.safeAreaLayoutGuide.top
            container.left == view.left + margin
            container.right == view.right - margin
            container.bottom == info.top
            
            button.centerY == container.centerY
            button.left == container.left
            button.right == container.right
            button.height == button.width
        }
        
        lockControl.addTarget(self, action: #selector(tapLock(_:)), for: .touchUpInside)
        lockControl.addTarget(self, action: #selector(handleLockControl(_:)), for: .valueChanged)
        
        view.addSubview(onboardContainer)
        onboardContainer.addSubview(onboardLabel)
        onboardContainer.addSubview(onboardButton)
        
        onboardLabel.textAlignment = .center
        onboardLabel.numberOfLines = 0
        onboardLabel.font = .elTitleLight
        onboardLabel.textColor = .black
        onboardLabel.text = "you_are_not_connected_to_any_locks".localized()
        
        smallPositiveStyle(onboardButton)
        onboardButton.setTitle("find_an_ellipse_to_connect_to".localized(), for: .normal)
        
        constrain(onboardContainer, infoContainer, view, onboardLabel, onboardButton) { container, info, view, label, button in
            container.edges == info.edges
            
            label.top == container.top
            label.left == container.left
            label.right == container.right
            
            button.top == label.bottom + 20
            button.centerX == container.centerX
        }
        
        onboardButton.addTarget(self, action: #selector(onboardLock(_:)), for: .touchUpInside)
    }
    
    @objc fileprivate func openSettings() {
        interactor.openSettigns()
    }
    
    @objc fileprivate func tapLock(_ sender: LockControl) {
        switch sender.lockState {
        case .locked:
            interactor.unlock()
        case .unlocked:
            interactor.lock()
        default:
            break
        }
    }
    
    @objc fileprivate func handleLockControl(_ sender: LockControl) {
        switch sender.lockState {
        case .connecting:
            onboardContainer.isHidden = true
            infoContainer.isHidden = true
        case .disconnected:
            onboardContainer.isHidden = false
            infoContainer.isHidden = true
        case .locked, .unlocked, .processing:
            onboardContainer.isHidden = true
            infoContainer.isHidden = false
        }
        
        navigationItem.rightBarButtonItem?.isEnabled = infoContainer.isHidden == false
    }
    
    @objc fileprivate func onboardLock(_ sender: Any) {
        interactor.onboard()
    }
    
    @objc fileprivate func crashDetection(_ sender: ImageTextButton) {
        guard interactor.checkContacts() else { return }
        sender.isSelected = !sender.isSelected
        interactor.setCrashAlert(enabled: sender.isSelected)
    }
    
    @objc fileprivate func autoLock(_ sender: ImageTextButton) {
        sender.isSelected = !sender.isSelected
        interactor.setAutoLock(enabled: sender.isSelected)
    }
    
    @objc fileprivate func autoUnlock(_ sender: ImageTextButton) {
        sender.isSelected = !sender.isSelected
        interactor.setAutoUnlock(enabled: sender.isSelected)
    }
    
    @objc fileprivate func theftDetection(_ sender: ImageTextButton) {
        sender.isSelected = !sender.isSelected
        interactor.setTheftAlert(enabled: sender.isSelected)
    }
}

extension DashboardViewController: DashboardInteractorOutput {
    
    func show(state: LockControl.LockState) {
        lockControl.lockState = state
    }
    
    func show(device: Ellipse.Device) {
        nameLabel.text = device.name
    }
    
    func show(batteryLevel: Double, and rssiStreight: Double) {
        batteryIndicator.isHidden = false
        rssiImageView.isHidden = false
        batteryIndicator.capacity = Float(batteryLevel)
        rssiImageView.image = rssiStreight.rssiImage
    }
    
    func setAutoLock(enabled: Bool) {
        autoLockButton.isSelected = enabled
    }
    
    func setAutoUnlock(enabled: Bool) {
        autoUnlockButton.isSelected = enabled
    }
    
    func setTheftAlert(enabled: Bool) {
        theftButton.isSelected = enabled
    }
    
    func setCrashAlert(enabled: Bool) {
        crashButton.isSelected = enabled
    }
    
    func showUpdateDialog(changelog: String?) {
        let update = AlertView.Action(title: "install_update".localized().lowercased().capitalized, style: .default, handler: {_ in self.interactor.updateFW() })
        let later = AlertView.Action(title: "update_later".localized(), style: .cancel)
        let alert = AlertView.alert(title: "firmware_update_available_alert".localized(), text: changelog, actions: [update, later])
        alert.show()
    }
    
    func beginFWUpdate() {
        let (alert, progress) = AlertView.update()
        self.alert = alert
        self.progress = progress
        stopLoading {
            alert.show()
        }
    }
    
    func updateFW(progress: Float) {
        self.progress?.progress = progress
    }
    
    func finishFWUpdate() {
        show(state: .disconnected)
        alert?.hide {
            self.startLoading(text: "Reconnecting...")
        }
    }
}

private extension Double {
    var rssiImage: UIImage {
        if self >= 1 {
            return #imageLiteral(resourceName: "rssi4")
        } else if self >= 0.75 {
            return #imageLiteral(resourceName: "rssi3")
        } else if self >= 0.5 {
            return #imageLiteral(resourceName: "rssi2")
        } else if self >= 0.25 {
            return #imageLiteral(resourceName: "rssi1")
        } else {
            return #imageLiteral(resourceName: "rssi0")
        }
    }
}
