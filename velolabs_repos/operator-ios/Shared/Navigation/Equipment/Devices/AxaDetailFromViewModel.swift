//
//  AxaDetailFromViewModel.swift
//  Operator
//
//  Created by Ravil Khusainov on 28.10.2021.
//

import Foundation
import Combine


final class AxaDetailFromViewModel: ObservableObject {
    
    let thing: Thing
    let device: AxaDevice
    
    @Published var link: Device.Link = .discovery
    @Published var security: Device.Security = .undefined
    @Published var batteryLevel: String? = nil
    @Published var modelNumber: String? = nil
    @Published var hwVersion: String? = nil
    @Published var fwVersion: String? = nil
    @Published var sfVersion: String? = nil
    fileprivate var cancellabels: Set<AnyCancellable> = []
    
    init(_ thing: Thing) {
        self.thing = thing
        self.device = AxaDevice(thing)
        handleStates()
        device.scan()
    }
    
    fileprivate func handleStates() {
        device.link
            .catch(linkFailed)
            .assign(to: \.link, on: self)
            .store(in: &cancellabels)
                    
        device.security
            .catch(securityFailed)
            .assign(to: \.security, on: self)
            .store(in: &cancellabels)
                    
        device.batteryLevel
            .sink { level in
                guard let lvl = level else { return }
                self.batteryLevel = "\(lvl)%"
            }
            .store(in: &cancellabels)
        
        device.hwVersion
            .assign(to: \.hwVersion, on: self)
            .store(in: &cancellabels)
        
        device.modelNumber
            .assign(to: \.modelNumber, on: self)
            .store(in: &cancellabels)
        
        device.sfVersion
            .assign(to: \.sfVersion, on: self)
            .store(in: &cancellabels)
        
        device.fwVersion
            .assign(to: \.fwVersion, on: self)
            .store(in: &cancellabels)
    }
    
    fileprivate func linkFailed(_ error: Error) -> Just<Device.Link> {
        Just(.discovery)
    }
    
    fileprivate func securityFailed(_ error: Error) -> Just<Device.Security> {
        Just(.undefined)
    }
}
