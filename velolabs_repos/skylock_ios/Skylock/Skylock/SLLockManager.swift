//
//  SLLockManger.swift
//  Ellipse
//
//  Created by Andre Green on 8/15/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import RestService
import KeychainSwift

enum SLLockManagerConnectionError: Error {
    case NotAuthorized
    case NoUser
    case MissingKeys
    case IncorrectKeys
    case InvalidSecurityStateOwner
    case NoRestToken
    case Default
    case invalidOffcet
    case invalidWriteLength
    case invalidParameter
    
    init?(value: UInt8) {
        switch value {
        case 129:
            self = .InvalidSecurityStateOwner
        case 131:
            self = .invalidOffcet
        case 132:
            self = .invalidWriteLength
        case 133:
            self = .invalidParameter
        default:
            return nil
        }
    }
}

class SLLockManager: NSObject, SEBLEInterfaceManagerDelegate {
    private enum SLLockManagerState {
        case FindCurrentLock
        case Connecting
        case ActiveSearch
        case UpdateFirmware
        case UpdateFirmwareComplete
    }
    
    private enum SLLockManagerSecurityPhase {
        case PublicKey
        case ChallengeKey
        case SignedMessage
        case Connected
    }
    
    private enum BLEService:String {
        case Security = "5E00"
        case Hardware = "5E40"
        case Configuration = "5E80"
        case Test = "5EC0"
        case Boot = "5D00"
    }
    
    private enum BLECharacteristic:String {
        case LED = "5E41"
        case Lock = "5E42"
        case HardwareInfo = "5E43"
        case Reserved = "5E44"
        case TxPower = "5E45"
        case Magnet = "5EC3"
        case Accelerometer = "5E46"
        case SignedMessage = "5E01"
        case PublicKey = "5E02"
        case ChallengeKey = "5E03"
        case ChallengeData = "5E04"
        case FirmwareVersion = "5D01"
        case WriteFirmware = "5D02"
        case WriteFirmwareNotification = "5D03"
        case FirmwareUpdateDone = "5D04"
        case ResetLock = "5E81"
        case SerialNumber = "5E83"
        case ButtonSequece = "5E84"
        case CommandStatus = "5E05"
        
        static let allValues = [
            LED,
            Lock,
            HardwareInfo,
            Reserved,
            TxPower,
            Magnet,
            Accelerometer,
            SignedMessage,
            PublicKey,
            ChallengeKey,
            ChallengeData,
            FirmwareVersion,
            WriteFirmware,
            WriteFirmwareNotification,
            FirmwareUpdateDone,
            ResetLock,
            SerialNumber,
            ButtonSequece,
            CommandStatus
        ]
    }
    
    static let sharedManager = SLLockManager()
    
    private let dbManager = SLDatabaseManager.shared()
    
    private var currentState: SLLockManagerState = .FindCurrentLock
    
    private var shallowlyConnectedLocks : Set<String> = []
    
    private var securityPhase: SLLockManagerSecurityPhase = .PublicKey
            
    private var firmware: [String] = []
    
    private var hardwareTimer:Timer?
    
    private var firmwareSize: Int = .max
    
    private let theftHandler = TheftHandler(
        sensitivity: Float(SLDatabaseManager.shared().getCurrentUser()?.theftSensitivity ?? 0.0)
    )
    
    private let crashHandler = CrashHandler()
    
    private var afterDisconnectLockClosure:(() -> ())?
    
    private var afterUserDisconnectLockClosure:(() -> ())?
    
    fileprivate let locksService = LocksService()
    
    lazy var bleManager: SEBLEInterfaceMangager = {
        let manager:SEBLEInterfaceMangager = SEBLEInterfaceMangager.sharedManager() as! SEBLEInterfaceMangager
        manager.delegate = self
        
        return manager
    }()
    
    // MARK: Public Methods
    func startBluetoothManager() {
        self.bleManager.powerOn()
        self.bleManager.setDeviceNamesToConnectTo(Set(self.namesToConntect()))
        self.bleManager.setDeviceNameFragmentsToConnect(self.namesToConntect())
        self.bleManager.setServiceToReadFrom(Set(self.servicesToSubscribe()))
        self.bleManager.setCharacteristicsToReadFrom(Set(self.characteristicsToRead()))
        self.bleManager.setCharacteristicsToReceiveNotificationsFrom(Set(self.characteristicsToNotify()))
        self.bleManager.setServicesToNotifyWhenTheyAreDiscoverd(Set(self.servicesToNotifyWhenFound()))
        self.bleManager.isInBackground = self.isInBackgroundState()
    }
    
    func isInBackgroundState() -> Bool {
        return UIApplication.shared.applicationState == .background;
    }
    
    func getCurrentLock() -> SLLock? {
        guard let locks:[SLLock] = self.dbManager.allLocks() as? [SLLock] else {
            return nil
        }
        
        return locks.filter({ $0.isCurrentLock && self.bleManager.hasConnectedPeripheral(withKey: $0.macId!) }).first
    }
    
    func disconnectFromCurrentLock(completion:(() -> ())?) {
        guard let lock = self.getCurrentLock() else {
            completion?()
            print("Error: could not disconnect from current lock. No current lock in database")
            return
        }
        
        lock.isCurrentLock = false
        lock.isConnecting = false
        lock.hasConnected = true
        self.dbManager.save(lock)
        
        self.afterUserDisconnectLockClosure = completion
        
        self.bleManager.disconnectFromPeripheral(withKey: lock.macId!)
    }
    
    func deleteAllNeverConnectedAndNotConnectingLocks() {
        guard let locks:[SLLock] = self.dbManager.allLocks() as? [SLLock] else {
            return
        }
        
        print("there are \(locks.count) locks")
        for lock in locks {
            if lock.isConnecting {
                // If the lock is currently conneting let's continue on our search.
                // This check is here primarily for clarity.
                continue
            } else if self.bleManager.hasConnectedPeripheral(withKey: lock.macId!) {
                // The lock is the currently connected lock. Let's continue our search
                continue
            } else if lock.hasConnected && !lock.isConnecting {
                // This is the case where the lock has previously connected but is
                // not currently connecting. We'll try to remove it from the
                // bluetooth managers not connected peripherals in case it has
                // been detected there.
                self.bleManager.removeNotConnectPeripheral(forKey: lock.macId!)
            } else if !lock.hasConnected && !lock.isConnecting {
                // In this case, the lock was detected during a scan, but was never
                // connected. We can get rid of these locks from the blue tooth manager
                // and the database.
                self.bleManager.removeNotConnectPeripheral(forKey: lock.macId!)
                self.dbManager.delete(lock, withCompletion: nil)
            } else {
                print(
                    "No cases were hit for lock: \(lock.displayName) "
                        + "durring deletion of never connected and not connecting locks"
                )
            }
        }
    }
    
    func readFirmwareDataForCurrentLock() {
        guard let macAddress = self.getCurrentLock()?.macId else {
            print("Error: could not read firmware. No current lock in database.")
            return
        }
        
        self.readFromLockWithMacAddress(
            macAddress: macAddress,
            service: .Configuration,
            characteristic: .FirmwareVersion
        )
    }
    
    func readSerialNumberForCurrentLock() {
        guard let macAddress = self.getCurrentLock()?.macId else {
            print("Error: could not read serial number. No current lock in database.")
            return
        }
        
        self.readFromLockWithMacAddress(
            macAddress: macAddress,
            service: .Configuration,
            characteristic: .SerialNumber
        )
    }
    
    func checkCurrentLockOpenOrClosed() {
        guard let lock = self.getCurrentLock() else {
            print("Error: could not check if lock is open or closed. No current lock in database.")
            return
        }
        
        self.readFromLockWithMacAddress(
            macAddress: lock.macId!,
            service: .Hardware,
            characteristic: .Lock
        )
    }
    
    func toggleLockOpenedClosedShouldLock(shouldLock: Bool) {
        guard let lock = self.getCurrentLock() else {
            print("Error: could not open or close lock. No current lock in databse for user.")
            return
        }
        
        let value:UInt8 = shouldLock ? 0x01 : 0x00
        let data = Data(bytes: [value])
        self.writeToLockWithMacAddress(
            macAddress: lock.macId!,
            service: .Hardware,
            characteristic: .Lock,
            data: data
        )
    }
    
    func allPreviouslyConnectedLocksForCurrentUser() -> [SLLock] {
        guard let user = self.dbManager.getCurrentUser() else { return [] }
        print("current user: \(user.fullName) type: \(user.userType)")
        guard let locks:[SLLock] = self.dbManager.locksForCurrentUser() as? [SLLock] else {
            return [SLLock]()
        }
        var unconnectedLocks = [SLLock]()
        for lock in locks {
            if !self.bleManager.hasConnectedPeripheral(withKey: lock.macId!) {
                unconnectedLocks.append(lock)
            }
        }
        
        return unconnectedLocks
    }
    
    func allLocksForCurrentUser() -> [SLLock] {
        guard let locks:[SLLock] = self.dbManager.locksForCurrentUser() as? [SLLock] else {
            return []
        }
        
        return locks
    }
    
    func locksInActiveSearch() -> [SLLock] {
        guard let locks:[SLLock] = self.dbManager.allLocks() as? [SLLock] else {
            return [SLLock]()
        }
        
        guard let user = self.dbManager.getCurrentUser() else {
            return [SLLock]()
        }
        
        var activeLocks:[SLLock] = []
        for lock in locks where self.bleManager.hasNonConnectedPeripheral(withKey: lock.macId) {
            if let lockUser = lock.user {
                if lockUser.userId == user.userId {
                    activeLocks.append(lock)
                }
            } else {
                activeLocks.append(lock)
            }
        }
        
        return activeLocks
    }
    
    func availableUnconnectedLocks() -> [SLLock] {()
        var availableLocks = [SLLock]()
        guard let locks = self.dbManager.locksForCurrentUser() as? [SLLock] else {
            return availableLocks
        }
        
        guard let user = self.dbManager.getCurrentLockForCurrentUser() else {
            return availableLocks
        }
        
        for lock in locks {
            if !self.bleManager.hasConnectedPeripheral(withKey: lock.macId!) && lock.user == user {
                availableLocks.append(lock)
            }
        }
        
        return availableLocks
    }
    
    func writeTouchPadButtonPushes(touches: [UInt8]) {
        guard let lock = self.getCurrentLock() else {
            print("Cannot write button pushes. There is no current lock")
            return
        }
        
        let maxTouches = 16
        if touches.count > maxTouches {
            print("Error: Attempting to write \(touches.count) to lock. Only \(maxTouches) are allowed")
            return
        }
        
        var touchesToWrite:[UInt8] = []
        touchesToWrite += touches
        while touchesToWrite.count < maxTouches {
            touchesToWrite.append(0x00)
        }
        
        let data = Data(bytes: touchesToWrite)
        self.writeToLockWithMacAddress(
            macAddress: lock.macId!,
            service: .Configuration,
            characteristic: .ButtonSequece,
            data: data
        )
    }
    
    func hasLocksForCurrentUser() -> Bool {
        guard let locks = self.dbManager.locksForCurrentUser() as? [SLLock] else {
            return false
        }
        
        return locks.count > 0
    }
    
    func deleteLockFromCurrentUserAccountWithMacAddress(macAddress: String) {
        guard let lock = self.dbManager.getLockWithMacId(macAddress) else {
            print("Error: could not delete lock: \(macAddress). No lock in database with that address")
            return
        }
        
        self.deleteLockFromServerWithMacAddress(macAddress: macAddress) { (success) in
            if success {
                if self.bleManager.hasConnectedPeripheral(withKey: macAddress) {
                    lock.isSetForDeletion = true
                    self.dbManager.save(lock)
                    
                    let value:UInt8 = 0xBC
                    let data = Data(bytes: [value])
                    
                    self.startBleScan()
                    
                    self.writeToLockWithMacAddress(
                        macAddress: macAddress,
                        service: .Configuration,
                        characteristic: .ResetLock,
                        data: data
                    )
                } else {
                    self.dbManager.delete(lock, withCompletion: nil)
                    self.bleManager.removePeripheral(forKey: macAddress)
                    self.bleManager.removeNotConnectPeripheral(forKey: macAddress)
                    
                    NotificationCenter.default.post(
                        name: Notification.Name(rawValue: kSLNotificationLockManagerDeletedLock),
                        object: macAddress
                    )
                }
            } else {
                // TODO: send notificaiton that the deletion was not successful
            }
        }
    }
    
    func isConnecedLock(with macId: String) ->Bool {
        return bleManager.hasConnectedPeripheral(withKey: macId)
    }
    
    func factoryResetCurrentLock() {
        guard let macAddress = self.getCurrentLock()?.macId else {
            print("Error: could not reset current lock. No current lock in databsase")
            return
        }
        
        let value:UInt8 = 0xBB
        let data = Data(bytes: [value])
        
        self.stopGettingHardwareInfo()
        
        self.writeToLockWithMacAddress(
            macAddress: macAddress,
            service: .Configuration,
            characteristic: .ResetLock,
            data: data
        )
    }
    
    func updateFirmwareForCurrentLock(completion: @escaping (Error) -> ()) {
        self.getFirmwareFromServer { error in
            if let err = error {
                completion(err)
            } else {
                guard let lock = self.getCurrentLock() else {
                    print("Error: could not update firmware for current lock. No current lock in databsase")
                    return
                }
                
                self.currentState = .UpdateFirmware
                if let macId = lock.macId {
                    self.writeFirmwareForLockWithMacAddress(macAddress: macId)
                }
            }
        }
    }
    
    func isBlePoweredOn() -> Bool {
        return self.bleManager.isPowerOn()
    }
    
    func isInActiveSearch() -> Bool {
        return self.currentState == .ActiveSearch
    }
    
    func startActiveSearch() {
        self.currentState = .ActiveSearch
        self.startBleScan()
    }
    
    func endActiveSearch() {
        if self.getCurrentLock() == nil {
            self.currentState = .FindCurrentLock
        }
        
        self.bleManager.stopScan()
        let locks = self.locksInActiveSearch()
        for lock in locks where !lock.isConnecting {
            self.bleManager.removeNotConnectPeripheral(forKey: lock.macId!)
        }
    }
    
    func updateBackground(state: Bool) {
        self.bleManager.isInBackground = state
    }
    
    func connectToLockWithMacAddress(macAddress: String) {
        print("Attempting to connect to lock with address: \(macAddress)")
        guard let lock = self.dbManager.getLockWithMacId(macAddress) else {
            print("Error: Could not connect to lock \(macAddress). It is not in database.")
            return
        }
        
        if self.currentState == .UpdateFirmwareComplete {
            NotificationCenter.default.post(
                name: NSNotification.Name(rawValue: kSLNotificationLockManagerEndedFirmwareUpdate),
                object: macAddress
            )
        }
        
        self.securityPhase = lock.isInFactoryMode ? .PublicKey : .SignedMessage
        
        if self.getCurrentLock() == nil {
            // There is no current lock. Let's just connect the lock that
            // the user has asked to connect.
            self.connectToLockWithMacAddressHelper(macAddress: macAddress)
            self.endActiveSearch()
            self.deleteAllNeverConnectedAndNotConnectingLocks()
        } else {
            // If there is a current lock, we'll need to disconnect from it before
            // connecting the new lock.
            self.afterDisconnectLockClosure = { [unowned self] in
                self.connectToLockWithMacAddressHelper(macAddress: macAddress)
            }
            self.disconnectFromCurrentLock(completion: nil)
        }
        
        self.currentState = .FindCurrentLock
    }
    
    func deleteLockWithMacAddress(macAddress: String) {
        guard let lock = self.dbManager.getLockWithMacId(macAddress) else {
            print("Error: No matching lock with mac address \(macAddress) to delete")
            return
        }
        
        if self.bleManager.hasConnectedPeripheral(withKey: lock.macId!) {
            // Lock is the current connected lock
            lock.isSetForDeletion = true
            self.dbManager.save(lock)
            self.bleManager.removePeripheral(forKey: macAddress)
            return
        }
        
        // Lock is not current lock. Let's check to see if the user has connected to the lock
        guard let locks = self.dbManager.locksForCurrentUser() as? [SLLock] else {
            print("Error: no locks for user matches mac address: \(macAddress)")
            return
        }
        
        for dbLock in locks {
            if dbLock.macId == macAddress {
                self.dbManager.delete(dbLock, withCompletion: { (success: Bool) in
                    if success {
                        self.removeKeyChainItemsForLock(macAddress: macAddress)
                        NotificationCenter.default.post(
                            name: NSNotification.Name(rawValue: kSLNotificationLockManagerDeletedLock),
                            object: macAddress
                        )
                    } else {
                        // TODO: handle case where the lock deletion is a failure.
                        print("Error: could not delete lock with mac address: \(macAddress). Something went wrong")
                    }
                })
                
                break
            }
        }
        
        // TODO: handle case where the lock deletion is a failure.
        print(
            "Error: could not delete lock with mac address: \(macAddress). "
                + "There is no matching lock in the database."
        )
    }
    
    func flashLEDsForCurrentLock() {
        guard let macAddress = self.getCurrentLock()?.macId else {
            print("Error: could not flash LEDs for current user. No current lock in database.")
            return
        }
        
        self.flashLEDsForLockMacAddress(macAddress: macAddress)
    }
    
    func removeAllUnconnectedLocks() {
        guard let locks = self.dbManager.allLocks() as? [SLLock] else {
            print("Error: could not retrieve current locks for user.")
            return
        }
        
        
        for lock in locks where !self.bleManager.hasConnectedPeripheral(withKey: lock.macId) {
            self.bleManager.removeNotConnectPeripheral(forKey: lock.macId)
        }
    }
    
    func revoke(lock: SLLock, unshareFrom: Int32, completion: ((Bool) -> ())?) {
        func removeLock() {
            if lock.isShared {
                guard let macAddress = lock.macId else { return }
                self.bleManager.disconnectFromPeripheral(withKey: macAddress)
                self.bleManager.removePeripheral(forKey: macAddress)
                self.bleManager.removeNotConnectPeripheral(forKey: macAddress)
                self.removeKeyChainItemsForLock(macAddress: macAddress)
                self.dbManager.delete(lock, withCompletion: nil)
                NotificationCenter.default.post(
                    name: Notification.Name(rawValue: kSLNotificationLockManagerDeletedLock),
                    object: macAddress
                )
                
            }
        }
        guard lock.shareId != 0 else {
            completion?(false)
            return
        }
        Oval.locks.revoke(sharing: lock.shareId, fromUser: unshareFrom, success: {
            removeLock()
            completion?(true)
        }, fail: { _ in
            completion?(false)
        })
    }
    
    func removeAllLocks() {
        guard let locks = dbManager.allLocks() as? [SLLock] else { return }
        for lock in locks {
            self.bleManager.removePeripheral(forKey: lock.macId!)
            self.bleManager.removeNotConnectPeripheral(forKey: lock.macId!)
            self.removeKeyChainItemsForLock(macAddress: lock.macId!)
            self.dbManager.delete(lock, withCompletion: nil)
        }
    }
    
    func clear(allLocks: [SLLock], existed: [String]) {
        let all = allLocks.flatMap({ $0.macId })
        let remove = Set(all).subtracting(Set(existed))
        let locks = allLocks.filter({ remove.contains($0.macId!) })
        for lock in locks {
            let macAddress = lock.macId!
            bleManager.disconnectFromPeripheral(withKey: macAddress)
            self.bleManager.removePeripheral(forKey: macAddress)
            self.bleManager.removeNotConnectPeripheral(forKey: macAddress)
            self.removeKeyChainItemsForLock(macAddress: macAddress)
            self.dbManager.delete(lock, withCompletion: nil)
            NotificationCenter.default.post(
                name: Notification.Name(rawValue: kSLNotificationLockManagerDeletedLock),
                object: macAddress
            )
        }
    }
    
    func armLock(macId: String) {
        let value: UInt8 = 0xFF
        let data = Data(bytes: [value])
        self.writeToLockWithMacAddress(macAddress: macId, service: .Hardware, characteristic: .Lock, data: data)
    }
    
    func disarmLock(macId: String) {
        let value: UInt8 = 0x00
        let data = Data(bytes: [value])
        self.writeToLockWithMacAddress(macAddress: macId, service: .Hardware, characteristic: .Lock, data: data)
    }
    
    func updateTheftSensitivity() {
        guard let user = self.dbManager.getCurrentUser() else {
            return
        }
        
        self.theftHandler.set(sensitivity: Float(user.theftSensitivity))
    }
    
    // MARK: Private Methods
    private func namesToConntect() -> [String] {
        return [
            "ellipse",
            "skylock"
        ]
    }
    
    private func servicesToSubscribe() -> [String] {
        return [
            self.serviceUUID(service: .Security),
            self.serviceUUID(service: .Hardware),
            self.serviceUUID(service: .Configuration),
            self.serviceUUID(service: .Test),
            self.serviceUUID(service: .Boot)
        ]
    }
    
    private func servicesToNotifyWhenFound() -> [String] {
        return [
            self.serviceUUID(service: .Security),
            self.serviceUUID(service: .Boot)
        ]
    }
    
    private func characteristicsToRead() -> [String] {
        var charsToRead = [String]()
        for uuid in BLECharacteristic.allValues {
            charsToRead.append(self.characteristicUUID(characteristic: uuid))
        }
        
        return charsToRead
    }
    
    private func characteristicsToNotify() -> [String] {
        return [
            self.characteristicUUID(characteristic: .Magnet),
            self.characteristicUUID(characteristic: .Accelerometer),
            self.characteristicUUID(characteristic: .CommandStatus),
            self.characteristicUUID(characteristic: .Lock),
            self.characteristicUUID(characteristic: .HardwareInfo)
        ]
    }
    
    private func serviceUUID(service: BLEService) -> String {
        return self.uuidWithSegment(segment: service.rawValue)
    }
    
    private func characteristicUUID(characteristic: BLECharacteristic) -> String {
        return self.uuidWithSegment(segment: characteristic.rawValue)
    }
    
    private func uuidWithSegment(segment: String) -> String {
        return "D399" + segment + "-FA57-11E4-AE59-0002A5D5C51B"
    }
    
    private func writeToLockWithMacAddress(
        macAddress: String,
        service: BLEService,
        characteristic: BLECharacteristic,
        data: Data
        )
    {
        self.bleManager.writeToPeripheral(
            withKey: macAddress,
            serviceUUID: self.serviceUUID(service: service),
            characteristicUUID: self.characteristicUUID(characteristic: characteristic),
            data: data
        )
    }
    
    private func readFromLockWithMacAddress(
        macAddress: String,
        service: BLEService,
        characteristic: BLECharacteristic
        )
    {
        self.bleManager.readValueForPeripheral(
            withKey: macAddress,
            forServiceUUID: self.serviceUUID(service: service),
            andCharacteristicUUID: self.characteristicUUID(characteristic: characteristic)
        )
    }
    
    private func startBleScan() {
        if self.bleManager.delegate == nil {
            self.bleManager.delegate = self
        }
        
        self.bleManager.startScan()
    }
    
    // MARK: Private methods and private helper methods
    private func connectToLockWithMacAddressHelper(macAddress: String) {
        print("Attempting to connect to lock with address: \(macAddress). Lock manager state: \(self.currentState)")
        
        guard let lock = self.dbManager.getLockWithMacId(macAddress) else {
            print("Error: connecting to lock with mac address \(macAddress). No lock with that address in db")
            return
        }
        
        if self.currentState == .UpdateFirmware {
            print("In update firmware mode. Will attept to connect to lock with address: \(macAddress)")
            lock.isConnecting = true;
            self.dbManager.save(lock)
            self.bleManager.connectToPeripheral(withKey: macAddress)
            return
        }
        
        self.currentState = .Connecting
        
        print("Attempting to connect to lock: \(lock.description)")
        
        lock.isConnecting = true
        self.dbManager.save(lock)
        
        func connect() {
            self.bleManager.connectToPeripheral(withKey: macAddress)
            NotificationCenter.default.post(
                name: NSNotification.Name(rawValue: kSLNotificationLockManagerStartedConnectingLock),
                object: nil
            )
        }
        
        self.securityPhase = lock.isInFactoryMode ? .PublicKey : .SignedMessage
        
        locksService.signLock(with: macAddress, completion: { (_, _) in
            if lock.hasConnected == false {
                if self.bleManager.notConnectedPeripheral(forKey: macAddress) == nil {
                    print(
                        "Error: connecting lock. No not connected peripheral " +
                        "in ble manager with key: \(macAddress)."
                    )
                    return
                }
                
                connect()
            } else {
                connect()
            }
            
            
        }, fail: { error in
            var info:[String: Any?] = [
                "lock": self.dbManager.getLockWithMacId(macAddress),
                "error": error,
                "message": self.textForConnectionError(error: .NotAuthorized)
            ]
            if let error = error as? SLLockManagerConnectionError, error == .NotAuthorized {
                info["message"] = self.textForConnectionError(error: .NotAuthorized)
                info["header"] = "This Ellipse belongs to another user".localized()
            }
            NotificationCenter.default.post(
                name: Notification.Name(rawValue: kSLNotificationLockManagerErrorConnectingLock),
                object: info
            )
        })
    }
    
    private func flashLEDsForLockMacAddress(macAddress: String) {
        func stopBlinking() {
            writeToLockWithMacAddress(
                macAddress: macAddress,
                service: .Hardware,
                characteristic: .LED,
                data:  Data(bytes: [0x00])
            )
            
            if (self.shallowlyConnectedLocks.contains(macAddress)){
                NotificationCenter.default.post(
                    name: NSNotification.Name(rawValue: kSLNotificationLockLedTurnedOff),
                    object: macAddress
                )
                let peripheral = self.bleManager.connectedPeripheral(forKey: macAddress)
                self.bleManager.setNotConnectedPeripheral(peripheral, forKey: macAddress)
                self.bleManager.disconnectFromPeripheral(withKey: macAddress)
            }
        }
        let blinking = Data(bytes: [0xCF, 0xCF, 0x00, 0x01, 0x00])
        
        writeToLockWithMacAddress(
            macAddress: macAddress,
            service: .Hardware,
            characteristic: .LED,
            data:  blinking
        )
        
        Timer.after(6.second, stopBlinking)
    }
    
    private func stopGettingHardwareInfo() {
        self.hardwareTimer?.invalidate()
        self.hardwareTimer = nil
    }
    
    private func startGettingHardwareInfo() {
        self.hardwareTimer = Timer.scheduledTimer(
            timeInterval: 2.0,
            target: self,
            selector: #selector(getHardwareInfo(timer:)),
            userInfo: nil,
            repeats: true
        )
    }
    
    @objc private func getHardwareInfo(timer: Timer) {
        print("Hardware timer is firing.")
        guard let macAddress = self.getCurrentLock()?.macId else {
            print("Error: getting hardware data. No current lock or no mac address for current lock")
            return
        }
        
        self.readFromLockWithMacAddress(macAddress: macAddress, service: .Hardware, characteristic: .HardwareInfo)
    }
    
    private func getFirmwareFromServer(completion: ((Error?) -> ())?) {
        Oval.locks.firmvare(success: { [weak self] (firmware) in
            self?.firmware = firmware
            self?.firmwareSize = firmware.count
            completion?(nil)
        }, fail: { error in completion?(error) })
    }
    
    private func deleteLockFromServerWithMacAddress(macAddress: String, completion: ((_ success: Bool) -> ())?) {
        Oval.locks.delete(lock: macAddress, success: {
            completion?(true)
        }, fail: { error in
            completion?(false)
        })
    }
    
    private func writeFirmwareForLockWithMacAddress(macAddress: String) {        
        print("Writing firmware for lock: \(macAddress). There are \(self.firmware.count) items left to write.")
        if self.firmware.isEmpty {
            self.currentState = .UpdateFirmwareComplete
            let value: UInt8 = 0x00
            let data = Data(bytes: [value])
            self.writeToLockWithMacAddress(
                macAddress: macAddress,
                service: .Configuration,
                characteristic: .FirmwareVersion,
                data: data
            )
        } else {
            let percentageComplete:Double = 1.0 - Double(self.firmware.count)/Double(firmwareSize)
            NotificationCenter.default.post(
                name: NSNotification.Name(rawValue: kSLNotificationLockManagerFirmwareUpdateState),
                object: NSNumber(value: percentageComplete)
            )
            
            if let data = self.firmware.removeFirst().bytesString() {
                self.writeToLockWithMacAddress(
                    macAddress: macAddress,
                    service: .Configuration,
                    characteristic: .WriteFirmware,
                    data: data
                )
            }
        }
    }
    
    private func setTxPowerForLockWithMacAddress(macAddress: String) {
        let value: UInt8 = Locale.current.region.isEurope ? 0x00 : 0x04
        let data = Data(bytes: [value])
        self.writeToLockWithMacAddress(
            macAddress: macAddress,
            service: .Hardware,
            characteristic: .TxPower,
            data: data
        )
    }
    
    private func challengeKey(forLockOwner macId: String) -> String? {
        guard let userId = self.dbManager.ownerOfLock(withMacId: macId)?.userId ?? dbManager.getCurrentUser()?.userId else {
            print("Error: could not create challege key. No current user")
            return nil
        }
        
        let cryptoHandler = SLCryptoHandler()
        guard var challengeKey = cryptoHandler.md5String(from: String(userId)) else {
            print("Error: could not create md5 hash of userId")
            return nil
        }
        

        challengeKey = challengeKey.lowercased()
        while challengeKey.characters.count < 64 {
            challengeKey.append("f")
        }
        
        print("Challenge key: \(challengeKey)")
        return challengeKey
    }
    
    private func textForConnectionError(error: SLLockManagerConnectionError) -> String {
        // TODO: This methods should be moved to another class. Having it in the lock manager
        // is a bit cumbersome and is bad encapsulation.
        let text:String
        switch error {
        case .NotAuthorized:
            text = NSLocalizedString(
                "Sorry. This Ellipse belongs to another user. We can't add it to your account. " +
                "You might need to reset the Ellipse. If you need help with this, please see the help section.",
                comment: ""
            )
        case .InvalidSecurityStateOwner:
            text = NSLocalizedString(
                "Sorry. Although you are the owner of this Ellipse, it will need to be reset before you " +
                "can connect to it. If you need help with this, please see the help section.",
                comment: ""
            )
        case .Default:
            text = NSLocalizedString("Sorry. There was an error connecting to your Ellipse", comment: "")
        default:
            print("Undefined error: \(error)")
            text = "It appears that something went wrong."
        }
        
        return text
    }
    
    // MARK: Update handlers
    private func handleCommandStatusUpdateForLockMacAddress(macAddress: String, data: NSData) {
        guard let lock = self.dbManager.getLockWithMacId(macAddress) else {
            print(
                "Error: Could not handle command status update for lock with mac address: \(macAddress). " +
                "No lock found in database with matching mac address"
            )
            return
        }
        
        let bytes:[UInt8] = data.UInt8Array()
        guard let value:UInt8 = bytes.first else {
            print("Error reading security state data. The updated data has zero bytes")
            return
        }
        
        print("Command status updated with value: \(value)")
        
        if value == 0 {
            // This is the case for a successful write to most characteristics. All possible
            // characteristic writes should be handled here.
            if self.securityPhase == .PublicKey {
                // Public Key has been written succesfully
                self.securityPhase = .ChallengeKey
                self.handleChallengeKeyConnectionPhaseForMacAddress(macAddress: macAddress)
            } else if self.securityPhase == .ChallengeKey {
                // Challege key has been written succesfully
                self.securityPhase = .SignedMessage
                self.handleSignedMessageConnectionPhaseForMacAddress(macAddress: macAddress)
            } else if securityPhase == .Connected {
                // The security between the lock and the phone has been established.
                // We can get hardware updates to this section of code. For example,
                // the lock/unlock state in the command status will be updated here.
                if self.currentState == .UpdateFirmware {
                    self.writeFirmwareForLockWithMacAddress(macAddress: macAddress)
                } else if self.currentState == .UpdateFirmwareComplete {
                    self.handleFirmwareUpdateCompletion(macAddress: macAddress, success: true)
                } else {
                    self.checkCurrentLockOpenOrClosed()
                }
            }
        } else if value == 1 || value == 2 {
            // Wrote signed message successfully. Security state is owner request
            // We now need to get the challege data from the lock.
            self.readFromLockWithMacAddress(macAddress: macAddress, service: .Security, characteristic: .ChallengeData)
        } else if value == 4 || value == 3 {
            // Challege data written successfully. Now owner verified. The owner is now
            // "paired" to the lock.
            guard let user = self.dbManager.getCurrentUser() else {
                print(
                    "Error: Could not handle command status update for lock with mac address: \(macAddress). " +
                    "No user found in database"
                )
                return
            }
            
            if lock.isInFactoryMode {
                lock.switchNameToProvisioned()
            }
            
            lock.isCurrentLock = true
            lock.hasConnected = true
            lock.isConnecting = false
            lock.lastConnected = NSDate()
            lock.isShared = value == 3
            lock.user = user
            if lock.owner == nil {
                lock.owner = user
            }
            
            self.dbManager.save(lock)
            
            self.securityPhase = .Connected
            self.bleManager.stopScan()
            self.bleManager.removeNotConnectPeripherals()
            
            self.checkCurrentLockOpenOrClosed()
            self.flashLEDsForLockMacAddress(macAddress: macAddress)
            self.startGettingHardwareInfo()
            self.setTxPowerForLockWithMacAddress(macAddress: macAddress)
            if user.isAutoLockOn {
                self.armLock(macId: macAddress)
            }
            
            self.removeAllUnconnectedLocks()
            
            self.bleManager.setCharacteristicUUIDToNotify(
                self.characteristicUUID(characteristic: .HardwareInfo),
                forPeripheralWithKey: macAddress
            )
            
            NotificationCenter.default.post(
                name: NSNotification.Name(rawValue: kSLNotificationLockPaired),
                object: lock
            )
        } else if value == 129 || value == 131 || value == 132 || value == 133 {
            // If the value is 129, it signals that "Access denied because of invalid security state."
            // If there is no signed message, or no public key this could occur. To fix this we can
            // refetch the keys from the server and try writing this again.
            locksService.cleanKeys(forLockWith: macAddress)
            let err = SLLockManagerConnectionError(value: value)
            print("Error: command status got \(err)")
            
            locksService.locks(updateCache: true, completion: { (locks, isServer, error) in
                guard isServer else { return }
                var info: [String: Any] = ["lock": lock]
                if locks.flatMap({ $0.macId }).contains(macAddress) {
                    info["error"] = err ?? SLLockManagerConnectionError.InvalidSecurityStateOwner
                    info["message"] = self.textForConnectionError(error: .InvalidSecurityStateOwner)
                } else {
                    info["error"] = error ?? SLLockManagerConnectionError.NotAuthorized
                    info["message"] = self.textForConnectionError(error: .NotAuthorized)
                    info["header"] = "This Ellipse belongs to another user".localized()
                }
                
                NotificationCenter.default.post(
                    name: NSNotification.Name(rawValue: kSLNotificationLockManagerErrorConnectingLock),
                    object: info
                )
            })
        } else if value == 130 {
            // If there is a lock/unlock error, the error is being sent to the sercurity service.
            // Although this is not very good encapsulation (the lock/unlock characteristic is
            // under the hardware characteristic), this is the way it was setup in the firmware.
            // As a result, it is necassary to deal with it here.
            // TODO: handle this error in UI
            print("Error: command status updated that the lock did not open/close correctly")
            NotificationCenter.default.post(
                name: NSNotification.Name(rawValue: kSLNotificationLockPositionMiddle),
                object: lock
            )
        } else if value == 255 {
            print("command status can't be updated. Write in progress")
        }
    }
    
    private func handleHardwareServiceForMacAddress(macAddress: String, data: NSData) {
        if data.length != 13 {
            print("Error: handling hardware service. Data is wrong number of bytes: \(data.length). Should be 13")
            return
        }
        
        guard let lock = self.dbManager.getLockWithMacId(macAddress) else {
            print("Error: handling hardware service. No lock in database with address: \(macAddress)")
            return
        }
        
        let values:[Int8] = data.Int8Array()
        var batteryVoltage:Int16 = Int16(values[0])
        batteryVoltage += Int16(Int32(values[1]) << CHAR_BIT)
        
        lock.batteryVoltage = batteryVoltage
        lock.temperature = values[2]
        lock.rssiStrength = Float(values[3])
        
        var isLocked:Int8 = values[4]
        var sendLockUpdate = false
        if isLocked == 0 && lock.isLocked {
            lock.isLocked = false
            sendLockUpdate = true
        } else if isLocked == 1 && !lock.isLocked {
            lock.isLocked = false
            sendLockUpdate = true
        }
        
        self.dbManager.save(lock)
        
        if (sendLockUpdate) {
            let data = NSData(bytes: &isLocked, length: MemoryLayout.size(ofValue: isLocked))
            self.handleLockStateForLockMacAddress(macAddress: macAddress, data: data)
        }
        
        NotificationCenter.default.post(
            name: NSNotification.Name(rawValue: kSLNotificationLockManagerUpdatedHardwareValues),
            object: lock.macId!
        )
        
        // TODO: move this somewhere more appropriate. I'm just hacking it in here for now
        guard let user = self.dbManager.getCurrentUser() else {
            print("Error no current user. Could not check auto lock/unlock")
            return
        }
        
        print("Lock rssi: \(lock.rssiStrength)")
        var value:UInt8?
        if lock.isLocked && user.isAutoUnlockOn && lock.rssiStrength! >= -70 {
            value = 0x00
        } else if !lock.isLocked && user.isAutoLockOn && lock.rssiStrength! <= -80 {
            value = 0x01
        }
        
        if let lockValue = value {
            let lockData = Data(bytes: [lockValue])
            self.writeToLockWithMacAddress(
                macAddress: macAddress,
                service: .Hardware,
                characteristic: .Lock,
                data: lockData
            )
        }
    }
    
    private func handleLockStateForLockMacAddress(macAddress: String, data: NSData) {
        let values:[UInt8] = data.UInt8Array()
        guard let value = values.first else {
            print("Error: in handling lock state. Data returned is empty")
            return
        }
        
        guard let position = SLLockPosition(rawValue: value) else {
            print("Error: in handling lock state. The value does not match a case in SLLockManagerLockPosition enum")
            return
        }
        
        guard let lock = self.dbManager.getLockWithMacId(macAddress) else {
            print("Error: could not update lock state. No lock with mac address: \(macAddress) in database")
            return
        }
        
        
        let notification:String
        var isLocked = false
        switch position {
        case .invalid:
            notification = kSLNotificationLockPositionInvalid
        case .locked:
            lock.lastLocked = NSDate()
            notification = kSLNotificationLockPositionLocked
            isLocked = true
        case .middle:
            notification = kSLNotificationLockPositionMiddle
        case .unlocked:
            notification = kSLNotificationLockPositionOpen
        }
        
        lock.isLocked = isLocked
        lock.lockPosition = Int16(position.rawValue)
        self.dbManager.save(lock)
        
        NotificationCenter.default.post(name: NSNotification.Name(rawValue: notification), object: lock)
    }
    
    private func handleAccelerometerForLockMacAddress(macAddress: String, data: NSData) {
        guard let user = self.dbManager.getCurrentUser() else {
            return
        }
        
        if !user.areCrashAlertsOn && !user.areTheftAlertsOn {
            return
        }
        
        var x:UInt16 = 0
        var y:UInt16 = 0
        var z:UInt16 = 0
        var xDev:UInt16 = 0
        var yDev:UInt16 = 0
        var zDev:UInt16 = 0
        
        let values:[UInt8] = data.UInt8Array()
        for i in 0..<values.count {
            switch i {
            case 0, 1:
                x += UInt16(values[i]) << UInt16(Int32((i % 2))*CHAR_BIT)
            case 2, 3:
                y += UInt16(values[i]) << UInt16(Int32((i % 2))*CHAR_BIT)
            case 4, 5:
                z += UInt16(values[i]) << UInt16(Int32((i % 2))*CHAR_BIT)
            case 6, 7:
                xDev += UInt16(values[i]) << UInt16(Int32((i % 2))*CHAR_BIT)
            case 8, 9:
                yDev += UInt16(values[i]) << UInt16(Int32((i % 2))*CHAR_BIT)
            case 10, 11:
                zDev += UInt16(values[i]) << UInt16(Int32((i % 2))*CHAR_BIT)
            default:
                continue
            }
        }
        
        let accValue = AccelerometerValue(
            x: Float(x),
            y: Float(y),
            z: Float(z),
            xDev: Float(xDev),
            yDev: Float(yDev),
            zDev: Float(zDev)
        )
        
        if user.areTheftAlertsOn {
            self.theftHandler.add(point: accValue)
            if self.theftHandler.shouldAlert() {
                let notificaitonManger = SLNotificationManager.sharedManager() as! SLNotificationManager
                notificaitonManger.sendTheftAlertForLock(withMacId: macAddress, withAccInfo: accValue.params())
            }
        } else if user.areCrashAlertsOn {
            self.crashHandler.add(point: accValue)
            if self.crashHandler.shouldAlert() {
                let notificaitonManger = SLNotificationManager.sharedManager() as! SLNotificationManager
                notificaitonManger.sendCrashAlertForLock(withMacId: macAddress, withAccInfo: accValue.params())
            }
        }
    }
    
    private func handleChallengeDataForLockMacAddress(macAddress: String, data: NSData) {
        if data.length != 32 {
            print("Error: challenge data from lock is not 32 bytes")
            let info:[String: Any?] = [
                "lock": self.dbManager.getLockWithMacId(macAddress),
                "error": SLLockManagerConnectionError.IncorrectKeys,
                "message": self.textForConnectionError(error: .IncorrectKeys)
            ]
            NotificationCenter.default.post(
                name: NSNotification.Name(rawValue: kSLNotificationLockManagerErrorConnectingLock),
                object: info
            )
            return
        }
        
        guard let challengeKey = self.challengeKey(forLockOwner: macAddress) else {
            print(
                "Error: could not write challege data for lock with address: \(macAddress). "
                    + "Could not retrieve challege key from the keychain"
            )
            let info:[String: Any?] = [
                "lock": self.dbManager.getLockWithMacId(macAddress),
                "error": SLLockManagerConnectionError.MissingKeys,
                "message": self.textForConnectionError(error: .MissingKeys)
            ]
            NotificationCenter.default.post(
                name: NSNotification.Name(rawValue: kSLNotificationLockManagerErrorConnectingLock),
                object: info
            )
            return
        }
        
        var challengeString:String = ""
        let values:[UInt8] = data.UInt8Array()
        for value in values {
            var byteString = String(value, radix: 16, uppercase: false)
            if byteString.characters.count == 1 {
                byteString = "0" + byteString
            }
            
            challengeString += byteString
        }
        
        let challengeDataString = challengeKey + challengeString
        
        print("challenge string length: \(challengeDataString.characters.count)")
        guard let unhashedChallegeData = challengeDataString.bytesString() else {
            print(
                "Error: could not write challege data for lock with address: \(macAddress). "
                    + "Could not convert challenge string to data."
            )
            return
        }
        
        let cryptoHandler = SLCryptoHandler()
        guard let challengeData = cryptoHandler.sha256(with: unhashedChallegeData) else {
            print("Error: could not convert challenge data to sha256")
            return
        }
        
        print("challenge data is \(challengeData.count) bytes long")
        self.writeToLockWithMacAddress(
            macAddress: macAddress,
            service: .Security,
            characteristic: .ChallengeData,
            data: challengeData
        )
    }
    
    private func handleLEDStateForLockMacAddress(macAddress: String, data: NSData) {
        print("Unused: \(#function)")
    }
    
    private func handleLockSequenceWriteForMacAddress(macAddress: String, data: NSData) {
        NotificationCenter.default.post(
            name: NSNotification.Name(rawValue: kSLNotificationLockSequenceWritten),
            object: nil
        )
    }
    
    private func handleReadFirmwareVersionForMacAddress(macAddress: String, data: Data) {
        NotificationCenter.default.post(
            name: NSNotification.Name(rawValue: kSLNotificationLockManagerReadFirmwareVersion),
            object: data.versionString
        )
    }
    
    private func handleReadSerialNumberForMacAddress(macAddress: String, data: NSData) {
        let values:[UInt8] = data.UInt8Array()
        var serialNumber = ""
        for value in values {
            let digit = String(Character(UnicodeScalar(value)))
            if digit != "\0" {
                serialNumber += digit
            }
        }
        
        NotificationCenter.default.post(
            name: NSNotification.Name(rawValue: kSLNotificationLockManagerReadSerialNumber),
            object: serialNumber
        )
    }
    
    // MARK: Notification handlers
    private func handlePublicKeyConnectionPhaseForMacAddress(macAddress: String) {
        
        guard let publicKey = KeychainSwift(keyPrefix: macAddress).get(.publicKey) else {
            print("Error: could not enter public key connection phase. No user public key in keychain.")
            return
        }
        
        guard let data = publicKey.bytesString() else {
            print("Error: could not enter public key connection phase. Could not converte public key to bytes.")
            return
        }
        
        self.writeToLockWithMacAddress(
            macAddress: macAddress,
            service: .Security,
            characteristic: .PublicKey,
            data: data
        )
    }
    
    private func handleChallengeKeyConnectionPhaseForMacAddress(macAddress: String) {
        guard let challengeKey = self.challengeKey(forLockOwner: macAddress) else {
            print("Error: Could not get challenge data for current user.")
            return
        }
        
        guard let challengeKeyData = challengeKey.bytesString() else {
            print("Error: Could not convert challege key to data")
            return
        }
        
        self.writeToLockWithMacAddress(
            macAddress: macAddress,
            service: .Security,
            characteristic: .ChallengeKey,
            data: challengeKeyData
        )
    }
    
    private func handleSignedMessageConnectionPhaseForMacAddress(macAddress: String) {
        guard let signedMessage = KeychainSwift(keyPrefix: macAddress).get(.signedMessage) else {
            print("Error: No signed message in keychain for \(macAddress)")
            return
        }
        
        guard let data = signedMessage.bytesString() else {
            print("Error: could not convert signed messsage to bytes")
            return
        }
        
        self.writeToLockWithMacAddress(
            macAddress: macAddress,
            service: .Security,
            characteristic: .SignedMessage,
            data: data
        )
    }
    
    private func handleFirmwareUpdateCompletion(macAddress: String, success: Bool) {
        let value:UInt8 = 0x00
        let data = Data(bytes: [value])
        self.writeToLockWithMacAddress(
            macAddress: macAddress,
            service: .Configuration,
            characteristic: .ResetLock,
            data: data
        )
    }
    
    private func removeKeyChainItemsForLock(macAddress: String) {
        let keychain = KeychainSwift(keyPrefix: macAddress)
        keychain.delete(.publicKey)
        keychain.delete(.signedMessage)
    }
    
    // MARK: Write handlers
    private func handleLockResetForMacAddress(macAddress: String, success: Bool) {
        if success {
            print("Successfully wrote reset value to lock with address: \(macAddress)")
        } else {
            print("Error: could not reset lock with mac address: \(macAddress). Write failed")
        }
    }
    
    // MARK: SEBLEInterfaceManager Delegate methods
    func bleInterfaceManagerIsPowered(on interfaceManager: SEBLEInterfaceMangager!) {
        guard let locks:[SLLock] = self.dbManager.locksForCurrentUser() as? [SLLock] else {
            print("Will not start BLE scan. There is no user or the user doesn't have any locks")
            return
        }
        
        for lock in locks where lock.isCurrentLock {
            interfaceManager.startScan()
            NotificationCenter.default.post(
                name: NSNotification.Name(rawValue: kSLNotificationLockManagerBlePoweredOn),
                object: nil
            )
            break
        }
    }
    
    func bleInterfaceManagerIsPoweredOff(_ interfaceManager: SEBLEInterfaceMangager!) {
        NotificationCenter.default.post(
            name: NSNotification.Name(rawValue: kSLNotificationLockManagerBlePoweredOff),
            object: nil
        )
    }
    
    public func bleInterfaceManager(
        _ interfaceManger: SEBLEInterfaceMangager!,
        discoveredPeripheral peripheral: SEBLEPeripheral!,
        withAdvertisemntData advertisementData: [AnyHashable : Any]!)
    {
        if let lockName = peripheral.peripheral.name {
            print("Found lock named \(lockName)")
        }
        
        guard let lockName = advertisementData["kCBAdvDataLocalName"] as? String else {
            print(
                "Discovered peripheral \(peripheral.description) but cannot connect. " +
                "No local name in advertisement data."
            )
            return
        }
        
        guard let macAddress = lockName.macAddress() else {
            print("Could not retreive mac address from \(peripheral.description)")
            return
        }
        
        let hasBeenDetected = self.bleManager.hasNonConnectedPeripheral(withKey: macAddress)
        self.bleManager.setNotConnectedPeripheral(peripheral, forKey: macAddress)
        let lock:SLLock
        if let dbLock = self.dbManager.getLockWithMacId(macAddress) {
            lock = dbLock
            lock.name = lockName
            self.dbManager.save(lock)
        } else {
            lock = self.dbManager.newLock(withName: lockName, andUUID: peripheral.cbuuidasString())
        }
        
        print("Discoved lock \(lock.description). Lock manager is in state \(self.currentState)")
        
        if lock.isSetForDeletion {
            self.dbManager.delete(lock, withCompletion: nil)
            self.bleManager.removePeripheral(forKey: macAddress)
            self.bleManager.removeNotConnectPeripheral(forKey: macAddress)
            self.bleManager.stopScan()
            self.stopGettingHardwareInfo()
            self.currentState = .FindCurrentLock
            self.removeKeyChainItemsForLock(macAddress: macAddress)
            NotificationCenter.default.post(
                name: Notification.Name(rawValue: kSLNotificationLockManagerDeletedLock),
                object: macAddress
            )
        } else if self.currentState == .FindCurrentLock && lock.isCurrentLock {
            // Case 1: Check if lock is the current lock. This is the case that happens
            // when the app first connects to the current lock after a disconnection.
            self.connectToLockWithMacAddress(macAddress: macAddress)
        } else if self.currentState == .ActiveSearch && !hasBeenDetected {
            // Case 2: We are actively looking for locks. When a new lock is found
            // We'll send out an alert to let the rest of the app know that the lock was discovered
            NotificationCenter.default.post(
                name: NSNotification.Name(rawValue: kSLNotificationLockManagerDiscoverdLock),
                object: lock
            )
        } else if self.currentState == .UpdateFirmware && lock.isInBootMode {
            // Case 3: The lock has been reset to boot mode. This is currently used for firmware update,
            // however, there are other use cases for this mode.
            self.connectToLockWithMacAddress(macAddress: macAddress)
        } else if self.currentState == .UpdateFirmwareComplete {
            self.connectToLockWithMacAddress(macAddress: macAddress)
        } else {
            // Case 4: If the lock does not pass any of the preceeding tests, we should handle
            // the case here. We may need to disconnect the peripheral in the ble manager, but
            // then again maybe not. I need to think about that for awhile.
            print("The discovered lock: \(lockName) could not be processed")
        }
    }
    
    func bleInterfaceManager(
        _ interfaceManager: SEBLEInterfaceMangager!,
        connectedPeripheralNamed peripheralName: String!)
    {
        guard let macAddress = peripheralName.macAddress() else {
            print("Cannot connect to peripheral\(peripheralName). No mac id")
            return
        }
        
        guard let peripheral = self.bleManager.notConnectedPeripheral(forKey: macAddress) else {
            print("Ble Manager does not have a not connect peripheral named: \(peripheralName)")
            return
        }
        
        if let lock = self.dbManager.getLockWithMacId(macAddress) {
            self.securityPhase = lock.isInFactoryMode ? .PublicKey : .SignedMessage
            if !lock.isConnecting && shallowlyConnectedLocks.contains(macAddress) == false {
                lock.isConnecting = true
                self.dbManager.save(lock)
                NotificationCenter.default.post(
                    name: NSNotification.Name(rawValue: kSLNotificationLockManagerStartedConnectingLock),
                    object: nil
                )
            }
        }
        
        self.bleManager.removeNotConnectPeripheral(forKey: macAddress)
        self.bleManager.setConnectedPeripheral(peripheral, forKey: macAddress)
        self.bleManager.discoverServices(nil, forPeripheralWithKey: macAddress)
    }
    
    func bleInterfaceManager(
        _ interfaceManager: SEBLEInterfaceMangager!,
        discoveredServicesForPeripheralNamed peripheralName: String!
        )
    {
        print("Discovered services for " + peripheralName)
        self.bleManager.discoverServices(forPeripheralKey: peripheralName.macAddress())
    }
    
    func bleInterfaceManager(
        _ interfaceManager: SEBLEInterfaceMangager!,
        discoveredCharacteristicsFor service: CBService!,
        forPeripheralNamed peripheralName: String!
        )
    {
        print("Discovered characteristics for service \(service.description)")
        guard let macAddress = peripheralName.macAddress() else {
            print("Error: Discovered characteristics for \(peripheralName), but there is no mac address")
            return
        }
        
        self.bleManager.discoverCharacteristics(for: service, forPeripheralKey: macAddress)
        
        let serviceUUID = service.uuid.uuidString
        
        if self.shallowlyConnectedLocks.contains(macAddress) &&
            self.serviceUUID(service: .Hardware) == serviceUUID {
            self.flashLEDsForLockMacAddress(macAddress: macAddress)
        }
    }
    
    func shallowlyConnectToLock(macAddress: String) {
        self.shallowlyConnectedLocks.insert(macAddress)
        self.bleManager.connectToPeripheral(withKey : macAddress)
    }
    
    func bleInterfaceManager(
        _ interfaceManager: SEBLEInterfaceMangager!,
        peripheralName: String!,
        changedUpdateStateForCharacteristic characteristicUUID: String!
        )
    {
        guard let macAddress = peripheralName.macAddress() else {
            print("Error: Notification state updated for \(peripheralName), but there is no mac address.")
            return
        }
        
        if characteristicUUID == self.characteristicUUID(characteristic: .CommandStatus) {
            switch self.securityPhase {
            case .PublicKey:
                self.handlePublicKeyConnectionPhaseForMacAddress(macAddress: macAddress)
            case .ChallengeKey:
                self.handleChallengeKeyConnectionPhaseForMacAddress(macAddress: macAddress)
            case .SignedMessage:
                self.handleSignedMessageConnectionPhaseForMacAddress(macAddress: macAddress)
            default:
                print(
                    "Changed notification state for uuid: \(characteristicUUID) "
                        + "case not handled for security state: \(self.securityPhase)"
                )
            }
        } else if characteristicUUID == self.characteristicUUID(characteristic: .HardwareInfo) {
            print("Hardware info updated...")
        } else {
            print("Warning: changed notification state for uuid: \(characteristicUUID), but the case is not handled.")
        }
    }
    
    func bleInterfaceManager(
        _ interfaceManager: SEBLEInterfaceMangager!,
        wroteValueToPeripheralNamed peripheralName: String,
        forUUID uuid: String,
        withWriteSuccess success: Bool
        )
    {
        guard let macAddress = peripheralName.macAddress() else {
            print("Error: wrote value to \(peripheralName), but there is no mac address.")
            return
        }
        
        switch uuid {
        case self.characteristicUUID(characteristic: .Lock):
            self.bleManager.readValueForPeripheral(
                withKey: macAddress,
                forServiceUUID: self.serviceUUID(service: .Hardware),
                andCharacteristicUUID: self.characteristicUUID(characteristic: .Lock)
            )
        case self.characteristicUUID(characteristic: .LED):
            self.bleManager.readValueForPeripheral(
                withKey: macAddress,
                forServiceUUID: self.serviceUUID(service: .Hardware),
                andCharacteristicUUID: self.characteristicUUID(characteristic: .LED)
            )
        case self.characteristicUUID(characteristic: .ButtonSequece):
            self.bleManager.readValueForPeripheral(
                withKey: macAddress,
                forServiceUUID: self.serviceUUID(service: .Configuration),
                andCharacteristicUUID: self.characteristicUUID(characteristic: .ButtonSequece)
            )
        case self.characteristicUUID(characteristic: .ResetLock):
            self.handleLockResetForMacAddress(macAddress: macAddress, success: success)
        case self.characteristicUUID(characteristic: .CommandStatus):
            print("handle command status")
        case self.characteristicUUID(characteristic: .FirmwareUpdateDone):
            self.handleFirmwareUpdateCompletion(macAddress: macAddress, success: success)
        case self.characteristicUUID(characteristic: .TxPower):
            print("handle power update")
        default:
            print("Write to \(uuid) was a \(success ? "success": "failure") but the case is not handled")
        }
    }
    
    public func bleInterfaceManager(
        _ interfaceManager: SEBLEInterfaceMangager!,
        updatedPeripheralNamed peripheralName: String,
        forCharacteristicUUID characteristicUUID: String,
        with data: Data)
    {
        guard let macAddress = peripheralName.macAddress() else {
            print(
                "Error: updated characteristc \(characteristicUUID) for "
                    + "\(peripheralName), but there is no mac address"
            )
            return
        }
        
        let convertedData = data as NSData
        switch characteristicUUID {
        case self.characteristicUUID(characteristic: .CommandStatus):
            self.handleCommandStatusUpdateForLockMacAddress(macAddress: macAddress, data: convertedData)
        case self.characteristicUUID(characteristic: .HardwareInfo):
            self.handleHardwareServiceForMacAddress(macAddress: macAddress, data: convertedData)
        case self.characteristicUUID(characteristic: .Lock):
            self.handleLockStateForLockMacAddress(macAddress: macAddress, data: convertedData)
        case self.characteristicUUID(characteristic: .Accelerometer):
            self.handleAccelerometerForLockMacAddress(macAddress: macAddress, data: convertedData)
        case self.characteristicUUID(characteristic: .ChallengeData):
            self.handleChallengeDataForLockMacAddress(macAddress: macAddress, data: convertedData)
        case self.characteristicUUID(characteristic: .LED):
            self.handleLEDStateForLockMacAddress(macAddress: macAddress, data: convertedData)
        case self.characteristicUUID(characteristic: .ButtonSequece):
            self.handleLockSequenceWriteForMacAddress(macAddress: macAddress, data: convertedData)
        case self.characteristicUUID(characteristic: .FirmwareVersion):
            self.handleReadFirmwareVersionForMacAddress(macAddress: macAddress, data: data)
        case self.characteristicUUID(characteristic: .SerialNumber):
            self.handleReadSerialNumberForMacAddress(macAddress: macAddress, data: convertedData)
        case self.characteristicUUID(characteristic: .Magnet):
            print("need to write a method to handle the magnet update")
        default:
            print("No matching case updating peripheral: \(peripheralName) for uuid: \(characteristicUUID)")
        }
    }
    
    func bleInterfaceManager(
        _ interfaceManager: SEBLEInterfaceMangager!,
        disconnectedPeripheralNamed peripheralName: String!)
    {
        guard let macAddress = peripheralName.macAddress() else {
            print("Could not get mac address from periphreal name: " + peripheralName)
            return
        }
        
        // It is removing the peripherals of the shallowly connected lock
        // so that's why this check is necessary
        if (shallowlyConnectedLocks.contains(macAddress)) {
            self.shallowlyConnectedLocks.remove(macAddress)
            return
        }
        
        let isInFirmwareUpdate = self.currentState == .UpdateFirmwareComplete
        let blePeripheral = self.bleManager.connectedPeripheral(forKey: macAddress)
        self.stopGettingHardwareInfo()
        self.currentState = .FindCurrentLock
        self.bleManager.isInBackground = self.isInBackgroundState()
        self.bleManager.removeConnectedPeripheral(forKey: macAddress)
        self.bleManager.removeNotConnectPeripheral(forKey: macAddress)
        
        NotificationCenter.default.post(
            name: NSNotification.Name(rawValue: kSLNotificationLockManagerDisconnectedLock),
            object: macAddress
        )
        
        if let lock = dbManager.getLockWithMacId(macAddress),
            lock.isSetForDeletion == false && self.afterUserDisconnectLockClosure == nil && self.afterDisconnectLockClosure == nil {
            // This is the case where the lock has been disconnected but should be reconnected whenever it is found
            print("Reconnecting to lock: \(peripheralName)")
            if isInFirmwareUpdate {
                self.startBleScan()
            } else if let peripheral = blePeripheral {
                print("peripheral exists")
                lock.isConnecting = true
                self.dbManager.save(lock)
                self.bleManager.setNotConnectedPeripheral(peripheral, forKey: macAddress)
                self.bleManager.connectToPeripheral(withKey: macAddress)
            }
        } else {
            self.afterDisconnectLockClosure?()
            self.afterDisconnectLockClosure = nil
            self.afterUserDisconnectLockClosure?()
            self.afterUserDisconnectLockClosure = nil
        }
    }
    
    func bleInterfaceManager(_ interfaceManager: SEBLEInterfaceMangager!, restoredPeripherals peripherals: [Any]!) {
        guard let lock = self.dbManager.getCurrentLockForCurrentUser() else {
            print("ble manager has restored state, but the user has no current locks")
            return
        }
        
        guard let blePeripherals = peripherals as? [SEBLEPeripheral] else {
            return
        }
        
        if let macId = lock.macId, let peripheral = blePeripherals.filter({$0.name.macAddress() == macId}).first {
            self.bleManager.setNotConnectedPeripheral(peripheral, forKey: macId)
            self.connectToLockWithMacAddress(macAddress: macId)
        }
    }
}

extension Data {
    var versionString: String {
        let versionIndex = 9
        let reversionIndex = 11
        let defaultValue = "Undefined".localized()
        
        var values = [UInt8](self)
        
        guard values.count > versionIndex else { return defaultValue }
        var version = "\(values[versionIndex])"
        
        guard values.count > reversionIndex else { return version }
        version += String(format: ".%02d", values[reversionIndex])

        return version
    }
}
