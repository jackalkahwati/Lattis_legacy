//
//  SasDeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 06.04.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import Model
import SasBLE
import OvalBackend
import SwiftUI
import Combine


final class SasDeviceManager: DeviceRepresenting {
    
    let kind: Device.Kind = .tapkey
    var security: Device.Security = .locked { didSet { sendState() }}
    var connection: Device.Connection = .search { didSet { sendState() }}
    let consent: String? = nil
    let qrCode: String?
    var bleRestricted: Bool = false
    
    let thing: Thing
    
    fileprivate let ble: SasBLE
    fileprivate var device: SasBLE.Device?
    fileprivate var scanSub: AnyCancellable?
    fileprivate var deviceSub: AnyCancellable?
    fileprivate var runningSubs: Set<AnyCancellable> = []
    fileprivate let timeout = Timeout(30)
    fileprivate var showuldUnlock: Bool = false
    
    init(_ thing: Thing) {
        self.thing = thing
        self.qrCode = thing.qrCode
        self.ble = SasBLE(OvalBackend(fleetId: thing.fleetId))
        ble.lost
            .sink { [weak self] device in
                if device == self?.device {
                    self?.device = nil
                    self?.ble.stop()
                    self?.scanSub?.cancel()
                }
            }
            .store(in: &runningSubs)
        ble.isOn
            .sink { [weak self] isOn in
                self?.bleRestricted = !isOn
                self?.send(.bleEnabled(isOn))
            }
            .store(in: &runningSubs)
    }
    
    func lock() {
        guard security == .unlocked else { return }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2, execute: { [weak self] in
            self?.security = .locked
        })
    }
    
    func unlock() {
        if device == nil {
            showuldUnlock = true
            connect()
            return
        }
        guard security == .locked else { return }
        security = .progress
        deviceSub = device?.status
            .sink { [unowned self] completion in
                
            } receiveValue: { [unowned self] status in
                self.handle(status: status)
            }
        device?.unlock()
    }
    
    func connect() {
        guard connection != .connected || showuldUnlock else { return }
        scanSub = ble.found
            .sink { [unowned self] device in
                guard self.thing.key.uppercased() == device.id.uppercased() else { return }
                self.device = device
                self.connection = .connected
                self.security = .locked
                self.timeout.stop()
                if showuldUnlock {
                    self.showuldUnlock = false
                    self.unlock()
                }
            }
        ble.scan(true)
        timeout.fire { [unowned self] in
            self.connection = .disconnected
            self.security = .undefined
            self.ble.stop()
        }
    }
    
    func disconnect() {
        connection = .disconnected
    }
    
    func refreshStatus() {
        sendState()
    }
    
    fileprivate func handle(status: SasBLE.Device.Status) {
        switch status {
        case .unlocked:
            security = .unlocked
            lock()
            deviceSub?.cancel()
        default:
            break
        }
    }
}

extension OvalBackend: SASBackend {
    
    @AppStorage("sas-fleet-id")
    static var fleetId: String = ""
    
    init(fleetId: Int) {
        OvalBackend.fleetId = "\(fleetId)"
        self.init()
    }
    
    public func token(for nonce: String, device: String) async throws -> String {
        struct Tkn: Codable {
            let token: String
        }
        let result: Tkn = try await get(.init(.sas, path: "credentials/\(device.uppercased())/\(nonce.uppercased())", query: [.init(name: "fleetId", value: OvalBackend.fleetId)]))
        return result.token
    }
}

final class Timeout {
    let duration: TimeInterval
    var timer: Timer?
    
    init(_ duration: TimeInterval) {
        self.duration = duration
    }
    
    func fire(reach: @escaping () -> ()) {
        stop()
        timer = Timer.scheduledTimer(withTimeInterval: duration, repeats: false, block: { timer in
            timer.invalidate()
            reach()
        })
    }
    
    func stop() {
        timer?.invalidate()
    }
}
