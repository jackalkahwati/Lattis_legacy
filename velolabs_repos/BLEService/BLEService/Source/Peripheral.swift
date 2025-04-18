//
//  Peripheral.swift
//  BLEService
//
//  Created by Ravil Khusainov on 05/02/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreBluetooth

public final class Peripheral: NSObject {
    internal var peripheral: CBPeripheral {
        didSet {
            peripheral.delegate = self
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                self.ready()
            }
        }
    }
    internal var ready: () -> () = {}
    internal var flash: (() -> ())?
    internal var breakConection: ((Peripheral) -> ())?
    public let name: String
    public let macId: String
    public var userId: String? // Should be set from outside
    public var publicKey: String? // Should be set from outside
    public var signedMessage: String? // Should be set from outside
    public var isFactoryMode: Bool
    public var isConnected = false
    public var connectionTimeout: TimeInterval = 60
    public internal(set) var firmwareVersion: String?
    public internal(set) var metadata: Metadata?
    public let accelerometerHandler = AccelerometerHandler()
    
    // Connection parameters
    public var txPower: UInt8?
    public var durationAfterDrop: Int = 30 // Minutes
    
    fileprivate var readers: [Characteristic: [(Data?) -> ()]] = [:]
    fileprivate let writeLimit = 132 // Bytes
    fileprivate var fwSize: Double = 0
    fileprivate var updateProgress: ((Double) -> ())?
    internal var updateState: UpdateState = .none
    fileprivate var timeoutTimer: Timer?
    fileprivate var reconnectTimer: Timer?
    fileprivate var currentState: LockState = .middle
    
    fileprivate var securityStorage: Set<Characteristic> = []
    fileprivate var security: Security {
        if securityStorage.contains(.publicKey) && securityStorage.contains(.challengeKey) == false {
            if securityStorage.contains(.signedMessage) {
                return .publicKey
            } else {
                return .challengeKey
            }
        } else if securityStorage.contains(.signedMessage) == false {
            return .signedMessage
        } else if securityStorage.contains(.challengeData) == false {
            return .challengeData
        } else {
            return .connected
        }
    }
    
    internal var delegates: [PeripheralDelegate] {
        return rawDelegates.flatMap({ $0 as? PeripheralDelegate })
    }
    fileprivate var rawDelegates = WeakSet<AnyObject>()
    
    internal var connectionState: Connection = .unpaired {
        didSet {
            let state = connectionState
            switch connectionState {
            case .unpaired:
                securityStorage.removeAll()
                timeoutTimer?.invalidate()
                timeoutTimer = nil
            case .reconnecting:
                securityStorage.removeAll()
                timeoutTimer?.invalidate()
                timeoutTimer = nil
                reconnectTimer?.invalidate()
                DispatchQueue.main.async {
                    self.reconnectTimer = Timer.scheduledTimer(timeInterval: self.connectionTimeout, target: self, selector: #selector(self.reconnectCheck(timer: )), userInfo: nil, repeats: false)
                }
            case .paired:
                timeoutTimer?.invalidate()
                timeoutTimer = nil
                reconnectTimer?.invalidate()
                reconnectTimer = nil
                readFirmwareVersion(completion: {_ in})
            case .failed(_):
                isConnected = false
            case .connecting:
                reconnectTimer?.invalidate()
                timeoutTimer?.invalidate()
                DispatchQueue.main.async {
                    self.timeoutTimer = Timer.scheduledTimer(timeInterval: self.connectionTimeout, target: self, selector: #selector(self.timeoutCheck(timer: )), userInfo: nil, repeats: false)
                }
            case .flashingLED:
                break
            }
            DispatchQueue.main.async {
                self.delegates.forEach{$0.peripheral(self, didChangeConnection: state)}
            }
        }
    }
    
    public func requestMetadata() {
        do {
            try read(.hardwareInfo, for: .hardware)
        } catch {
            print(error)
        }
    }
    
    public func subscribe(delegate: PeripheralDelegate) {
        rawDelegates.insert(delegate)
        checkLockState()
    }
    
    @objc fileprivate func timeoutCheck(timer: Timer) {
        timeoutTimer?.invalidate()
        timeoutTimer = nil
        switch connectionState {
        case .connecting:
            connectionState = .failed(Error.connectionTimeout)
        default:
            break
        }
    }
    
    @objc fileprivate func reconnectCheck(timer: Timer) {
        reconnectTimer?.invalidate()
        reconnectTimer = nil
        switch connectionState {
        case .reconnecting:
            breakConection?(self)
            connectionState = .failed(Error.reconnectionTimeout)
        default:
            break
        }
    }
    
    public init?(_ periperal: CBPeripheral) {
        guard let name = periperal.name,
            let macId = periperal.macId else { return nil }
        self.peripheral = periperal
        self.name = name
        self.macId = macId
        self.isFactoryMode = name.contains("-")
        super.init()
        peripheral.delegate = self
        accelerometerHandler.onChange = { [unowned self] value in
            self.delegates.forEach{$0.peripheral(self, didUpdate: value)}
        }
        // FIXME: need to call ready() after all services and characteristics is discovered
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            self.ready()
        }
    }
    
    public func update(firmware: [UInt8], progress: @escaping (Double) -> ()) {
        updateState = .inProgress
        fwSize = Double(firmware.count)
        self.firmware = firmware
        self.updateProgress = progress
        updateFirmware()
    }
    
    public var firmware: [UInt8]? {
        didSet {
            if firmware == nil {
                fwSize = 0
            }
        }
    }
    
    /*!
     *  @method read(_ char:, completion: )
     *
     *  @discussion Method for read characteristic using completion block
     *
     *  @completion: (Data?) -> () Be carefull and weakyfy self always inside completion block by [weak self]
     */
    public func read(_ char: Characteristic, for service: Service = .configuration, completion: @escaping (Data?) -> ()) throws {
        if readers[char] == nil {
            readers[char] = [completion]
            try read(char, for: service)
        } else {
            readers[char]?.append(completion)
        }
    }
    
    public func set(lockState: LockState) throws {
        try write(bytes: [lockState.rawValue], to: .lock, for: .hardware)
    }
    
    public class func randomPin(to digits: Int) -> [Pin] {
        var code: [Pin] = []
        while digits > code.count {
            let rand = arc4random()%4
            code.append(Pin(int: rand))
        }
        return code
    }
    
    public func setRandomPin(to digits: Int) throws -> [Pin] {
        let code = Peripheral.randomPin(to: digits)
        try set(pinCode: code)
        return code
    }
    
    public func set(pinCode: [Pin]) throws {
        let max = 16
        let min = 4
        guard pinCode.count >= min && pinCode.count <= max else { throw Error.wrongPinCode(pinCode) }
        let empty = repeatElement(Byte.null.rawValue, count: max - pinCode.count)
        try write(bytes: pinCode.map({ $0.byteValue }) + empty, to: .buttonSequece, for: .configuration)
    }
    
    public func factoryReset() throws {
        try write(byte: .reset, to: .resetLock, for: .configuration)
    }
    
    public func flashLED() {
        let blinking: [UInt8] = [0xCF, 0xCF, 0x00, 0x01, 0x00]
        do {
            try write(bytes: blinking , to: .LED, for: .hardware)
        } catch {
            print(error)
        }
        DispatchQueue.global(qos: .default).asyncAfter(deadline: .now() + 6) { [weak self] in
            try? self?.write(byte: .null, to: .LED, for: .hardware)
            self?.flash?()
            self?.flash = nil
        }
    }
    
    public func arm(_ shouldArm: Bool) throws {
        let byte: Byte = shouldArm ? .arm : .null
        try write(byte: byte, to: .lock, for: .hardware)
    }
    
    fileprivate func setConnectionParams() {
        do {
            let params: [UInt8] = [txPower ?? Locale.current.txPowerValue, UInt8(durationAfterDrop/15), Byte.null.rawValue]
            try write(bytes: params, to: .connection, for: .hardware)
        } catch {
            print("Setting connection params error: \(error)")
        }
    }
    
    internal func connect() {
        connectionState = flash != nil ? .flashingLED : .connecting
        updateState = .none
        peripheral.discoverServices(Service.all.map{$0.uuid})
    }
}

internal extension Peripheral {
    internal func write(byte: Byte, to char: Characteristic, for service: Service) throws {
        try write(bytes: [byte.rawValue], to: char, for: service)
    }
    internal func write(bytes: [UInt8], to char: Characteristic, for service: Service) throws {
        try write(data: Data(bytes: bytes), to: char, for: service)
    }
    
    internal func write(data: Data, to char: Characteristic, for service: Service) throws {
        let responce = try characteristic(char: char, for: service)
        peripheral.writeValue(data, for: responce.characteristic, type: .withResponse)
    }
    
    internal func read(_ char: Characteristic, for service: Service) throws {
        let responce = try characteristic(char: char, for: service)
        peripheral.readValue(for: responce.characteristic)
    }
    
    internal func characteristic(char: Characteristic, for service: Service) throws -> (service: CBService, characteristic: CBCharacteristic) {
        guard let serv = peripheral.services?.filter({ $0.uuid == service.uuid }).first else { throw Error.missingService(service) }
        guard let characteristic = serv.characteristics?.filter({ $0.uuid == char.uuid }).first else { throw Error.missingCharacteristic(char) }
        return (serv, characteristic)
    }
    
    fileprivate func updateFirmware() {
        guard let firmware = firmware else { return }
        if firmware.isEmpty {
            self.firmware = nil
            updateState = .needReset
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
//        let bytes = Array(firmware[(firmware.count - limit - 1)..<(firmware.count - 1)])
        do {
            print(bytes)
            if peripheral.isEllboot {
                try write(bytes: bytes, to: .writeFirmware, for: .boot)
            } else {
                try write(bytes: bytes, to: .writeFirmware, for: .configuration)
            }
        } catch {
            print(error)
        }
        self.firmware = limit == firmware.count ? [] : Array(firmware[limit..<firmware.endIndex])
        
        let progress = 1 - (Double(firmware.count) / fwSize)
        DispatchQueue.main.async {self.updateProgress?(progress)}
    }
    
    fileprivate func checkLockState() {
        do {
            try read(.lock, for: .hardware)
        } catch {
            print(error)
        }
    }
    
    fileprivate var challengeKey: String? {
        guard var userId = userId?.md5 else {
            print("Error: userId is not defined for lock: \(macId)")
            connectionState = .failed(Error.missingUserId)
            return nil
        }
        while userId.characters.count < 64 {
            userId.append("f")
        }
        return userId.lowercased()
    }
    
    fileprivate func writePublicKey() {
        guard let publicKey = publicKey, let data = publicKey.dataValue else {
            return connectionState = .failed(Error.missingPublicKey)
        }
        do {
            try write(data: data, to: .publicKey, for: .security)
        } catch {
            print(error)
        }
    }
    
    fileprivate func writeChallengeKey() {
        guard let challengeKey = challengeKey, let data = challengeKey.dataValue  else {
            return
        }
        
        do {
            try write(data: data, to: .challengeKey, for: .security)
        } catch {
            print(error)
        }
    }
    
    
    fileprivate func writeSignedMessage() {
        guard let signedMessage = signedMessage, let data = signedMessage.dataValue  else {
            return connectionState = .failed(Error.missingSignedMessage)
        }
        
        do {
            try write(data: data, to: .signedMessage, for: .security)
        } catch {
            print(error)
        }
    }
    
    fileprivate func handleCommandStatus(data: Data?) {
        guard flash == nil else { return }
        guard let data = data else { return print("Error: command status nil value") }
        let values = [UInt8](data)
        guard let value = values.first, let byte = Byte(rawValue: value) else { return print("Error: wtong data for command status (\(values))") }
        print("Command status updated with value: \(byte)")
        
        switch byte {
        case .null where security == .challengeKey:
            writeChallengeKey()
        case .null where security == .signedMessage:
            writeSignedMessage()
        case .null where updateState == .needReset:
            reset()
        case .securityGuest, .securityOwner:
            try? read(.challengeData, for: .security)
        case .securityOwnerVerified, .securityGuestVerified:
            isConnected = true
            flashLED()
            setConnectionParams()
            checkLockState()
            connectionState = .paired
            DispatchQueue.main.async {
                self.updateProgress?(1)
                self.updateProgress = nil
            }
        case .accessDenied where security == .challengeKey:
            isFactoryMode = false
            writeSignedMessage()
        case .accessDenied:
            connectionState = .failed(Error.accessDenided)
        case .lockUnlockFailed:
            DispatchQueue.main.async {
                self.delegates.forEach{$0.peripheral(self, didChangeLock: .middle)}
            }
        default:
            print("Command status: no handler for value \(byte)")
        }
    }
    
    fileprivate func handleChallengeData(data: Data?) {
        guard let data = data, let challengeKey = challengeKey else { return }
        
        var challengeString = ""
        let values = [UInt8](data)
        for value in values {
            var byteString = String(value, radix: 16, uppercase: false)
            if byteString.characters.count == 1 {
                byteString = "0" + byteString
            }
            
            challengeString += byteString
        }
        
        challengeString = challengeKey + challengeString
        
        guard let result = challengeString.dataValue else {
            return
        }
        
        do {
            try write(data: result.sha256, to: .challengeData, for: .security)
        } catch {
            print(error)
        }
    }
    
    fileprivate func handleLock(data: Data?) {
        guard let data = data,
            let value = [UInt8](data).first,
            let state = LockState(rawValue: value) else { return }
        DispatchQueue.main.async {
            self.delegates.forEach{$0.peripheral(self, didChangeLock: state)}
        }
    }
    
    fileprivate func handleHardwareInfo(data: Data?) {
        guard let data = data else { return }
        let bytes = [UInt8](data)
        self.metadata = Metadata(bytes: bytes)
        guard let metadata = self.metadata else { return }
        if currentState != metadata.lockState {
            currentState = metadata.lockState
            DispatchQueue.main.async {
                self.delegates.forEach{$0.peripheral(self, didChangeLock: self.currentState)}
            }
        }
        delegates.forEach({$0.peripheral(self, didUpdate: metadata)})
    }
    
    fileprivate func handleMagnet(data: Data?) {
        guard let data = data else { return }
        let bytes = [UInt8](data)
        let coordinate = Coordinate(bytes: bytes, x: 0, y: 2, z: 4, count: 2)
        DispatchQueue.main.async {
            self.delegates.forEach{$0.peripheral(self, didUpdate: coordinate)}
        }
    }
    
    fileprivate func reset() {
        do {
            try write(byte: .null, to: .resetLock, for: .configuration)
        } catch {
            print(error)
        }
    }
}

extension Peripheral: CBPeripheralDelegate {
    public func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Swift.Error?) {
        guard let char = Characteristic(characteristic) else { return }
        
        var service: Service?
        switch char {
        case .publicKey:
            securityStorage.insert(.publicKey)
        case .challengeKey:
            securityStorage.insert(.challengeKey)
        case .signedMessage:
            securityStorage.insert(.signedMessage)
        case .challengeData:
            securityStorage.insert(.challengeData)
        case .LED:
            service = .hardware
        case .buttonSequece:
            service = .configuration
        case .writeFirmware:
            updateFirmware()
        case .lock:
            _ = try? read(.lock, for: .hardware)
        default:
            break
        }
        
        if let service = service {
            _ = try? read(char, for: service)
        }
        
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Swift.Error?) {
        guard let char = Characteristic(characteristic) else { return }
        if let chars = readers[char]  {
            DispatchQueue.main.async {
                chars.forEach({ $0(characteristic.value) })
            }
            readers[char] = nil
        }
        let data = characteristic.value
        switch char {
        case .challengeData:
            handleChallengeData(data: data)
        case .commandStatus:
            handleCommandStatus(data: data)
        case .lock:
            handleLock(data: data)
        case .accelerometer:
            accelerometerHandler.handle(data: data)
        case .hardwareInfo:
            handleHardwareInfo(data: data)
        case .magnet:
            handleMagnet(data: data)
        default:
            break
        }
        
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Swift.Error?) {
        guard flash == nil, let char = Characteristic(characteristic) else { return }
        switch char {
        case .commandStatus:
            writePublicKey()
        default:
            break
        }
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Swift.Error?) {
        if flash != nil, let service = Service(service.uuid), service == .hardware {
            flashLED()
            return
        }
        for characteristic in service.characteristics ?? [] {
            guard let char = Characteristic(characteristic), Characteristic.notify.contains(char) else { continue }
            peripheral.setNotifyValue(true, for: characteristic)
        }
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Swift.Error?) {
        let chars = Characteristic.all.map({ $0.uuid })
        peripheral.services?.forEach({ peripheral.discoverCharacteristics(chars, for: $0) })
    }
}

public extension Peripheral {
    public enum Service: String, UUIDCompatible {
        case security = "5E00"
        case hardware = "5E40"
        case configuration = "5E80"
        case test = "5EC0"
        case boot = "5D00"
        static let all: [Service] = [.security, .hardware, .configuration, .test, .boot]
    }
    
    public enum Characteristic: String, UUIDCompatible {
        case LED = "5E41"
        case lock = "5E42"
        case hardwareInfo = "5E43"
        case connection = "5E45"
        case magnet = "5E44"
        case accelerometer = "5E46"
        case signedMessage = "5E01"
        case publicKey = "5E02"
        case challengeKey = "5E03"
        case challengeData = "5E04"
        case firmwareVersion = "5D01"
        case writeFirmware = "5D02"
        case writeFirmwareNotification = "5D03"
        case firmwareUpdateDone = "5D04"
        case resetLock = "5E81"
        case serialNumber = "5E83"
        case buttonSequece = "5E84"
        case commandStatus = "5E05"
        
        static let all: [Characteristic] = [.LED, .lock, .hardwareInfo, .connection, .magnet, .accelerometer, .signedMessage, .publicKey, .challengeKey, .challengeData, .firmwareVersion, .writeFirmware, .writeFirmwareNotification, .firmwareUpdateDone, .resetLock, .serialNumber, .buttonSequece, .commandStatus]
        static let notify: [Characteristic] = [.magnet, .accelerometer, .commandStatus, .lock, .hardwareInfo, .writeFirmwareNotification]
    }
    
    public enum LockState: UInt8 {
        case unlocked = 0x00
        case locked = 0x01
        case middle = 0x04
        case invalid = 0x08
    }
    
    public enum Pin: String {
        case up, right, down, left
    }
    
    public enum Byte: UInt8 {
        case null = 0x00
        case reset = 0xBB
        case securityOwnerVerified = 0x04
        case securityGuest = 0x01
        case securityOwner = 0x02
        case securityGuestVerified = 0x03
        case invalidLenghtWriteIgnored = 0x80
        case accessDenied = 0x81
        case lockUnlockFailed = 0x82
        case invalidOffcet = 0x83
        case invalidWriteLenght = 0x84
        case invalidParameter = 0x85
        case commandInProgress = 0xFF
        
        // Duplicated values
        static let arm: Byte = .commandInProgress
    }
    
    public enum Connection {
        case paired, unpaired, connecting, reconnecting, flashingLED, failed(Swift.Error)
    }
    
    internal enum Security {
        case publicKey, challengeKey, challengeData, signedMessage, connected
    }
    
    internal enum UpdateState {
        case none, inProgress, needReset
    }
}

internal protocol UUIDCompatible {}
internal extension UUIDCompatible where Self: RawRepresentable, Self.RawValue == String {
    var uuid: CBUUID {
        return CBUUID(string: "D399\(self.rawValue)-FA57-11E4-AE59-0002A5D5C51B")
    }
    
    init?(_ uuid: CBUUID) {
        let string = uuid.uuidString
        let start = string.index(string.startIndex, offsetBy: 4)
        let end = string.index(start, offsetBy: 3)
        let str = string[start...end]
        self.init(rawValue: String(str))
    }
}

internal extension Peripheral.Characteristic {
    init?(_ characteristic: CBCharacteristic) {
        self.init(characteristic.uuid)
    }
}

public extension Peripheral.Pin {
    public var byteValue: UInt8 {
        switch self {
        case .up:
            return 0x01
        case .right:
            return 0x02
        case .down:
            return 0x04
        case .left:
            return 0x08
        }
    }
    
    init(int: UInt32) {
        switch int {
        case 0: self = .up
        case 1: self = .right
        case 2: self = .down
        default: self = .left
        }
    }
}

