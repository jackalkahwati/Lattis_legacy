//
//  PhysicalDevice.swift
//  Operator
//
//  Created by Ravil Khusainov on 07.10.2021.
//

import Foundation
import Combine

protocol PhysicalDevice: AnyObject {
    var security: CurrentValueSubject <Device.Security, Error> { get }
    var link: CurrentValueSubject <Device.Link, Error> { get }
    func connect()
    func unlock()
    func lock()
}

enum Device {
    enum Security: Hashable {
        case undefined
        case locked
        case unlocked
        case locking
        case unlocking
    }
    
    enum Link: Hashable {
        case nearby
        case connected
        case disconnected
        case discovery
        case connecting
    }
}


extension Device {
    static func physicalDevice(from thing: Thing) -> PhysicalDevice? {
        guard let vendor = Thing.Vendor(rawValue: thing.metadata.vendor) else { return nil }
        switch vendor {
        case .ellipse:
            return EllipseDevice(thing)
        case .axa:
            return AxaDevice(thing)
        default:
            return nil
        }
    }
}

extension PhysicalDevice {
    func toggle() {
        switch security.value {
        case .locked:
            unlock()
        case .unlocked:
            lock()
        default:
            break
        }
    }
}
