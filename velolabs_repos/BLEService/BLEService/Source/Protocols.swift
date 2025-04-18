//
//  Protocols.swift
//  BLEService
//
//  Created by Ravil Khusainov on 29/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

public protocol BLEServiceDelegate: class {
    func service(_ service: BLEService, didUpdateBluetoothStatus isEnabled: Bool)
    // didRefresh use it if you want only update whole the list witout handling insert/delete/update cases
    func service(_ service: BLEService, didRefresh peripherals: [Peripheral])
    func service(_ service: BLEService, didInsert peripheral: Peripheral)
    func service(_ service: BLEService, didUpdate peripheral: Peripheral)
    func service(_ service: BLEService, didDelete peripherals: [Peripheral])
}

public extension BLEServiceDelegate {
    func service(_ service: BLEService, didUpdateBluetoothStatus isEnabled: Bool){}
    func service(_ service: BLEService, didRefresh peripherals: [Peripheral]){}
    func service(_ service: BLEService, didInsert peripheral: Peripheral){}
    func service(_ service: BLEService, didUpdate peripheral: Peripheral){}
    func service(_ service: BLEService, didDelete peripherals: [Peripheral]){}
}

public protocol PeripheralDelegate: class {
    func peripheral(_ peripheral: Peripheral, didUpdate magnitude: Coordinate)
    func peripheral(_ peripheral: Peripheral, didUpdate metadata: Peripheral.Metadata)
    func peripheral(_ peripheral: Peripheral, got firmwareVersion: String)
    func peripheral(_ peripheral: Peripheral, didChangeConnection state: Peripheral.Connection)
    func peripheral(_ peripheral: Peripheral, didChangeLock state: Peripheral.LockState)
    func peripheral(_ peripheral: Peripheral, didUpdate accelerometer: AccelerometerValue)
}

public extension PeripheralDelegate {
    func peripheral(_ peripheral: Peripheral, didUpdate magnitude: Coordinate) {}
    func peripheral(_ peripheral: Peripheral, didUpdate metadata: Peripheral.Metadata) {}
    func peripheral(_ peripheral: Peripheral, got firmwareVersion: String) {}
    func peripheral(_ peripheral: Peripheral, didChangeConnection state: Peripheral.Connection) {}
    func peripheral(_ peripheral: Peripheral, didChangeLock state: Peripheral.LockState) {}
    func peripheral(_ peripheral: Peripheral, didUpdate accelerometer: AccelerometerValue) {}
}

public protocol Network {
    func sign(lockWith macId: String, success: @escaping (String, String, String) -> (), fail: @escaping (Error) -> ())
}
