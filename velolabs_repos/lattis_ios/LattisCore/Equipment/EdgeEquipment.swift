//
//  EdgeEquipment.swift
//  LattisCore
//
//  Created by Roger Molas on 11/10/22.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import Combine
import Model
import OvalBackend

final class EdgeEquipment: Equipment {
    
    let thing: Thing
    let asset: Asset
    var callback: PassthroughSubject<EquipmentControler.Callback, Error> = .init()
    
    fileprivate let backend = OvalBackend()
    
    init(_ thing: Thing, asset: Asset) {
        self.thing = thing
        self.asset = asset
        connect()
    }
    
    func lock() {
        callback.send(.status(.locked))
    }
    
    func unlock() {
        callback.send(.status(.processing))
        Task {
            do {
                try await self.backend.unlock(equipment: thing.controllerId, asset: asset)
                self.callback.send(.alert(.success))
                self.lock()
            } catch {
                self.callback.send(completion: .failure(error))
            }
        }
    }
    
    func connect() {
        Task {
            do {
                let info = try await self.backend.info(equipment: thing.controllerId, asset: asset)
                if info.locked {
                    self.callback.send(.status(.locked))
                } else {
                    self.callback.send(.status(.unlocked))
                }
            } catch {
                self.callback.send(completion: .failure(error))
            }
        }
    }
}

