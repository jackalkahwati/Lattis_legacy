//
//  EllipseManager.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 8/1/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import Foundation
import CoreBluetooth
@_implementationOnly
import OvalAPI

fileprivate var sharedChallengeKey: String?

internal let main: (@escaping () -> ()) -> () = { DispatchQueue.main.async(execute: $0) }
internal func safe<A>(_ collection: WeakCollection<A>, enumerate: @escaping (A) -> ()) {
    guard collection.isEmpty == false else { return }
    let copy = WeakCollection<A>(objects: collection.storage.allObjects)
    main {
        copy.forEach(enumerate)
    }
}
fileprivate let checkLocksInterval: TimeInterval = 10

@objcMembers
public class EllipseManager: NSObject {
    
    @objc
    public static let shared = EllipseManager()
    /// Actual list of avaliable locks
    public var locks: [Ellipse] {
        return _locks.filter({ (ellipse) -> Bool in
            switch ellipse.connection {
            case .failed(let error) where error.isEllipseTimeout:
                return false
            default:
                return true
            }
        })
    }
    
    /// Server side APIs
    public var api: NetworkAPI
    public var restoringStrategy: Ellipse.RestoringStrategy = .reconnect
    public var cashingStrategy: Ellipse.CachingStrategy = .default
    
    /// Check BLE status
    public var isOn: Bool {
        return manager.state == .poweredOn
    }
    
    /// Default challenge key
    public static var secret: String? {
        set {
            sharedChallengeKey = newValue
        }
        get {
            return sharedChallengeKey
        }
    }
    fileprivate var manager: CBCentralManager!
    fileprivate var handlers = WeakCollection<EllipseManagerDelegate>()
    fileprivate let queue = DispatchQueue.global(qos: .default)
    fileprivate let timerQueue = DispatchQueue(label: "BLE.EllipseManager.Queue.Timer", qos: .background)
    fileprivate var locksTimer: DispatchSourceTimer?
    fileprivate(set) var _locks: [Ellipse] = []
    fileprivate let operationQueue = OperationQueue()
    
    override init() {
        operationQueue.isSuspended = true
        api = Session()
        super.init()
        
        var options: [String: Any]? = nil
        if let arr = Bundle.main.infoDictionary?["UIBackgroundModes"] as? [String], arr.contains("bluetooth-central") {
            options = [CBCentralManagerOptionShowPowerAlertKey: true,
                       CBCentralManagerOptionRestoreIdentifierKey: "BLE.EllipseManager"]
        }
        manager = CBCentralManager(delegate: self, queue: queue, options: options)
        Key.clean()
    }
    
    /// Start scan for locks
    ///
    /// - Parameter handler: EllipseManagerDelegate protocol confirmed property to handle manager events
    public func scan(with handler: EllipseManagerDelegate? = nil) {
        if let handler = handler {
            subscribe(handler: handler)
        }
        func scan() {
            self.manager.scanForPeripherals(withServices: Ellipse.Service.all, options: nil)
            timerQueue.async(execute: startTimer)
        }
        operationQueue.addOperation(scan)
        checkLocks(true)
    }
    
    /// Subscribe to EllipseManager events
    ///
    /// - Parameter handler: EllipseManagerDelegate protocol confirmed property to handle manager events
    public func subscribe(handler: EllipseManagerDelegate) {
        if locks.isEmpty == false {
            main {
                handler.manager(self, didUpdateLocks: self.locks, delete: [])
            }
        }
        handlers.insert(handler)
    }
    
    /// Stop scan for locks
    public func stopScan() {
        manager.stopScan()
//        invalidateTimer()
    }
    
    public func disconnect(ellipse: Ellipse) {
        ellipse.connection = .unpaired
        manager.cancelPeripheralConnection(ellipse.peripheral)
    }
    
    public func clean() {
        locks.forEach(disconnect(ellipse: ))
        stopScan()
        EllipseManager.secret = nil
    }
    
    fileprivate func ellipse(for peripheral: CBPeripheral) -> Ellipse? {
        return _locks.filter({$0.peripheral.macId == peripheral.macId}).first
    }
    
    fileprivate func startTimer() {
        invalidateTimer()
        locksTimer = DispatchSource.makeTimerSource(queue: timerQueue)
        locksTimer?.schedule(deadline: .now() + checkLocksInterval, repeating: checkLocksInterval)
        locksTimer?.setEventHandler(handler: { [weak self] in
            if let s = self?.restoringStrategy, s == .disconnect {
                self?.checkLocks(false)
                self?.invalidateTimer()
            } else {
                self?.checkLocks(true)
            }
        })
        locksTimer?.resume()
    }
    
    fileprivate func invalidateTimer() {
        checkLocks(true)
        if let timer = locksTimer, !timer.isCancelled {
            timer.cancel()
        }
        locksTimer = nil
    }
    
    fileprivate func checkLocks(_ connect: Bool) {
        guard self.manager.state == .poweredOn else { return }
        let action: (Ellipse) -> () = { e in
            if connect {
                e.connect()
            } else {
                e.disconnect()
            }
        }
        _locks.filter({ ellipse in
            switch ellipse.connection {
            case .reconnecting:
                return true
            case .failed(let error) where error.isEllipseTimeout:
                return true
            default:
                return false
            }
        }).forEach(action)
    }
    
    fileprivate func disconnectLocks() {
        _locks.forEach { (ellipse) in
            switch ellipse.peripheral.state {
            case .disconnected:
                ellipse.connection = .reconnecting
            default:
                break
            }
        }
        safe(handlers) {$0.manager(self, didUpdateLocks: [], delete: self._locks)}
    }
    
    @discardableResult fileprivate func addLocks(peripherals: [CBPeripheral]) -> [Ellipse] {
        let ellipses = peripherals.filter({$0.isEllboot == false}).compactMap(Ellipse.init)
        ellipses.forEach { (ellipse) in
            ellipse.connectClosure = { [unowned self, unowned ellipse] in
                switch ellipse.connection {
                case .connecting:
                    return
                default:
                    self.manager.connect(ellipse.peripheral, options: nil)
                }                
            }
            ellipse.disconnectClosure = { [unowned self, unowned ellipse] in
                self.disconnect(ellipse: ellipse)
            }
            ellipse.onTimeout = { [unowned self, unowned ellipse] in
                safe(self.handlers) {$0.manager(self, didUpdateLocks: [], delete: [ellipse])}
            }
            ellipse.sign = { [unowned self, unowned ellipse] closure in
                self.api.sign(lockWith: ellipse.macId, completion: { (result) in
                    switch result {
                    case .success((let signedMessage, let publicKey)):
                        closure(signedMessage, publicKey)
                    case .failure(let error):
                        ellipse.connection = .failed(error)
                    }
                })
            }
        }
        _locks += ellipses
        if ellipses.isEmpty == false {
            safe(handlers) {$0.manager(self, didUpdateLocks: ellipses, delete: [])}
        }
        return ellipses
    }
}

extension EllipseManager: CBCentralManagerDelegate {
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            operationQueue.isSuspended = false
        } else {
            disconnectLocks()
            invalidateTimer()
        }
        locks.forEach{print($0.peripheral.state.string)}
        safe(handlers) { (delegate) in
            delegate.manager(self, didUpdateConnectionState: self.manager.state == .poweredOn)
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        if let exists = ellipse(for: peripheral) {
            switch exists.connection {
            case .reconnecting:
                exists.connect()
            case .updating where peripheral.isEllboot == false:
                exists.peripheral = peripheral
                exists.connect()
            default:
                break
            }
        } else {
            addLocks(peripherals: [peripheral])
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        guard let ellipse = ellipse(for: peripheral) else { return }
        switch ellipse.connection {
        case .failed(let error) where error.isEllipseTimeout:
            disconnect(ellipse: ellipse)
            safe(handlers) {$0.manager(self, didUpdateLocks: [ellipse], delete: [])}
        case .flashingLED:
            ellipse.peripheral.discoverServices([Ellipse.Service.hardware.uuid])
        case .manageCapTouch:
            ellipse.peripheral.discoverServices([Ellipse.Service.configuration.uuid])
        default:
            ellipse.connection = .connecting
//            ellipse.connection = .paired
//            ellipse.peripheral.discoverServices(Ellipse.services)
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        if let ellipse = ellipse(for: peripheral), let error = error {
            ellipse.connection = .failed(error)
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        guard let ellipse = ellipse(for: peripheral) else { return }
        switch ellipse.connection {
        case .paired:
            ellipse.connection = .reconnecting
        case .updating:
            scan()
        case .restored where restoringStrategy == .reconnect:
            ellipse.connect()
        case .unpaired:
            return
        default:
            ellipse.connection = .unpaired
        }
    }
    
    public func centralManager(_ central: CBCentralManager, willRestoreState dict: [String : Any]) {
        guard let pers = dict[CBCentralManagerRestoredStatePeripheralsKey] as? [CBPeripheral] else { return }
        let locks = addLocks(peripherals: pers)
        let connected = locks.filter({$0.peripheral.state == .connected})
        operationQueue.addOperation {
            connected.forEach({ ellipse in
                ellipse.restoreFromBackground()
//                ellipse.connection = .restored
//                self.manager.cancelPeripheralConnection(ellipse.peripheral)
            })
            safe(self.handlers) {$0.manager(self, didRestoreConnected: connected)}
        }
    }
}
