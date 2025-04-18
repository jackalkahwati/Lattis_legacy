//
//  EdgeDeviceManager.swift
//  LattisCore
//
//  Created by Roger Molas on 11/10/22.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import Model
import OvalBackend


final class EdgeDeviceManager: DeviceRepresenting {
    
    let kind: Device.Kind = .edge
    let consent: String? = "we_can_temporarily_turn_off".localized()
    var security: Device.Security = .undefined { didSet { sendState() }}
    var connection: Device.Connection = .disconnected { didSet { sendState() }}
    let qrCode: String?
    let bleRestricted: Bool = false
    
    let thing: Thing
    let bike: Bike
    
    fileprivate let backend: OvalBackend
    
    init(_ thing: Thing, bike: Bike) {
        self.thing = thing
        self.bike = bike
        self.qrCode = thing.qrCode
        self.backend = .init()
    }
    
    func lock() {
        guard security != .locked else { return }
        security = .progress
        Task {
            do {
                let _ = try await self.backend.lockEdge(controller: thing.controllerId, bikeId: bike.bikeId)
                security = .locked
            } catch {
                send(.failure(error))
            }
        }
    }
    
    @MainActor
    func unlock() {
        guard security != .unlocked else { return }
        security = .progress
        Task {
            do {
                _ = try await self.backend.unlockEdge(controller: thing.controllerId, bikeId: bike.bikeId)
                security = .unlocked
            } catch {
                send(.failure(error))
            }
        }
    }
    
    @MainActor
    func connect() {
        connection = .connecting
        refreshStatus()
    }
    
    func disconnect() {
        connection = .disconnected
    }
    
    @MainActor
    func refreshStatus() {
        security = .progress
        Task {
            do {
                let status = try await self.backend.iotStatus(thing: thing)
                self.security = status.locked ? .locked : .unlocked
                self.connection = .connected
            } catch {
                self.send(.failure(error))
            }
        }
    }
}

fileprivate extension OvalBackend {
    func lockEdge(controller: Int, bikeId: Int) async throws -> EmptyJSON {
        struct Params: Encodable {
            let bike_id: String?
            let endUrl: String?
            
        }
        return try await post(Params(bike_id:"\(bikeId)", endUrl: "bike"),
                              endpoint: .init(.equipment, path: "\(controller)/lock"))
    }
    
    func unlockEdge(controller: Int, bikeId: Int) async throws -> EmptyJSON {
        struct Params: Encodable {
            let bike_id: String?
            let endUrl: String?
        }
        return try await post(Params(bike_id: "\(bikeId)", endUrl: "bike"),
                              endpoint: .init(.equipment, path: "\(controller)/unlock"))
    }
    func iotStatus(thing: Thing) async throws -> Thing.Status {
        try await get(.init(.bikes, path: "\(thing.bikeId!)/iot/status",
                            query: [.init(name: "controller_key", value: thing.key)]))
    }
}
