//
//  InversDetailFormViewModel.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.11.2021.
//

import Foundation
import Combine
import SwiftUI

final class InversDetailFormViewModel: ObservableObject {
    
    let thing: Thing
    @Published var link: Device.Link = .connecting
    @Published var centralLock: InversDevice.Security = .locked
    @Published var immobilizer: InversDevice.Security = .locked
    @Published var processingLock = true
    @Published var processingImmo = true
    fileprivate var cancellables: Set<AnyCancellable> = []
    
    init(_ thing: Thing) {
        self.thing = thing
        connnect()
    }
    
    func toggleCentralLock() {
        let new: InversDevice.Security = centralLock == .locked ? .unlocked : .locked
        processingLock = true
        CircleAPI.change(.init(central_lock: new, immobilizer: nil, ignition: nil), inverse: thing.metadata.key)
            .sink { result in
                
            } receiveValue: { [unowned self] in
                self.processingLock = false
                self.centralLock = new
            }
            .store(in: &cancellables)
    }
    
    func toggleImmobilizer() {
        let new: InversDevice.Security = immobilizer == .locked ? .unlocked : .locked
        processingImmo = true
        CircleAPI.change(.init(central_lock: nil, immobilizer: new, ignition: nil), inverse: thing.metadata.key)
            .sink { result in
                
            } receiveValue: { [unowned self] in
                self.processingImmo = false
                self.immobilizer = new
            }
            .store(in: &cancellables)
    }
    
    fileprivate func connnect() {
        CircleAPI.status(thing.metadata.key)
            .sink {[unowned self] result in
                
            } receiveValue: { [unowned self] status in
                self.link = .connected
                self.handle(status)
            }
            .store(in: &cancellables)

    }
    
    fileprivate func handle(_ status: InversDevice.Status) {
        if let central = status.central_lock {
            self.processingLock = false
            centralLock = central == .locked ? .locked : .unlocked
        }
        if let immo = status.immobilizer {
            self.processingImmo = false
            immobilizer = immo == .locked ? .locked : .unlocked
        }
    }
}
