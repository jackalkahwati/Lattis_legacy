//
//  EllipseDetailFormViewModel.swift
//  Operator (iOS)
//
//  Created by Ravil Khusainov on 28.10.2021.
//

import Combine
import Dispatch

final class EllipseDetailFormViewModel: ObservableObject {
    
    @Published var link: Device.Link = .discovery
    @Published var credentials: EllipseDevice.Credentials?
    @Published var security: Device.Security = .undefined
    @Published var fwVersion: String?
    @Published var batteryLevel: String?
    @Published var capTouchEnabled: Bool = true
    @Published var serialNumber: String?
    @Published var autoLockEnabled: Bool?
    @Published var shackleInserted: Bool?
    @Published var resetting: Bool = false
    
    let thing: Thing
    let device: EllipseDevice
    
    fileprivate var cancellabels: Set<AnyCancellable> = []
    
    init(thing: Thing) {
        self.thing = thing
        self.device = Device.physicalDevice(from: thing) as! EllipseDevice
        fwVersion = thing.metadata.fwVersion
        batteryLevel = thing.batteryLevel
        handleUpdates()
    }
    
    func toggleCapTouch() {
        self.device.setCapTouch(enabled: !capTouchEnabled)
    }
    
    func toggleAutoLock() {
        guard let enabled = autoLockEnabled else { return }
        self.device.setAutoLock(enabled: !enabled)
    }
    
    func reset() {
        resetting = true
        unlock()
        DispatchQueue.main.asyncAfter(deadline: .now() + 5, execute: device.reset)
        DispatchQueue.main.asyncAfter(deadline: .now() + 8) { [weak self] in
            self?.resetting = false
        }
    }
    
    func unlock() {
        CircleAPI.pin(lockId: thing.id)
            .sink { result in
                
            } receiveValue: { pin in
                self.device.unlock(with: pin)
            }
            .store(in: &cancellabels)
    }
    
    fileprivate func handleUpdates() {
        device.credentials
            .sink { result in
                
            } receiveValue: { cred in
                self.credentials = cred
                self.calculateStatus()
            }
            .store(in: &cancellabels)
        
        device.link
            .sink { result in
                
            } receiveValue: { link in
                self.calculateStatus(link: link)
            }
            .store(in: &cancellabels)
        
        device.security
            .sink { result in
                
            } receiveValue: { sec in
                self.security = sec
            }
            .store(in: &cancellabels)
        device.fwVersion
            .assign(to: \.fwVersion, on: self)
            .store(in: &cancellabels)
        device.batteryLevel
            .assign(to: \.batteryLevel, on: self)
            .store(in: &cancellabels)
        device.capTouchEnabled
            .assign(to: \.capTouchEnabled, on: self)
            .store(in: &cancellabels)
        device.serialNumber
            .assign(to: \.serialNumber, on: self)
            .store(in: &cancellabels)
        device.autoLockEnabled
            .assign(to: \.autoLockEnabled, on: self)
            .store(in: &cancellabels)
        device.shackleInserted
            .assign(to: \.shackleInserted, on: self)
            .store(in: &cancellabels)
    }
    
    fileprivate func calculateStatus(link: Device.Link? = nil) {
        if credentials != nil {
            if self.link == .discovery && !device.isScanning {
                device.scan()
            } else if let link = link {
                self.link = link
            }
        }
    }
}

