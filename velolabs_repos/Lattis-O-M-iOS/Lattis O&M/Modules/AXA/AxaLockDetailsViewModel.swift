//
//  AxaLockDetailsViewModel.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 18.03.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import Foundation
import AXALock
import Oval
import UIKit
import CoreLocation


final class AxaLockDetailsViewModel {
    fileprivate(set) var device: AxaDevice
    var lock: AxaBLE.Lock { device.lock }
    var present: ((UIViewController?) -> ())? = nil
    var load: ((Bool) -> ())? = nil
    var reloadRows: (([IndexPath]) -> ())? = nil
    let sections = ["settings_lock_section_title".localized(), "settings_bike_section_title".localized()]
    fileprivate let handler = AxaBLE.Handler()
    fileprivate var information: [AxaLockDetailsInfo] = []
    fileprivate let network: BikeNetwork & IoTNetwork = Session.shared
    
    init(_ device: AxaDevice) {
        self.device = device
        updateLockInfo()
        handler.lockInfoUpdated = { [unowned self] _ in
            self.updateLockInfo()
        }
        handler.add(lock)
    }
    
    func numberOfRows(in section: Int) -> Int {
        if section == 0 {
            return information.count
        }
        if device.bike != nil {
            return 3
        }
        return 1
    }
    
    func info(for indexPath: IndexPath) -> AxaLockDetailsInfo {
        if indexPath.section == 0 {
            return information[indexPath.row]
        } else if let bike = device.bike {
            switch indexPath.row {
            case 0:
                return .init(title: "settings_bike_name".localized(), value: bike.name, action: nil)
            case 1:
                return .init(title: nil, value: nil, action: .plain(title: "settings_unassign_bike".localized(), handler: { [unowned self] in
                    self.unassignBike()
                }))
            default:
                return .init(title: nil, value: nil, action: .plain(title: "settings_change_label".localized(), handler:{ [unowned self] in
                    self.assignBike()
                }))
            }
        }
        return .init(title: "no_bike_linked_axa".localized(), value: nil, action: .plain(title: "assign_lock".localized(), handler: { [unowned self] in
            self.assignBike()
        }))
    }
    
    
    
    fileprivate func updateLockInfo() {
        func handle(info: AxaLockDetailsInfo, idx: Int) {
            if information.count >= idx + 1 {
                information[idx] = info
            } else {
                information.append(info)
            }
        }
        handle(info: .init(title: "Manufacturer", value: lock.manufacturerName, action: nil), idx: 0)
        handle(info: .init(title: "Model Number", value: lock.modelNumber, action: nil), idx: 1)
        handle(info: .init(title: "Serial Number", value: lock.serialNumber, action: nil), idx: 2)
        handle(info: .init(title: "Firmware Version", value: lock.fwVersion, action: nil), idx: 3)
        handle(info: .init(title: "Hardware Version", value: lock.hwVersion, action: nil), idx: 4)
        handle(info: .init(title: "Software Version", value: lock.sfVersion, action: nil), idx: 5)
        handle(info: .init(title: "Battry Level", value: "\(lock.batteryLevel ?? 0)%", action: nil), idx: 6)
        if let reload = reloadRows {
            let rows = [0, 1, 2, 3, 4, 5, 6].map{IndexPath(row: $0, section: 0)}
            reload(rows)
        }
    }
    
    fileprivate func assignBike() {
        let controller = AxaLockAssignViewController(device) { [unowned self] (bike) in
            self.didAssign(bike: bike)
        }
        present?(UINavigationController(rootViewController: controller, style: .blue))
    }
    
    fileprivate func didAssign(bike: Bike) {
        present?(nil)
        device = device.change(bike: bike)
        reloadRows?([])
    }
    
    fileprivate func unassignBike() {
        load?(true)
        network.unassignBike(from: device.module) { [weak self] (result) in
            switch result {
            case .success:
                self?.didUnassignBike()
            case .failure(let error):
                print(error)
            }
        }
    }
    
    fileprivate func didUnassignBike() {
        device = device.change(bike: nil)
        load?(false)
        reloadRows?([])
    }
}
