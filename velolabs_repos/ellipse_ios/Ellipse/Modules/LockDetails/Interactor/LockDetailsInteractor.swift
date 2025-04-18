//
//  LockDetailsLockDetailsInteractor.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 27/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import LattisSDK
import Oval

class LockDetailsInteractor {
    weak var view: LockDetailsInteractorOutput! {
        didSet {
            self.errorHandler = ErrorHandler(view)
        }
    }
    var router: LockDetailsRouter!
    var changelog: String?
    
    var lock: Ellipse.Lock!
    fileprivate var sections: [LockDetails.Section] = []
    fileprivate let ble = EllipseManager.shared
    fileprivate var network: LocksNetwork = Session.shared
    fileprivate let storage: EllipseStorage & UserStorage = CoreDataStack.shared
    fileprivate var storageHandler: StorageHandler?
    fileprivate var errorHandler: ErrorHandler!
    fileprivate var fail: ((Error) -> ())!
    fileprivate var isUpdating = false
    
    init() {
        fail = {[weak self] error in
            self?.errorHandler?.handle(error: error)
        }
    }
}

extension LockDetailsInteractor: LockDetailsInteractorInput {
    func start() {
        lock.peripheral?.subscribe(self)
        sections.append(LockDetails.Section(title: "lock_details".localized(), items: [
            .name(lock.name, lock.ellipse.isShared == false),
            .owner(lock.ellipse.owner?.fullName),
            .serial(lock.peripheral?.serialNumber ?? ""),
            .firmware(lock.peripheral?.firmwareVersion ?? "", false)
            ]))
        var items: [LockDetails.Info] = [.sensetivity(lock.ellipse.sensorSensitivity)]
        if lock.ellipse.isShared == false {
            items.append(.pin(lock.ellipse.pin ?? []))
        }
        sections.append(LockDetails.Section(title: "locksetting_devicesetting_title".localized(), items: items))
        view.refresh()
        if let ver = lock.peripheral?.firmwareVersion {
            checkUpdates(current: ver)
        }
        storageHandler = storage.ellipse(lockId: lock.ellipse.lockId) { [unowned self] (ellipse) in
            let old = self.lock.ellipse
            self.lock.ellipse = ellipse
            self.sections[0].items[0] = .name(ellipse.name, ellipse.isShared == false)
            var paths: [IndexPath] = [IndexPath(row: 0, section: 0)]
            if let pin = ellipse.pin {
                self.sections[1].items[1] = .pin(pin)
                paths.append(IndexPath(row: 1, section: 1))
            }
            if old != ellipse {
                self.view.reloadRows(at: paths)
            }
        }
    }
    
    fileprivate func checkUpdates(current: String) {
        let show: (Bool) -> () = { [weak self] update in
            self?.sections[0].items[3] = .firmware(current, update)
            self?.view?.reloadRows(at: [IndexPath(row: 3, section: 0)])
        }
        if case let .firmware(display, _) = sections[0].items[3], display != current {
            show(false)
        }
        network.firmvareVersions { [weak self] result in
            switch result {
            case .success(let versions):
                guard let max = versions.map({$0.version}).max(), max > current.version else { return }
                self?.network.firmvareChangeLog(for: nil, completion: { [weak self] res in
                    switch res {
                    case .success(let log):
                        self?.changelog = log.joined(separator: "\n")
                        show(true)
                    case .failure(let error):
                        report(error: error)
                    }
                })
            case .failure(let e):
                self?.fail(e)
            }
        }
    }
    
    var numberOfSections: Int {
        return sections.count
    }
    
    func numberOfRows(in section: Int) -> Int {
        return sections[section].items.count
    }
    
    func item(for indexPath: IndexPath) -> LockDetails.Info {
        return sections[indexPath.section].items[indexPath.row]
    }
    
    func title(for section: Int) -> String {
        return sections[section].title
    }
    
    func deleteLock() {
        let action: () -> () = { [unowned self] in
            self.view.startLoading(text: "deleting".localized())
            self.network.delete(lock: self.lock.ellipse) { [weak self] result in
                switch result {
                case .success:
                    self?.view.stopLoading(completion: nil)
                    self?.storage.delete(ellipse: self!.lock.ellipse)
                    self?.router.pop()
                    self?.lock.delete()
                    if self?.lock.ellipse.shareId == nil {
                        log(.custom(.deleteLock), attributes: [.screen("Details")])
                    } else {
                        log(.share(.unshared))
                    }
                case .failure(let error):
                    self?.fail(error)
                }
            }
        }
        AlertView.deleteLock(unshare: lock.ellipse.isShared, action: action).show()
    }
    
    func select(itemAt indexPath: IndexPath) {
        let info = item(for: indexPath)
        switch info {
        case .name(_, let canEdit) where canEdit:
            let edit = router.editName(info: info)
            edit.validate = { value in
                return value.count > 1
            }
            edit.save = { [unowned self] value in
                self.view.startLoading(text: "saving_lock_name".localized())
                var ell = self.lock.ellipse
                ell.name = value
                self.network.update(lock: ell) { [weak self] result in
                    switch result {
                    case .success(let ellipse):
                        self?.router.dismiss()
                        self?.view.stopLoading(completion: nil)
                        self?.storage.save(ellipse)
                        log(.custom(.rename))
                    case .failure(let error):
                        self?.fail(error)
                    }
                }
            }
        default:
            router.open(info: info)
        }
    }
    
    func update() {
        view.startLoading(text: "downloading_firmware".localized())
        network.firmvare(version: nil) { [weak self] result in
            switch result {
            case .success(let firmware):
                self?.view.beginFWUpdate()
                self?.lock.peripheral?.update(firmware: firmware.compactMap({$0.bytesArray}).flatMap({$0}))
            case .failure(let error):
                self?.view.show(error: error)
            }
        }
    }
    
    func sensetivityChanged(value: Ellipse.Sensetivity) {
        lock.ellipse.sensorSensitivity = value
        storage.save(lock.ellipse)
        log(.custom(.sensetivityChange), attributes: [.value(value.stringValue)])
    }
    
    func isLockShared() -> Bool {
        return lock.ellipse.isShared
    }
}

extension LockDetailsInteractor: EllipseDelegate {
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
        switch connection {
        case .updating(let progress):
            view.updateFW(progress: progress)
            if progress >= 1 {
                view.finishFWUpdate()
                log(.custom(.fwUpdate))
            }
            isUpdating = true
        case .paired:
            guard isUpdating else {return}
            isUpdating = false
            view.stopLoading(completion: {
                AlertView.alert(title: "success".localized(), text: "fw_update_success".localized(), actions: [.ok]).show()
            })
        default:
            break
        }
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {
        
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate value: Peripheral.Value) {
        switch value {
        case .firmwareVersion(let ver) where sections.isEmpty == false:
            self.checkUpdates(current: ver)
        case .serialNumber(let serial) where sections.isEmpty == false:
            self.sections[0].items[2] = .serial(serial)
            self.view?.reloadRows(at: [IndexPath(row: 2, section: 0)])
        default:
            break
        }
    }
}

extension LockDetailsInteractor: OnboardingPinPageDelegate {
    func save(pin: [Ellipse.Pin]) {
        view.startLoading(text: "saving_new_pin_code".localized())
        network.save(pinCode: pin, forLock: lock.macId) { [weak self] result in
            switch result {
            case .success:
                do {
                    try self?.lock.peripheral?.set(pinCode: pin.map(Peripheral.Pin.init))
                    if var ellipse = self?.lock.ellipse {
                        ellipse.pin = pin
                        self?.storage.save(ellipse)
                    }
                    self?.view.stopLoading(completion: nil)
                    self?.router.pop()
                    log(.custom(.pinSave))
                } catch {
                    self?.errorHandler.handle(error: error)
                }
            case .failure(let error):
                self?.fail(error)
            }
        }
    }
}

extension String {
    var version: String {
        return replacingOccurrences(of: ".", with: "").trimmingCharacters(in: CharacterSet(charactersIn: "0"))
    }
}
