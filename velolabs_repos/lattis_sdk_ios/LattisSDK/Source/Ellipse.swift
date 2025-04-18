//
//  Ellipse.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 8/1/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import CoreBluetooth

fileprivate let connectionTime: TimeInterval = 20
fileprivate let writeLimit: Int = 132 // Maximum bytes count allowed to write at the time
fileprivate let magnetShackleDetected: Float = 8192

@objcMembers
public class Ellipse: NSObject {
    
    /// Ellipse unique identifier for internal Lattis services
    /// - Warning: Not the same as Bluetoothd device Mac Address
    public let macId: String
    
    /// GATT Server name
    public let name: String
    public let accelerometer = Accelerometer()
    public fileprivate(set) var metadata: Metadata? {
        didSet {
            guard let meta = metadata else { return }
            security = meta.security
            safe(handlers) {$0.ellipse(self, didUpdate: .metadata(meta))}
        }
    }
    public fileprivate(set) var magnet: Accelerometer.Coordinate = .zero {
        didSet {
            safe(handlers) {$0.ellipse(self, didUpdate: .magnet(self.magnet))}
        }
    }
    
    /// Convenience property; Only checks if device is pared
    public var isPaired: Bool {
        switch connection {
        case .paired:
            return true
        default:
            return false
        }
    }
    
    /// Convinience property; Check if FW update is finished
    public var isUpdated: Bool {
        switch connection {
        case .updating(let progress):
            return progress >= 1
        default:
            return false
        }
    }
    public fileprivate(set) var firmwareVersion: String?
    public fileprivate(set) var bootloaderVersion: String?
    public fileprivate(set) var serialNumber: String?
    public internal(set) var isFactoryMode: Bool
    
    /// Connection status
    public internal(set) var connection: Connection = .unpaired {
        didSet {
            let c = connection
            switch connection {
            case .unpaired, .reconnecting:
                invalidateTimer()
                status = .signedMessage
                _security = .middle
                charsToCheck.removeAll()
            case .connecting:
                if let pin = unlockPin {
                    peripheral.discoverServices([Ellipse.Service.configuration.uuid])
                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                        _ = try? self.set(pinCode: pin)
                    }
                } else {
                    peripheral.discoverServices(Ellipse.Service.pairing)
                    startTimer(reset: false)
                }
            case .paired:
                saveKeys()
                invalidateTimer()
            case .failed(let error):
                charsToCheck.removeAll()
                _security = .middle
                invalidateTimer()
                status = .signedMessage
                if error.isEllipseTimeout {
                    onTimeout()
                }
                main { self.onFlashLED?(error) }
                disconnect()
            default:
                break
            }
            safe(handlers) {
                $0.ellipse(self, didUpdate: c)
            }
        }
    }
    
    /// Lock security status
    public fileprivate(set) var security: Security {
        set {
            guard case .paired = connection else { return }
            let previous = _security
            if newValue != .invalid && newValue != .middle {
                _security = newValue
            }
            if previous != newValue {
                safe(handlers, enumerate: {$0.ellipse(self, didUpdate: newValue)})
            }
        }
        get {
            return _security
        }
    }
    
    var connectClosure: (() -> ())!
    var sign: ((@escaping (String, String) -> ()) -> ())!
    var disconnectClosure: (() -> ())!
    var peripheral: CBPeripheral {
        didSet {
            peripheral.delegate = self
        }
    }
    var challengeKey: String?
    var signedMessage: String?
    var publicKey: String?
    var isSigningDataExists: Bool {
        return challengeKey != nil && signedMessage != nil
    }
    var onTimeout: () -> () = {}
    
    // Connection parameters
    fileprivate var txPower: UInt8?
    fileprivate var durationAfterDrop: Int = 30 // Minutes
    fileprivate let queue = DispatchQueue(label: "BLE.Ellipse.Queue", qos: .utility)
    
    fileprivate var handlers = WeakCollection<EllipseDelegate>()
    fileprivate var onFlashLED: ((Error?) -> ())?
    fileprivate var _security: Security = .middle
    fileprivate var statusStorage: Set<Characteristic> = []
    fileprivate var timeoutTimer: DispatchSourceTimer?
    fileprivate var notificationsStorage: [CBCharacteristic] = []
    fileprivate var firmware: [UInt8] = []
    fileprivate var firmwareSize: Float = 0
    fileprivate var unlockPin: [Pin]? = nil
    fileprivate let allChars: Set<Characteristic> = [
        .challengeKey, .hardwareInfo, .connection, .challengeData, .magnet, .serialNumber, .firmwareVersion, .writeFirmware, .accelerometer, .resetLock, .signedMessage, .LED, .lock, .buttonSequece, .publicKey, .capTouch, .commandStatus
    ]
    fileprivate var charsToCheck: Set<Characteristic> = [] {
        didSet {
            if allChars == charsToCheck, case .paired = connection {
                safe(handlers) {
                    $0.ellipse(self, didUpdate: .ready)
                }
            }
        }
    }
    fileprivate var status: Status {
        get {
            if statusStorage.contains(.publicKey) && statusStorage.contains(.challengeKey) == false {
                if statusStorage.contains(.signedMessage) {
                    return .publicKey
                } else {
                    return .challengeKey
                }
            } else if statusStorage.contains(.signedMessage) == false {
                return .signedMessage
            } else if statusStorage.contains(.challengeData) == false {
                return .challengeData
            } else {
                return .connected
            }
        }
        set {
            switch newValue {
            case .publicKey:
                statusStorage.removeAll()
            default:
                statusStorage = [.publicKey, .challengeKey]
            }
        }
    }
    
    init?(peripheral: CBPeripheral) {
        guard let name = peripheral.name, let macId = peripheral.macId else { return nil }
        self.macId = macId
        self.name = name
        self.isFactoryMode = name.contains("-")
        self.peripheral = peripheral
        super.init()
        self.status = .signedMessage
        peripheral.delegate = self
        accelerometer.onUpdate = { [unowned self] value in
            safe(self.handlers) {$0.ellipse(self, didUpdate: .accelerometer(self.accelerometer.value))}
        }
    }
    
    /// Start connection/pairing process
    ///
    /// - Parameters:
    ///   - handler: weak object of EllipseDelegate. No need to unsubscribe handler before releasing it's proberty
    ///   - secret: Challenge Key value in case if should come from outside of SDK
    public func connect(handler: EllipseDelegate? = nil, secret: String? = nil) {
        if let d = handler {
            subscribe(d)
        }
        switch connection {
        case .unpaired, .restored, .reconnecting:
            break
        case .paired:
            handler?.ellipse(self, didUpdate: .paired)
            return
        default:
            return
        }
        startTimer()
        guard unlockPin == nil else {
            return connectClosure()
        }
        if let key = Key(macId) {
            signedMessage = key.signedMessage
            challengeKey = key.challengeKey
            connectClosure()
        } else  {
            sign { [weak self] message, key in
                self?.publicKey = key
                self?.signedMessage = message
                let s = (secret ?? EllipseManager.secret)
                if let sec = s {
                    self?.challengeKey = sec.challengeKeyValue
                }
                self?.connectClosure()
            }
        }
    }
    
    public func disconnect() {
        disconnectClosure()
    }
    
    /// Subscribe to lock events. Use it when you want to get events from the lock and you don't need to connect
    ///
    /// - Parameters:
    ///   - handler: weak object of EllipseDelegate. No need to unsubscribe handler before releasing it's proberty
    ///   - theft: TheftPresentable protocol used to get theft alerts
    ///   - crash: CrashPresentable protocol used to get crash alerts
    public func subscribe(_ handler: EllipseDelegate, theft: TheftPresentable? = nil, crash: CrashPresentable? = nil) {
        if let t = theft {
            accelerometer.subscribeTheft(handler: t)
        }
        if let c = crash {
            accelerometer.subscribeCrash(handler: c)
        }
        guard handlers.contains(where: {$0 === handler}) == false else { return }
        handlers.insert(handler)
        main {
            if case .failed = self.connection {} else {
                handler.ellipse(self, didUpdate: self.connection)
            }
            switch self.connection {
            case .failed, .flashingLED, .paired:
                guard self.security != .middle else { break }
                handler.ellipse(self, didUpdate: self.security)
            default:
                break
            }
            if let ver = self.firmwareVersion {
                handler.ellipse(self, didUpdate: .firmwareVersion(ver))
            }
            if let meta = self.metadata {
                handler.ellipse(self, didUpdate: .metadata(meta))
            }
            if let serial = self.serialNumber {
                handler.ellipse(self, didUpdate: .serialNumber(serial))
            }
            if let magLock = self.isMagnetAutoLockEnabled {
                handler.ellipse(self, didUpdate: .magnetAutoLockEnabled(magLock))
            }
        }
    }
    
    /// Unsubscribe from lock events. Call this if you only want to stop handling lock events
    ///
    /// - Parameter handler: EllipseDelegate protocol confirmed object
    public func unsubscribe(_ handler: EllipseDelegate) {
        handlers.delete(handler)
    }
    
    /// Call this to secure the lock
    /// If lock is already in secure state it will notify all handlers with actual security state
    public func lock() {
        guard security != .locked else {
            safe(handlers) { $0.ellipse(self, didUpdate: .locked) }
            return
        }
        try? write(bytes: [Security.locked.rawValue], to: .lock, for: .hardware)
    }
    
    /// Call this to unsecure the lock
    /// If lock is already in unsecure state it will notify all handlers with actual security state
    public func unlock() {
        guard security != .unlocked else {
            safe(handlers) { $0.ellipse(self, didUpdate: .unlocked) }
            return
        }
        try? write(bytes: [Security.unlocked.rawValue], to: .lock, for: .hardware)
    }
    
    /// Enable automatic locks
    /// This feature based on RSSI/connection
    /// It will lock automatically when disconnected from the lock or too far from it based on RSSI streingh
    public func enableAutoLock() {
        _ = try? write(bytes: [Security.auto.rawValue], to: .lock, for: .hardware)
    }
    
    /// Asynchronos read of actual security state
    /// Response will come in delegate method
    public func checkSecurityStatus() {
        do {
            try read(.lock, for: .hardware)
        } catch {
            print("Lock State read error:", error)
        }
    }
    
    /// Write new pin code to the lock
    ///
    /// - Warning: Higly not recommended to use. If you decide to use it, please make sure to save a new pin-code value.
    /// - Parameter pinCode: Array of Ellipse.Pin values
    /// - Throws: EllipseError.wrongPinCode(_) if pin is shorter than 4 or larger than 16
    public func set(pinCode: [Pin]) throws {
        let max = 16
        let min = 4
        guard pinCode.count >= min && pinCode.count <= max else { throw EllipseError.wrongPinCode(pinCode) }
        let empty = repeatElement(Byte.null.rawValue, count: max - pinCode.count)
        try write(bytes: pinCode.map({ $0.rawValue }) + empty, to: .buttonSequece, for: .configuration)
    }
    
    public func updateWith(contentsOf url: URL) throws {
        let data = try Data(contentsOf: url)
        queue.async {
            let limit = 128
            var bytes = [UInt8](data)
            var result: [UInt8] = []
            var offcet: [UInt8] = Array(repeating: 0x00, count: 4)
            var start = bytes.startIndex
            var end = start + limit
            while end != start {
                var part = offcet + bytes[start..<end]
                while part.count < 132 {
                    part.append(0xFF)
                }
                start = end
                end = bytes.endIndex - end >= limit ? end + limit : start + bytes.endIndex - end
                if offcet[0] == 0x00 {
                    offcet[0] = 0x80
                } else {
                    offcet[0] = 0x00
                }
                if offcet[0] == 0x00 {
                    offcet[1] += 1
                }
                result += part
            }
            self.update(firmware: result)
        }
    }
    
    /// Strart Firmware update. You can handle the progress in delegate methods (self.connection == .updating(Float))
    ///
    /// - Parameter firmware: Prepared bytes array
    public func update(firmware: [UInt8]) {
        self.firmware = firmware
        firmwareSize = Float(firmware.count)
        updateFirmware()
    }
    
    /// Remove all the cached values (signed message, challenge key)
    public func cleanCache() {
        Key.remove(macId)
    }
    
    /// Reset the lock to the factory mode (remove public key)
    ///
    /// - Parameter disconnect: use true if you don't want to reconnect automatically after reset
    public func factoryReset(disconnect: Bool = false) {
        cleanCache()
        do {
            try write(byte: .factoryReset, to: .resetLock, for: .configuration)
            if disconnect {
                connection = .unpaired
            }
        } catch {
            print(error)
        }
    }
    
    /// Reboot the lock
    public func bootReset() {
        try? write(byte: .reset, to: .resetLock, for: .configuration)
    }
    
    fileprivate var capTouchBytes: [UInt8] = []
    
    fileprivate var shouldEnableCapTouch: Bool?
    fileprivate var shouldChangeMagnetAutoLockState: Bool?
    
    /// Manage captive touch pannel
    /// nil - captive touch pannel status is not received from the lock yet
    public var isCapTouchEnabled: Bool? {
        set {
            guard let isEnabled = newValue else { return }
            if case .unpaired = connection {
                shouldEnableCapTouch = isEnabled
                connection = .manageCapTouch
                connectClosure()
                return
            }
            if capTouchBytes.count < 2 {
                return
            }
            let byte = capTouchBytes[1]
            let isDesabled = byte.isBitSet(position: .capTouch(.disableCapTouch))
            if isEnabled && isDesabled == false {
                return
            }
            if isEnabled == false && isDesabled {
                return
            }
            capTouchBytes[1] = byte.replaced(bit: isEnabled ? .zero : .one, in: .capTouch(.disableCapTouch))
            try? write(bytes: capTouchBytes, to: .capTouch, for: .configuration)
            if case .manageCapTouch = connection {
                connection = .unpaired
            }
        }
        get {
            guard capTouchBytes.count > 1 else { return nil }
            return capTouchBytes[1].isBitSet(position: .capTouch(.disableCapTouch)) == false
        }
    }
    
    /// Manage magned based auto lock feature
    /// nil - magnet auto lock status is not received from the lock yet
    public var isMagnetAutoLockEnabled: Bool? {
        set {
            guard let value = newValue, case .paired = connection, capTouchBytes.count >= 2 else {
                shouldChangeMagnetAutoLockState = newValue
                return
            }
            let byte = capTouchBytes[1]
            let isEnabled = byte.isBitSet(position: .capTouch(.magneticAutoLock))
            if isEnabled && value {
                return
            }
            if !isEnabled && !value {
                return
            }
            capTouchBytes[1] = byte.replaced(bit: value ? .one : .zero, in: .capTouch(.magneticAutoLock))
            try? write(bytes: capTouchBytes, to: .capTouch, for: .configuration)
        }
        get {
            guard capTouchBytes.count > 1 else { return nil }
            return capTouchBytes[1].isBitSet(position: .capTouch(.magneticAutoLock))
        }
    }
    
    /// Check if shackle is insertet to the lock
    public var isShackleInserted: Bool = false {
        didSet {
            guard isShackleInserted != oldValue else { return }
            safe(handlers) { $0.ellipse(self, didUpdate: .shackleInserted(self.isShackleInserted)) }
        }
    }
    
    /// Set only property
    /// set true to start blinking, it won't stop automatically
    /// will stop after disconnect or if you set false to the property
    public var isLEDBlinking: Bool {
        set {
            let blinking: [UInt8] = newValue ? [0xCF, 0xCF, 0x00, 0x01, 0x00] : [0x00]
            _ = try? write(bytes: blinking , to: .LED, for: .hardware)
        }
        get {
            return false
        }
    }
    
    /// Async read of actual captive touch state
    /// Handle response in delegate methotds
    public func readCapTouchState() {
        do {
            try read(.capTouch, for: .configuration)
        } catch {
            print(error)
        }
    }
    
    /// Starts blinking LEDs. No pairing requered. You can call it even for disconnected lock
    ///
    /// - Parameter completion: callback received when LEDs stop blinking or Error occured
    public func flashLED(completion: ((Error?) -> ())? = nil) {
        onFlashLED = completion
        connection = .flashingLED
        connectClosure()
        startTimer()
    }
    
    /// Unlock with valid pin-code. 3 attempts in one day
    /// No pairing required
    ///
    /// - Parameter pin: valid pin code sequence
    public func unlock(with pin: [Pin]) {
        unlockPin = pin
        connect()
    }
    
    func blinkLED() {
        let blinking: [UInt8] = [0xCF, 0xCF, 0x00, 0x01, 0x00]
        do {
            try write(bytes: blinking , to: .LED, for: .hardware)
        } catch {
            print(error)
        }
        queue.asyncAfter(deadline: .now() + 6) { [weak self] in
            try? self?.write(byte: .null, to: .LED, for: .hardware)
            if let flash = self?.onFlashLED {
                main { flash(nil) }
                self?.onFlashLED = nil
            }
            if let s = self, case .flashingLED = s.connection {
                s.disconnect()
            }
        }
    }
    
    func restoreFromBackground() {
        statusStorage = [.signedMessage, .publicKey, .challengeKey, .challengeData]
        connection = .paired
        peripheral.discoverServices(Ellipse.Service.manage)
    }
    
    func write(byte: Byte, to char: Characteristic, for service: Service) throws {
        try write(bytes: [byte.rawValue], to: char, for: service)
    }
    
    func write(bytes: [UInt8], to char: Characteristic, for service: Service) throws {
        try write(data: Data(bytes), to: char, for: service)
    }
    
    func write(data: Data, to char: Characteristic, for service: Service) throws {
        let characteristic = try transform(char: char, for: service)
        peripheral.writeValue(data, for: characteristic, type: .withResponse)
    }
    
    func read(_ char: Characteristic, for service: Service) throws {
        let characteristic = try transform(char: char, for: service)
        peripheral.readValue(for: characteristic)
    }
    
    fileprivate func saveKeys() {
        guard let sMess = signedMessage, let cKey = challengeKey else { return }
        Key(signedMessage: sMess, challengeKey: cKey).save(macId)
    }
    
    fileprivate func transform(char: Characteristic, for service: Service) throws -> CBCharacteristic {
        guard let serv = peripheral.services?.filter({ $0.uuid == service.uuid }).first else { throw ellipse(.missingService(service)) }
        guard let characteristic = serv.characteristics?.filter({ $0.uuid == char.uuid }).first else { throw ellipse(.missingCharacteristic(char)) }
        return characteristic
    }
    
    fileprivate func startTimer(reset: Bool = true) {
        guard timeoutTimer == nil || reset else { return }
        let multi: Double
        switch connection {
        case .updating:
            multi = 2
        default:
            multi = 1
        }
        invalidateTimer()
        timeoutTimer = DispatchSource.makeTimerSource()
        timeoutTimer?.schedule(deadline: .now() + connectionTime*multi, repeating: connectionTime)
        timeoutTimer?.setEventHandler(handler: { [weak self] in
            self?.connection = .failed(ellipse(.timeout))
        })
        timeoutTimer?.resume()
    }
    
    fileprivate func invalidateTimer() {
        timeoutTimer?.cancel()
        timeoutTimer = nil
    }
    
    fileprivate func setConnectionParams() {
        notificationsStorage.forEach{peripheral.setNotifyValue(true, for: $0)}
        
        do {
            let params: [UInt8] = [txPower ?? Locale.current.txPowerValue, UInt8(durationAfterDrop/15), Byte.null.rawValue]
            try write(bytes: params, to: .connection, for: .hardware)
        } catch {
            print("Setting connection params error: \(error)")
        }
    }
    
    fileprivate func updateFirmware() {
        let progress = 1 - (Float(firmware.count) / firmwareSize)
        connection = .updating(progress)
        if firmware.isEmpty {
            _security = .middle
            do {
                if peripheral.isEllboot {
                    try write(byte: .null, to: .firmwareUpdateDone, for: .boot)
                } else {
                    try write(byte: .null, to: .firmwareVersion, for: .configuration)
                }
            } catch {
                print(error)
            }
            return
        }
        
        let limit = firmware.count / writeLimit > 0 ? writeLimit : firmware.count
        let bytes = Array(firmware[0..<limit])
        do {
            if peripheral.isEllboot {
                try write(bytes: bytes, to: .writeFirmware, for: .boot)
            } else {
                try write(bytes: bytes, to: .writeFirmware, for: .configuration)
            }
        } catch {
            print(error)
        }
        firmware = limit == firmware.count ? [] : Array(firmware[limit..<firmware.endIndex])
    }
    
    fileprivate func writePublicKey() {
        guard let publicKey = publicKey else {
            return sign { [weak self] _, key in
                self?.publicKey = key
                self?.writePublicKey()
            }
        }
        guard let data = publicKey.dataValue else {
            return connection = .failed(EllipseError.missingPublicKey)
        }
        do {
            try write(data: data, to: .publicKey, for: .security)
        } catch {
            connection = .failed(error)
        }
    }
    
    fileprivate func writeChallengeKey() {
        guard let challengeKey = challengeKey, let data = challengeKey.dataValue  else {
            return connection = .failed(EllipseError.missingChallengeKey)
        }
        do {
            try write(data: data, to: .challengeKey, for: .security)
        } catch {
            connection = .failed(error)
        }
    }
    
    fileprivate func writeSignedMessage() {
        guard let signedMessage = signedMessage, let data = signedMessage.dataValue  else {
            return connection = .failed(EllipseError.missingSignedMessage)
        }
        do {
            try write(data: data, to: .signedMessage, for: .security)
        } catch {
            connection = .failed(error)
        }
    }
    
    // MARK: Handlers
    
    fileprivate func handleChallenge(data: Data?) {
        guard let data = data?.hex else { return connection = .failed(EllipseError.missingChallengeData)}
        guard let challengeKey = challengeKey,
            let value = (challengeKey + data).dataValue?.sha256() else { return connection = .failed(EllipseError.missingChallengeKey)}
        do {
            try write(data: value, to: .challengeData, for: .security)
        } catch {
            connection = .failed(error)
        }
    }
    
    fileprivate func handleCommandStatus(data: Data?) {
        guard let data = data else { return print("Error: command status nil value") }
        let values = [UInt8](data)
        guard let value = values.first, let byte = Byte(rawValue: value) else {
            return print("Error: wtong data for command status (\(values))")
        }
        print("Command status updated with value: \(byte)")
        switch byte {
        case .null where status == .challengeKey:
            writeChallengeKey()
        case .null where status == .signedMessage:
            writeSignedMessage()
        case .null where isUpdated:
            reset()
        case .securityGuest, .securityOwner:
            try? read(.challengeData, for: .security)
        case .securityOwnerVerified, .securityGuestVerified:
            connection = .paired
            peripheral.discoverServices(Ellipse.Service.manage)
        case .accessDenied where status == .challengeKey:
            isFactoryMode = false
            writeSignedMessage()
        case .accessDenied where status == .challengeData:
            status = .publicKey
            writePublicKey()
        case .accessDenied:
            connection = .failed(EllipseError.accessDenided)
        case .lockUnlockFailed:
            security = .middle
        default:
            print("Command status: no handler for value \(byte)")
        }
    }
    
    fileprivate func discoverManagementServices() {
        peripheral.discoverServices(Ellipse.Service.manage)
    }
    
    fileprivate func handleSecurityState(data: Data?) {
        guard let data = data,
            let value = [UInt8](data).first,
            let state = Security(rawValue: value) else { return }
        security = state
    }
    
    fileprivate func handleFirmwareVersion(data: Data?) {
        guard let data = data else { return }
        bootloaderVersion = data.bootLoaderVersion
        firmwareVersion = data.ellipseVersion
        if let ver = firmwareVersion {
            safe(handlers, enumerate: {$0.ellipse(self, didUpdate: .firmwareVersion(ver))})
        }
    }
    
    fileprivate func handleSerialNumber(data: Data?) {
        guard let data = data else { return }
        serialNumber = String(data: data, encoding: .utf8)
        if let serial = serialNumber {
            safe(handlers, enumerate: {$0.ellipse(self, didUpdate: .serialNumber(serial))})
        }
    }
    
    fileprivate func handleHardwareInfo(data: Data?) {
        guard let data = data else { return }
        let bytes = [UInt8](data)
        guard var metadata = Metadata(bytes: bytes) else { return }
        if metadata.security != security, let voltage = self.metadata?.voltage {
            metadata.voltage = voltage
        }
        self.metadata = metadata
    }
    
    fileprivate func handleMagnet(data: Data?) {
        guard let data = data, data.count >= 6 else { return }
        let bytes = [UInt8](data)
        magnet = Accelerometer.Coordinate(bytes: bytes, x: 0, y: 2, z: 4, count: 2)
        isShackleInserted = abs(magnet.z) >= magnetShackleDetected
    }
    
    fileprivate func handleCapTouch(data: Data?) {
        guard let data = data else { return }
        let bytes = [UInt8](data)
        if bytes.count >= 2 {
            capTouchBytes = bytes
        }
        if let should = shouldEnableCapTouch {
            isCapTouchEnabled = should
            shouldEnableCapTouch = nil
        }
        if let enabled = isCapTouchEnabled {
            safe(handlers) { $0.ellipse(self, didUpdate: .capTouchEnabled(enabled)) }
        }
        if let should = shouldChangeMagnetAutoLockState {
            isMagnetAutoLockEnabled = should
            shouldChangeMagnetAutoLockState = nil
        }
        if let enabled = isMagnetAutoLockEnabled {
            safe(handlers) { $0.ellipse(self, didUpdate: .magnetAutoLockEnabled(enabled))}
        }
//        if let first = bytes.first, first == 0 {
//            var b = bytes
//            b[0] = 0xFF
//            do {
//                try write(bytes: b, to: .capTouch, for: .configuration)
//            } catch {
//                print(error)
//            }
//        }
    }
    
    fileprivate func readFWVersion() {
        do {
            try read(.firmwareVersion, for: .configuration)
        } catch {
            print(error)
        }
    }
    
    fileprivate func readSerialNumber() {
        do {
            try read(.serialNumber, for: .configuration)
        } catch {
            print(error)
        }
    }
    
    fileprivate func reset() {
        status = .signedMessage
        do {
            try write(byte: .null, to: .resetLock, for: .configuration)
        } catch {
            print(error)
        }
    }
}

extension Ellipse: CBPeripheralDelegate {
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        notificationsStorage.removeAll()
        peripheral.services?.forEach { service in
            peripheral.discoverCharacteristics(Ellipse.Characteristic.all.map({$0.uuid}), for: service)
        }
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        service.characteristics?.forEach { characteristic in
            guard let char = Characteristic(characteristic) else { return }
            charsToCheck.insert(char)
            switch char {
            case .commandStatus:
                peripheral.setNotifyValue(true, for: characteristic)
            case .LED:
                blinkLED()
            case .capTouch:
                readCapTouchState()
            case .firmwareVersion:
                readFWVersion()
            case .serialNumber:
                readSerialNumber()
            case .lock:
                checkSecurityStatus()
            case .connection:
                setConnectionParams()
            default:
                break
            }
            
            if char.shouldNotify {
                peripheral.setNotifyValue(true, for: characteristic)
//                self.notificationsStorage.append(characteristic)
            }
        }
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        guard let char = Characteristic(characteristic) else { return }
        var service: Service? = nil
        switch char {
        case .publicKey:
            statusStorage.insert(.publicKey)
        case .challengeKey:
            statusStorage.insert(.challengeKey)
        case .signedMessage:
            statusStorage.insert(.signedMessage)
        case .challengeData:
            statusStorage.insert(.challengeData)
        case .LED:
            service = .hardware
        case .buttonSequece:
            if unlockPin != nil {
                unlockPin = nil
                return disconnect()
            }
            service = .configuration
        case .writeFirmware:
            updateFirmware()
        case .lock:
            checkSecurityStatus()
        default:
            break
        }
        
        if let service = service {
            _ = try? read(char, for: service)
        }
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        guard let char = Characteristic(characteristic) else { return }
        let data = characteristic.value
        switch char {
        case .challengeData:
            handleChallenge(data: data)
        case .commandStatus:
            handleCommandStatus(data: data)
        case .lock:
            handleSecurityState(data: data)
        case .firmwareVersion:
            handleFirmwareVersion(data: data)
        case .serialNumber:
            handleSerialNumber(data: data)
        case .accelerometer:
            accelerometer.handle(data: data, ellipse: self)
        case .hardwareInfo:
            handleHardwareInfo(data: data)
        case .magnet:
            handleMagnet(data: data)
        case .capTouch:
            handleCapTouch(data: data)
        default:
            break
        }
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        guard let char = Characteristic(characteristic) else { return }
        switch (char, status) {
        case (.commandStatus, .publicKey):
            writePublicKey()
        case (.commandStatus, .signedMessage):
            writeSignedMessage()
        default:
            break
        }
    }
}
