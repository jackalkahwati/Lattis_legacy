//
//  EquipmentController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 28.03.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Combine
import Model
import OvalBackend

@MainActor
final class EquipmentControler: ObservableObject {
    
    @Published var status: Status = .fetching
    
    let thing: Thing
    let asset: Asset
    let callback: PassthroughSubject<Callback, Error> = .init()
    
    fileprivate let equipment: Equipment!
    fileprivate var storage: Set<AnyCancellable> = []
    
    init(_ thing: Thing, asset: Asset) {
        self.thing = thing
        self.asset = asset
        self.equipment = thing.equipment(asset: asset)
        subscribe()
    }
    
    var active: Bool {
        status ~~ [.locked, .unlocked]
    }
    
    func toggle() {
        switch status {
        case .locked:
            equipment.unlock()
        case .unlocked:
            equipment.lock()
        default:
            break
        }
    }
    
    fileprivate func subscribe() {
        equipment.callback
            .sink { [unowned self] result in
                switch result {
                case .failure(let error):
                    DispatchQueue.main.async {
                        self.handle(error: error)
                    }
                case .finished:
                    break
                }
            } receiveValue: { [unowned self] value in
                switch value {
                case .status(let status):
                    self.status = status
                default:
                    self.callback.send(value)
                }
            }
            .store(in: &storage)
    }
    
    fileprivate func handle(error: Error) {
        switch status {
        case .processing:
            status = .locked
            callback.send(.alert(.unsuccessful))
        case .fetching:
            status = .disconnected
        default:
            break
        }
        callback.send(completion: .failure(error))
    }
}

extension EquipmentControler {
    
    enum Status {
        case disconnected
        case fetching
        case locked
        case unlocked
        case processing
    }
    
    enum Alert {
        case code(String)
        case success
        case unsuccessful
    }
    
    enum Callback {
        case alert(Alert)
        case show(message: String)
        case hide(message: String)
        case status(Status)
    }
}

