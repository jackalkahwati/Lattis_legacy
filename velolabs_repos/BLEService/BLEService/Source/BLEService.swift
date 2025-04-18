
//  BLEService.swift
//  BLEService
//
//  Created by Ravil Khusainov on 04/02/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreBluetooth

public final class BLEService: NSObject {
    public static let shared = BLEService()
    public class func setup(network: Network, backgroundEnabled: Bool = true) {
        shared.network = network
        let options: [String: Any]? = backgroundEnabled ? [CBCentralManagerOptionShowPowerAlertKey: true, CBCentralManagerOptionRestoreIdentifierKey: "ble.periperalmanager"] : nil
        shared.peripheralManager = CBCentralManager(delegate: shared, queue: shared.queue, options: options)
    }
    
    override init() {
        super.init()
        peripheralManager = CBCentralManager(delegate: self, queue: queue, options: nil)
    }
    
    fileprivate(set) public var network: Network!
    fileprivate var peripheralManager: CBCentralManager!
    fileprivate let queue = DispatchQueue(label: "ble.periperalmanager.queue")
    fileprivate var peripherals: [Peripheral] = []
    fileprivate weak var peripheralToConnect: CBPeripheral?
    fileprivate var scanOperation: BlockOperation?
    fileprivate var delegates = WeakSet<AnyObject>()
    fileprivate let services = Peripheral.Service.all.map{ $0.uuid }
    fileprivate var updateTimer: Timer?
    fileprivate var alivePerIds: Set<UUID> = []
    
    public func startScan() {
        guard peripheralManager.isScanning == false else { return }
        scanOperation = BlockOperation(block: {
            self.peripheralManager.scanForPeripherals(withServices: self.services, options: nil)
        })
        if peripheralManager.state == .poweredOn {
            scanOperation?.start()
            scanOperation = nil
        }
//        updateTimer?.invalidate()
//        updateTimer = Timer.scheduledTimer(timeInterval: 20, target: self, selector: #selector(updatePeripheralsState(timer:)), userInfo: nil, repeats: true)
    }
    
    public func stopScan() {
        updateTimer?.invalidate()
        updateTimer = nil
        guard peripheralManager.isScanning else { return }
        peripheralManager.stopScan()
    }
    
    public func subscribe(delegate: BLEServiceDelegate) {
        delegates.insert(delegate)
        delegate.service(self, didUpdateBluetoothStatus: peripheralManager.state == .poweredOn)
        delegate.service(self, didRefresh: peripherals)
    }
    
    public func unsubsribe(_ delegate: BLEServiceDelegate) {
        delegates.remove(delegate)
    }
    
    public func connect(_ peripheral: Peripheral, stopScan: Bool = true) {
        if stopScan {
            self.stopScan()
        }
        if case .connecting = peripheral.connectionState { return }
        guard peripheral.isConnected == false else { return }
        func perform() {
            let connected = peripheralManager.retrieveConnectedPeripherals(withServices: services)
            if connected.isEmpty == false {
                peripheralToConnect = peripheral.peripheral
                connected.forEach{ self.peripheralManager.cancelPeripheralConnection($0) }
            } else {
                peripheralManager.connect(peripheral.peripheral, options: nil)
            }
        }
        guard peripheral.peripheral.isEllboot == false else {
            return perform()
        }
        guard peripheral.userId == nil || peripheral.signedMessage == nil || peripheral.publicKey == nil else { return perform() }
        network.sign(lockWith: peripheral.macId, success: { (message, key, userId) in
            peripheral.signedMessage = message
            peripheral.publicKey = key
            peripheral.userId = userId
            perform()
        }, fail: { error in
            peripheral.connectionState = .failed(error)
        })
    }
    
    public func flashLED(on peripheral: Peripheral) {
        peripheral.flash = { [unowned self] in
            self.peripheralManager.cancelPeripheralConnection(peripheral.peripheral)
        }
        peripheralManager.connect(peripheral.peripheral, options: nil)
    }
    
    public func disconnect(_ peripheral: Peripheral) {
        peripheral.isConnected = false
        peripheralManager.cancelPeripheralConnection(peripheral.peripheral)
    }
    
    public func clean() {
        if let per = peripherals.filter({ $0.isConnected }).first {
            disconnect(per)
        }
        stopScan()
    }
}

extension BLEService: CBCentralManagerDelegate {
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        guard peripheralManager != nil else { return }
        if peripheralManager.state == .poweredOn {
            scanOperation?.start()
            scanOperation = nil
        }
        DispatchQueue.main.async {
            self.delegates.flatMap({$0 as? BLEServiceDelegate}).forEach({$0.service(self, didUpdateBluetoothStatus: self.peripheralManager.state == .poweredOn)})
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        if let exist = peripherals.find({ $0.macId == peripheral.macId }) {
            if exist.updateState == .needReset, peripheral.isEllboot == false {
                exist.peripheral = peripheral
                self.connect(exist)
            } else {
                alivePerIds.insert(peripheral.identifier)
            }
        } else if let per = Peripheral(peripheral) {
            peripherals.append(per)
            alivePerIds.insert(peripheral.identifier)
            per.ready = { [unowned self] in
                DispatchQueue.main.async {
                    self.delegates.flatMap({$0 as? BLEServiceDelegate}).forEach({ (delegate) in
                        delegate.service(self, didRefresh: self.peripherals)
                        delegate.service(self, didInsert: per)
                    })
                }
            }
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        guard let per = peripherals.find({ $0.macId == peripheral.macId }) else { return }
        per.connect()
        DispatchQueue.main.async {
            self.delegates.flatMap({$0 as? BLEServiceDelegate}).forEach({$0.service(self, didUpdate: per)})
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        if let error = error, let per = peripherals.find({ $0.macId == peripheral.macId }) {
            per.connectionState = .failed(error)
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        if let exist = peripherals.find({ $0.macId == peripheral.macId }) {
            exist.isConnected = false
            if let err = error as NSError?, err.code == 6 || err.code == 7 {
                exist.connectionState = .reconnecting
                connect(exist)
                exist.breakConection = breakConnection(peripheral: )
            } else {
                exist.connectionState = .unpaired
            }
            if exist.updateState == .needReset {
                DispatchQueue.main.async(execute: startScan)
            }
            DispatchQueue.main.async {
                self.delegates.flatMap({$0 as? BLEServiceDelegate}).forEach({$0.service(self, didUpdate: exist)})
            }
        }
        guard let per = peripheralToConnect else { return }
        peripheralToConnect = nil
        peripheralManager.connect(per, options: nil)
    }
    
    public func centralManager(_ central: CBCentralManager, willRestoreState dict: [String : Any]) {
        guard let pers = dict[CBCentralManagerRestoredStatePeripheralsKey] as? [CBPeripheral] else { return }
        if let per = peripherals.filter({
            switch $0.connectionState {
            case .reconnecting:
                return pers.contains($0.peripheral)
            default: return false
            }
        }).first {
            connect(per)
            per.breakConection = breakConnection(peripheral: )
        } else {
            peripherals = pers.flatMap(Peripheral.init)
            for per in peripherals where per.peripheral.state == .connected {
                disconnect(per)
            }
        }
    }
    
    fileprivate func breakConnection(peripheral: Peripheral) {
        disconnect(peripheral)
    }
}

private extension BLEService {
    @objc func updatePeripheralsState(timer: Timer) {
        peripheralManager.scanForPeripherals(withServices: services, options: nil)
        let identifiers = peripherals.map({ $0.peripheral.identifier })
        var shouldUpdate = false
        var deleted: [Peripheral] = []
        for ident in identifiers {
            if let idx = peripherals.index(where: { $0.peripheral.identifier == ident }), alivePerIds.contains(where: { $0 == ident }) == false {
                let per = peripherals.remove(at: idx)
                deleted.append(per)
                shouldUpdate = true
            }
        }
        if shouldUpdate {
            delegates.flatMap({$0 as? BLEServiceDelegate}).forEach({$0.service(self, didDelete: deleted)})
        }
        let connected = peripherals.filter({ $0.isConnected }).map({ $0.peripheral.identifier })
        alivePerIds = Set(connected)
    }
}





