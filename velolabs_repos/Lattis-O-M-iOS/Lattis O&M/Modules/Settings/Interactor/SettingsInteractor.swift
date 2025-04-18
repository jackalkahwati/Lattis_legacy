//
//  SettingsSettingsInteractor.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 18/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import QRCodeReader
import AVFoundation
import Oval
import LattisSDK

public struct QRCodeBike: Codable {
    public let name: String
    public let id: UInt
    
    enum CodingKeys: String, CodingKey {
        case name = "bike_name"
        case id = "qr_id"
    }
}

extension QRCodeBike: Equatable {
    
}

class SettingsInteractor {
    weak var view: SettingsInteractorOutput!
    var router: SettingsRouter!
    var lock: Lock!
    var qrBike: QRCodeBike?
    var checkFWUpdate: (String, @escaping (Bool) -> ()) -> () = {_, _ in}
    var showAutoLockAlert: (@escaping (Bool) -> ()) -> () = {_ in}
    
    fileprivate var groups: [Group] = []
    fileprivate var selectedGroup: Group?
    
    fileprivate let fleets: FleetsStorage = CoreDataStack.shared
    typealias Network = EllipseNetwork & BikeNetwork
    fileprivate let network: Network
    fileprivate let storage: LocksStorage
    init(network: Network = Session.shared, storage: LocksStorage = CoreDataStack.shared) {
        self.network = network
        self.storage = storage
        checkFWUpdate = { [unowned self] version, completion in
            self.checkUpdate(version: version, completion: completion)
        }
        showAutoLockAlert = { [unowned self] completion in
            let alert = AlertController(title: "auto_lock_warning_title".localized(), message: "auto_lock_warning_message".localized())
            alert.actions = [
                .plain(title: "continue".localized(), handler: {
                    completion(true)
                }),
                .plain(title: "general_btn_cancel".localized(), style: .inactive, handler: {
                    completion(false)
                })
            ]
            (self.view as? UIViewController)?.present(alert, animated: true, completion: nil)
        }
    }
}

extension SettingsInteractor: SettingsInteractorInput {
    func viewLoaded() {
        lock.peripheral?.subscribe(self)
//        view.show(lock: lock)
    }
    
    func assignBike() {
        guard let fleet = fleets.currentFleet else { return }
        network.getGroups(for: fleet) { [weak self] result in
            switch result {
            case .success(let groups):
                self?.groups = groups
            case .failure(let e):
                self?.view.show(error: e)
            }
        }
        router.openQRScanner(with: self, completion: { [unowned self] in
            self.assign()
        })
    }
    
    func unassignBike() {
        guard let ellipse = lock.lock else { return }
        network.unassignBike(from: ellipse) { [weak self] result in
            switch result {
            case .success(let lock):
                self?.didUpdate(ellipse: lock)
            case .failure(let e):
                self?.view.show(error: e)
            }
        }
    }
    
    func updateFirmware() {
        view.update(progress: 0)
        network.firmvare(version: nil) { [weak self] result in
            switch result {
            case .success(let firmware):
                self?.update(firmware: firmware)
            case .failure(let error):
                self?.view.show(error: error)
            }
        }
    }
    
    func changeLabel() {
        view.showLabelDialog {
            self.router.openQRScanner(with: self, completion: self.updateLabel)
        }
    }
    
    func dispatch() {
        router.dispatch(for: lock)
    }
    
    func select(group: Group) {
        selectedGroup = group
        assign()
    }
    
    func delete() {
        guard let macId = lock.macId else { return }
        network.removeLock(macId: macId) { _ in }
    }
    
    func switchCapTouch(isOn: Bool) {
        lock.peripheral?.isCapTouchEnabled = isOn
    }
}

extension SettingsInteractor: QRCodeReaderViewControllerDelegate {
    func reader(_ reader: QRCodeReaderViewController, didScanResult result: QRCodeReaderResult) {
        qrBike = result.bike
    }
    
    func reader(_ reader: QRCodeReaderViewController, didSwitchCamera newCaptureDevice: AVCaptureDeviceInput) {}
    
    func readerDidCancel(_ reader: QRCodeReaderViewController) {
        reader.dismiss(animated: true, completion: nil)
    }
}

extension SettingsInteractor: EllipseDelegate {
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {
        
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
        switch connection {
        case .updating(let progress):
            self.view.update(progress: Double(progress))
        case .paired:
            self.view.show(lock: self.lock)
        default:
            break
        }
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate value: Peripheral.Value) {
        switch value {
        case .firmwareVersion:
            self.view.show(lock: lock)
        default:
            break
        }
    }
}

private extension SettingsInteractor {
    func didUpdate(ellipse: Ellipse) {
        storage.save([ellipse], update: false) {}
        lock.lock = ellipse
        view.show(lock: lock)
    }
    
    func checkUpdate(version: String, completion: @escaping (Bool) -> ()) {
        network.firmvareVersions { [weak self] result in
            switch result {
            case .success(let avaliable):
                guard let latest = avaliable.map({$0.replacingOccurrences(of: ".", with: "")}).sorted(by: <).last else { return completion(false) }
                completion(latest > "0" + version.replacingOccurrences(of: ".", with: ""))
            case .failure(let e):
                self?.view.show(error: e)
            }
        }
    }
    
    func assign() {
        guard let values = assignValues else { return }
        network.assign(bikeWith: values.0, and: selectedGroup, to: values.1) { [weak self] result in
            switch result {
            case .success(let lock):
                EBikeHandler.shared.update(ellipse: lock)
                self?.didUpdate(ellipse: lock)
            case .failure(let e):
                self?.handleLabel(error: e, qrCode: values.0)
            }
        }
        selectedGroup = nil
    }
    
    func updateLabel() {
        guard let bike = lock.lock?.bikeId, let qr = qrBike else { return }
        view.startLoading(title: "")
        network.changeLabel(for: bike, qrCode: qr) { [weak self] result in
            switch result {
            case .success(let lock):
                self?.view.stopLoading {}
                self?.didUpdate(ellipse: lock)
            case .failure(let e):
                self?.handleLabel(error: e, qrCode: qr)
            }
        }
    }
    
    func handleLabel(error: Error, qrCode: QRCodeBike) {
        if let error = error as? SessionError {
            switch error.code {
            case .unauthorized:
                let subtitle = String(format: "settings_alert_qr_code_is_busy".localized(), "\(qrCode.name): \(qrCode.id)")
                view.showAlert(title: nil, subtitle: subtitle)
            case .badRequest:
                if self.groups.isEmpty {
                    self.view.showAlert(title: nil, subtitle: "settings_alert_no_group".localized())
                } else {
                    self.view.show(groups: self.groups, bike: self.qrBike!)
                }
            default:
                view.showAlert(title: nil, subtitle: nil)
            }
        } else {
            view.show(error: error)
        }
    }
    
    var assignValues: (QRCodeBike, Ellipse)? {
        guard let qrCode = qrBike else {
            view.showAlert(title: nil, subtitle: "settings_alert_no_qr_code".localized())
            return nil
        }
        guard let ellipse = lock.lock else {
            view.showAlert(title: nil, subtitle: "settings_alert_no_ellipse".localized())
            return nil
        }
//        guard selectedGroup != nil else {
//            view.showAlert(title: nil, subtitle: "settings_alert_no_group".localized())
//            return nil
//        }
        return (qrCode, ellipse)
    }
    
    func update(firmware: [String]) {
        let update = firmware.compactMap({$0.bytesArray}).flatMap({$0})
        lock.peripheral?.update(firmware: update)
    }
}

extension QRCodeReaderResult {
    var bike: QRCodeBike? {
        guard let data = value.data(using: .utf8) else { return nil }
        return try? JSONDecoder().decode(QRCodeBike.self, from: data)
    }
}


