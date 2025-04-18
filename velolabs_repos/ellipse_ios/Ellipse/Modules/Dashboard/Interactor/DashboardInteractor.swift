//
//  DashboardDashboardInteractor.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import LattisSDK
import Oval

fileprivate let onboardingPresentedKey = "onboardingPresented"
var isFwAlertShown: Bool {
    get {
        return UserDefaults.standard.bool(forKey: "isFwAlertShown")
    }
    set {
        UserDefaults.standard.set(newValue, forKey: "isFwAlertShown")
        UserDefaults.standard.synchronize()
    }
}

var dismissAutoUnlockPopUp: Bool {
    get {
        return UserDefaults.standard.bool(forKey: "dismissAutoUnlockPopUp")
    }
    set {
        UserDefaults.standard.set(newValue, forKey: "dismissAutoUnlockPopUp")
        UserDefaults.standard.synchronize()
    }
}

class DashboardInteractor {
    weak var view: DashboardInteractorOutput! {
        didSet {
            errorHandlert = DashboardErrorHandler(view)
        }
    }
    var router: DashboardRouter!
    var isInitial: Bool = false
    var changelog: String?
    
    fileprivate let ble = EllipseManager.shared
    fileprivate var device: Ellipse.Device? {
        didSet {
            guard let device = device else {
                view?.show(state: .disconnected)
                return
            }
            view?.show(device: device)
            device.peripheral.subscribe(self)
            updateInitialState()
        }
    }
    fileprivate var lockState: LockControl.LockState?
    fileprivate let storage: EllipseStorage & ContactStorage = CoreDataStack.shared
    fileprivate var ellipse: Ellipse?
    fileprivate var handler: StorageHandler?
    fileprivate var errorHandlert: DashboardErrorHandler!
    fileprivate var isLocked: Bool  {
        guard let sec = device?.peripheral.security else { return false }
        return sec == .locked
    }
    fileprivate let network: LocksNetwork = Session.shared
    fileprivate var networkVersion: String?
    fileprivate var isJustConnected: Bool = true
    fileprivate var isOnboarding: Bool = false
    
    init() {
        LockManager.shared.navigate = { [weak self] macId in
            self?.navigate(macId: macId)
        }
    }
    
    fileprivate func updateInitialState() {
        guard let ellipse = device?.ellipse else { return }
        view.setAutoUnlock(enabled: ellipse.isAutoUnlockEnabled)
        view.setAutoLock(enabled: ellipse.isAutoLockEnabled)
        view.setTheftAlert(enabled: ellipse.isTheftEnabled)
        view.setCrashAlert(enabled: ellipse.isCrashEnabled)
    }
}

extension DashboardInteractor: DashboardInteractorInput {
    
    func updateFW() {
        view.startLoading(text: "downloading_firmware".localized())
        network.firmvare(version: nil) { [weak self] result in
            switch result {
            case .success(let firmware):
                self?.view.stopLoading(completion: {self?.view.beginFWUpdate()})
                self?.device?.peripheral.update(firmware: firmware.compactMap({$0.bytesArray}).flatMap({$0}))
            case .failure(let error):
                self?.errorHandlert.handle(error: error)
            }
        }
    }
    
    func setAutoLock(enabled: Bool) {
        if enabled {
            device?.peripheral.enableAutoLock()
        } else {
            device?.peripheral.unlock()
        }
        ellipse?.isAutoLockEnabled = enabled
        saveEllipse()
    }
    
    func setAutoUnlock(enabled: Bool) {
        if enabled {
            if !dismissAutoUnlockPopUp {
                let dismiss: AlertView.Action = .init(title: "general_dismiss_message".localized()) { (_) in
                    dismissAutoUnlockPopUp = true
                }
                let ok: AlertView.Action = .init(title: "ok".localized(), style: .cancel)
                let alert = AlertView.alert(title: "auto_unlock_popup_title".localized(), text: "auto_unlock_popup_text".localized(), actions: [dismiss, ok])
                alert.show()
            }
            if isLocked {
                device?.peripheral.unlock()
                if let ellipse = device?.ellipse, ellipse.isAutoLockEnabled {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1, execute: {
                        self.device?.peripheral.enableAutoLock()
                    })
                }
            }
        }
        ellipse?.isAutoUnlockEnabled = enabled
        saveEllipse()
    }
    
    func setTheftAlert(enabled: Bool) {
        if enabled {
            device?.peripheral.accelerometer.subscribeTheft(handler: LockManager.shared)
            device?.peripheral.accelerometer.unsubscribeCrash(handler: LockManager.shared)
            if let ellipse = ellipse, ellipse.isCrashEnabled {
                self.ellipse?.isCrashEnabled = false
                self.view.setCrashAlert(enabled: false)
            }
        } else {
            device?.peripheral.accelerometer.unsubscribeTheft(handler: LockManager.shared)
        }
        ellipse?.isTheftEnabled = enabled
        saveEllipse()
    }
    
    func setCrashAlert(enabled: Bool) {
        if enabled {
            device?.peripheral.accelerometer.subscribeCrash(handler: LockManager.shared)
            device?.peripheral.accelerometer.unsubscribeTheft(handler: LockManager.shared)
            if let ellipse = ellipse, ellipse.isTheftEnabled {
                self.ellipse?.isTheftEnabled = false
                self.view.setTheftAlert(enabled: false)
            }
        } else {
            device?.peripheral.accelerometer.unsubscribeCrash(handler: LockManager.shared)
        }
        ellipse?.isCrashEnabled = enabled
        saveEllipse()
    }
    
    func unlock() {
        view.show(state: .processing)
        lockState = .locked
        device?.peripheral.unlock()
        if let ellipse = ellipse {
            if ellipse.isAutoUnlockEnabled {
                self.ellipse?.isAutoUnlockEnabled = false
                saveEllipse()
                view.setAutoUnlock(enabled: false)
            }
            //FIXME:
            if ellipse.isAutoLockEnabled {
                DispatchQueue.main.asyncAfter(deadline: .now() + 1, execute: {
                    self.device?.peripheral.enableAutoLock()
                })
            }
        }
    }
    
    func lock() {
        view.show(state: .processing)
        lockState = .unlocked
        device?.peripheral.lock()
        if let ellipse = ellipse, ellipse.isAutoLockEnabled {
            self.ellipse?.isAutoLockEnabled = false
            saveEllipse()
            view.setAutoLock(enabled: false)
        }
    }
    
    func viewDidLoad() {
        
        switch device?.peripheral.connection {
        case .connecting?:
            view.show(state: .connecting)
        case .paired?:
            let isLocked = device!.peripheral.security == .locked
            view.show(state: isLocked ? .locked : .unlocked)
            if let ellipse = device!.ellipse, isLocked, ellipse.isAutoUnlockEnabled {
                device?.peripheral.unlock()
            }
        default:
            view.show(state: .disconnected)
        }
        updateInitialState()
        handler = storage.current { [unowned self] ellipse in
            self.handle(current: ellipse)
        }
//        if isInitial {
            presentInitial()
//        }
    }
    
    func onboard() {
        isOnboarding = true
        router.showOnboarding(delegate: self)
    }
    
    func checkContacts() -> Bool {
        let contacts = storage.getEmergency()
        if contacts.isEmpty {
            let action = AlertView.Action(title: "continue_lable".localized(), handler: { [unowned self] _ in
                self.router.contacts(delegate: self)
            })
            AlertView.alert(title: "alert.important.title".localized(), text: "alert.crash.precondition.contact.error.message".localized(), actions: [.cancel, action]).show()
            return false
        }
        return true
    }
    
    @discardableResult fileprivate func checkDB() -> Bool {
        guard let device = device else { return false }
        guard let _ = storage.getEllipse(device.macId) else {
            if let e = device.ellipse, e.isShared {
                // TODO: unshared popup
            }
            device.peripheral.disconnect()
            return false
        }
        return true
    }
    
    fileprivate func checkFWUpdate() {
        guard isFwAlertShown == false && isOnboarding == false else { return }
        func compare(network: String) {
            guard let local = device?.peripheral.firmwareVersion?.version, network > local else { return }
            isFwAlertShown = true
            self.network.firmvareChangeLog(for: nil) { [weak self] result in
                switch result {
                case .success(let log):
                    self?.view.showUpdateDialog(changelog: log.joined(separator: "\n"))
                case .failure(let error):
                    report(error: error)
                }
            }
        }
        if let networkVersion = networkVersion {
            compare(network: networkVersion)
        } else {
            network.firmvareVersions { [weak self] result in
                switch result {
                case .success(let versions):
                    if let max = versions.map({$0.version}).max() {
                        self?.networkVersion = max
                        compare(network: max)
                    }
                case .failure(let error):
                    self?.errorHandlert.handle(error: error)
                }
            }
        }
    }
    
    func openSettigns() {
        if let ellipse = self.device?.ellipse {
            router.openSettings(.init(ellipse, peripheral: device?.peripheral))
        }
    }
}

extension DashboardInteractor: LockOnboardingDelegate {
    func didFinishLockOnboarding() {
        isOnboarding = false
        checkFWUpdate()
    }
}

extension DashboardInteractor: EllipseDelegate {
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {
        guard let device = device, device.macId == ellipse.macId, ellipse.isPaired else { return }
        switch security {
        case .locked:
            view.show(state: .locked)
            if isJustConnected, let ell = self.ellipse, ell.isAutoUnlockEnabled {
                isJustConnected = false
                ellipse.unlock()
                if ell.isAutoLockEnabled {
                    // TODO: implement auto lock
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1, execute: {
                        ellipse.enableAutoLock()
                    })
                }
            }
        case .unlocked:
            isJustConnected = false
            view.show(state: .unlocked)
        case .invalid, .middle:
            view.show(warning: "lock_malfunction".localized(), title: "warning".localized())
            guard let prev = lockState else { break }
            view.show(state: prev)
        default:
            break
        }
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
        guard let device = device, device.macId == ellipse.macId else { return }
        switch connection {
        case .connecting:
            view.show(state: checkDB() ? .connecting : .disconnected)
        case .paired:
            if let sense = self.ellipse?.sensorSensitivity {
                ellipse.accelerometer.theftLimit = .init(sense)
            }
            
            view.stopLoading(completion: nil)
            isJustConnected = true
            self.ellipse?.connectedAt = Date()
            saveEllipse()
            LocationTracker.shared.track(lock: device)
            checkDB()
        case .unpaired, .reconnecting:
            view?.show(state: .disconnected)
        case .failed(let error):
            self.ellipse?.isCurrent = false
            errorHandlert.handle(error: error)
            view.show(state: .disconnected)
            if error.isEllipseTimeout && Ellipse.count > 1 && !isMultyLockAlertShown {
                showLocksAlert()
            }
        case .updating(let progress):
            view.updateFW(progress: progress)
            if progress >= 1 {
                log(.custom(.fwUpdate))
                view.finishFWUpdate()
            }
        default:
            break
        }
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate value: Peripheral.Value) {
        switch value {
        case .metadata(let data):
            self.view.show(batteryLevel: data.batteryLevel, and: data.signalLevel)
        case .firmwareVersion:
            checkFWUpdate()
        default:
            break
        }
    }
}

extension DashboardInteractor: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Peripheral], delete: [Peripheral]) {
        if let ellipse = ellipse, delete.contains(where: {$0.macId == ellipse.macId}) {
//            device = nil
            return
        }
        guard let ellipse = ellipse, let per = lockManager.locks.filter({$0.macId == ellipse.macId}).first else { return }
        device = Ellipse.Device(per)
        device?.ellipse = ellipse
        updateInitialState()
        lockManager.stopScan()
        if per.isPaired {
            isJustConnected = true
        } else {
            device?.connect(self)
        }
    }
}

extension DashboardInteractor: EmergencyDelegate {
    func didCloseEmergency() {
        if storage.getEmergency().isEmpty == false {
            setCrashAlert(enabled: true)
            view.setCrashAlert(enabled: true)
        }
    }
}

private extension DashboardInteractor {
    func handle(current: Ellipse?) {
        if let ellipse = current {
            if let device = device, device.macId == ellipse.macId {
                self.device?.peripheral.accelerometer.theftLimit = .init(ellipse.sensorSensitivity)
                self.device?.ellipse = ellipse
                self.view.show(device: self.device!)
                self.updateInitialState()
            } else {
                ble.scan(with: self)
            }
            self.ellipse = ellipse
        } else if let device = device {
//            device.peripheral.factoryReset(disconnect: true)
            self.device = nil
        } else if Ellipse.count > 1 && !isMultyLockAlertShown {
            showLocksAlert()
        }
    }
    
    func showLocksAlert() {
        let alert = AlertView.alert(title: "note_title".localized(), text: "no_current_multy_lock".localized(), actions: [
            .yes(handler: self.router.openLocks),
            .no
            ])
        alert.show()
        isMultyLockAlertShown = true
    }
    
    func saveEllipse() {
        guard let ellipse = ellipse else { return }
        storage.save(ellipse)
    }
    
    func navigate(macId: String) {
        guard let ellipse = storage.getEllipse(macId) else { return }
        router.navigate(to: ellipse)
    }
    
    func presentInitial() {
        if Permission.isGranted(.location) {
            presentOnboardingIfNeeded()
        } else {
            router.grant(permission: .location)
        }
    }
    
    func presentOnboardingIfNeeded() {
        guard storage.isEmpty, onboardingPresented == false else { return }
        onboardingPresented = true
        isOnboarding = true
        router.showOnboarding(delegate: self)
    }
    
    var onboardingPresented: Bool {
        get {
            return UserDefaults.standard.bool(forKey: onboardingPresentedKey)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: onboardingPresentedKey)
            UserDefaults.standard.synchronize()
        }
    }
}

extension DashboardInteractor: PermissionsDelegate {
    func permissionsFinished(for permission: Permission, dismiss: @escaping (@escaping () -> ()) -> ()) {
        dismiss() { [unowned self] in
            self.presentOnboardingIfNeeded()
        }
    }
}
