//
//  DeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30.09.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import Model

extension DeviceRepresenting {
    func sendState() {
        NotificationCenter.default.post(name: .deviceStatusUpdated, object: self)
    }
    func send(_ message: DeviceManager.Message) {
        NotificationCenter.default.post(name: .deviceMessage, object: self, userInfo: ["message": message])
    }
}

final class DeviceManager {
    var security: Device.Security {
        // For single device we translate it's state
        // Else we translate state of the first device without consent value
        if let device = list.first, list.count == 1 {
            return device.security
        }
//        } else if let device = list.first(where: {$0.consent == nil}) {
//            return device.security
//        }
        let set = Set(list.filter({$0.kind != .adapter}).map(\.security))
        if set.count == 1, let first = set.first {
            return first
        }
        return .undefined
    }
    
    var connection: Device.Connection {
        let connections = list.map(\.connection)
        let set = Set(connections)
        if let first = connections.first, set.count == 1 {
            return first
        }
        if connections.contains(.connecting) {
            return .connecting
        }
        return .disconnected
    }

    var qrCode: String? {
        guard let device = list.first, list.count == 1 else { return nil }
        return device.qrCode
    }
    var bleRestricted: Bool { list.map(\.bleRestricted).first(where: {$0}) ?? false }
    
    var state: State {
        let types = list.map(\.kind)
        if types.count == 1 && types.contains(.adapter) {
            return .hub
        }
        if types.contains(.iot) && types.contains(.ellipse) {
            return .ellipseWithIot
        }
        if types.contains(.iot) && types.contains(.adapter) && types.count == 2 {
            return .iotWithAdapter
        }
        if types.contains(.iot) || types.contains(.omni) {
            return .iot
        }
        if types.contains(.ellipse) {
            return .ellipse
        }
        if types.contains(.axa) {
            return .axa
        }
        if types.contains(.manualLock) {
            return .manualLock
        }
        if types.contains(.tapkey) || types.contains(.kisi) {
            return .tapkey
        }
        if types.contains(.parcelHive) {
            return .parcelHive
        }
        if types.contains(.edge) {
            return .edge
        }
        return .ellipse
    }
    
    let list: [DeviceRepresenting]
    fileprivate var unlockCompletion: (() -> Void)? = nil
    fileprivate var lockCompletion: (() -> Void)? = nil
    fileprivate var connectCompletion: ((Error?) -> Void)? = nil
    fileprivate var requestCompletion: ((String) -> Void)? = nil
    
    init(_ list: [DeviceRepresenting]) {
        self.list = list
        NotificationCenter.default.addObserver(self, selector: #selector(handle(notification:)), name: .deviceStatusUpdated, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(handle(notification:)), name: .deviceMessage, object: nil)
    }
    
    func connect(completion: ((Error?) -> Void)? = nil) {
        if connection == .connected {
            completion?(nil)
        } else {
            connectCompletion = completion
            list.forEach{$0.connect()}
        }
    }
    
    func refreshStatus() {
        list.forEach{$0.refreshStatus()}
    }
    
    func disconnect() {
        list.forEach{$0.disconnect()}
    }
    
    func strongLock(completion: (() -> Void)? = nil) {
        #if targetEnvironment(simulator)
        completion?()
        return
        #endif
        for device in list {
            if device.security != .locked {
                lockCompletion = completion
                device.lock()
            }
        }
        if lockCompletion == nil {
            completion?()
        }
    }
    
    func weakLock(completion: ((String) -> Void)? = nil) {
        for device in list {
            if device.consent == nil || list.count == 1 || state == .iotWithAdapter {
                device.lock()
            } else {
                requestCompletion = completion
            }
        }
    }
    
    func unlock(completion: (() -> Void)? = nil) {
        for device in list {
            if device.security != .unlocked {
                unlockCompletion = completion
                device.unlock()
                if completion != nil {
                    completion?()
                }
            }
        }
        if unlockCompletion == nil {
            completion?()
        }
    }
    
    @objc
    fileprivate func handle(notification: Notification) {
        guard let device = notification.object as? DeviceRepresenting else { return }
        if list.first(where: {$0.security != .locked}) == nil, let completion = lockCompletion {
            lockCompletion = nil
            completion()
        }
        if list.first(where: {$0.security != .unlocked}) == nil, let completion = unlockCompletion {
            unlockCompletion = nil
            completion()
        }
        if connection == .connected, let completion = connectCompletion {
            connectCompletion = nil
            completion(nil)
        }
        if device.security == .locked,
           let request = list.compactMap(\.consent).first,
           let completion = requestCompletion {
            requestCompletion = nil
            completion(request)
        }
        guard let message = notification.userInfo?["message"] as? DeviceManager.Message else {
            return
        }
        switch message {
        case .failure(let error):
            if let completion = connectCompletion {
                connectCompletion = nil
                completion(error)
            }
        default:
            break
        }
    }
}

enum DeviceError: Error {
    case badShackle
}

extension DeviceManager {
    enum Message {
        case ellipseJamming(Bool)
        case axaCloseAlert(Bool)
        case linkaOperationAlert(Bool, Bool)
        case bleEnabled(Bool)
        case failure(Error)
    }
    
    enum State {
        case iot
        case ellipse
        case axa
        case noke
        case ellipseWithIot
        case iotWithAdapter
        case hub
        case manualLock
        case tapkey
        case parcelHive
        case edge
    }
}
