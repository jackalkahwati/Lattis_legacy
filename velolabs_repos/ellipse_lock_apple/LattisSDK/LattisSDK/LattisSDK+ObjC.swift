//
//  LattisSDK+ObjC.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 21/03/2019.
//  Copyright © 2019 Lattis Inc. All rights reserved.
//

import Foundation

@objc public enum LSEllipseConnection: Int {
    case paired
    case unpaired
    case connecting
    case reconnecting
    case flashingLED
    case manageCapTouch
    case failed
    case updating
    case restored
    case ready
    
    init(_ connection: Ellipse.Connection) {
        switch connection {
        case .connecting:
            self = .connecting
        case .paired:
            self = .paired
        case .unpaired:
            self = .unpaired
        case .reconnecting:
            self = .reconnecting
        case .flashingLED:
            self = .flashingLED
        case .manageCapTouch:
            self = .manageCapTouch
        case .failed:
            self = .failed
        case .updating:
            self = .updating
        case .restored:
            self = .restored
        case .ready:
            self = .ready
        }
    }
}

@objc public enum LSEllipseSecurity: Int {
    case unlocked
    case locked
    case middle
    case invalid
    case auto
    
    init(_ security: Ellipse.Security) {
        switch security {
        case .auto:
            self = .auto
        case .invalid:
            self = .invalid
        case .middle:
            self = .middle
        case .locked:
            self = .locked
        case .unlocked:
            self = .unlocked
        }
    }
}

@objc public enum LSEllipseValue: Int {
    case firmwareVersion
    case serialNumber
    case capTouchEnabled
    case shackleInserted
    case magnetAutoLockEnabled
    case batteryLevel
}


private final class LSEllipseManager {
    static let shared = LSEllipseManager()
    var ellipses: [LSEllipse] = []
    var handlers: [FakeManager] = []
    
    func ellips(for handler: LSEllipseDelegate) -> LSEllipse {
        ellipses = ellipses.filter{$0.delegate != nil}
        if let ellipse = ellipses.filter({$0.delegate! === handler}).first {
            return ellipse
        }
        let ellipse = LSEllipse(handler)
        ellipses.append(ellipse)
        return ellipse
    }
    
    func handler(for delegate: LSEllipseManagerDelegate) -> FakeManager {
        handlers = handlers.filter{$0.delegate != nil}
        if let handler = handlers.filter({$0.delegate === delegate}).first {
            return handler
        }
        let handler = FakeManager(delegate)
        handlers.append(handler)
        return handler
    }
}

private final class LSEllipse: EllipseDelegate {
    weak var delegate: LSEllipseDelegate?
    
    init(_ delegate: LSEllipseDelegate?) {
        self.delegate = delegate
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate security: Ellipse.Security) {
        delegate?.ellipse(ellipse, didUpdate: .init(security))
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate connection: Ellipse.Connection) {
        let error: Error?
        switch connection {
        case .failed(let err):
            error = err
        default:
            error = nil
        }
        delegate?.ellipse(ellipse, didUpdate: .init(connection), error: error)
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate value: Ellipse.Value) {
        switch value {
        case .firmwareVersion(let version):
            delegate?.ellipse(ellipse, didUpdate: version, with: .firmwareVersion)
        case .serialNumber(let serial):
            delegate?.ellipse(ellipse, didUpdate: serial, with: .serialNumber)
        case .capTouchEnabled(let isEnabled):
            delegate?.ellipse(ellipse, didUpdate: isEnabled, with: .capTouchEnabled)
        case .shackleInserted(let isInserted):
            delegate?.ellipse(ellipse, didUpdate: isInserted, with: .shackleInserted)
        case .magnetAutoLockEnabled(let isEnabled):
            delegate?.ellipse(ellipse, didUpdate: isEnabled, with: .magnetAutoLockEnabled)
        case .metadata(let metadata):
            delegate?.ellipse(ellipse, didUpdate: metadata.batteryLevel, with: .batteryLevel)
        default:
            break;
        }
    }
}

private final class FakeManager: EllipseManagerDelegate {
    weak var delegate: LSEllipseManagerDelegate?
    
    init(_ delegate: LSEllipseManagerDelegate) {
        EllipseManager.shared.cashingStrategy = .never
        self.delegate = delegate
    }
    
    func manager(_ lockManager: EllipseManager, didRestoreConnected locks: [Ellipse]) {
        delegate?.manager(lockManager, didRestoreConnected: locks)
    }
    
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Ellipse], delete: [Ellipse]) {
        delegate?.manager(lockManager, didUpdateLocks: insert, delete: delete)
    }
    
    func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool) {
        delegate?.manager(lockManager, didUpdateConnectionState: connected)
    }
}

@objc public protocol LSEllipseManagerDelegate: class {
    func manager(_ lockManager: EllipseManager, didRestoreConnected locks: [Ellipse])
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Ellipse], delete: [Ellipse])
    func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool)
}

@objc public protocol LSEllipseDelegate: class {
    func ellipse(_ ellipse: Ellipse, didUpdate connection: LSEllipseConnection, error: Error?)
    func ellipse(_ ellipse: Ellipse, didUpdate security: LSEllipseSecurity)
    func ellipse(_ ellipse: Ellipse, didUpdate value: Any, with valueType: LSEllipseValue)
}

public extension Ellipse {
    func connect(handler: LSEllipseDelegate) {
        let ellipse = LSEllipseManager.shared.ellips(for: handler)
        connect(handler: ellipse)
    }
    
    func subscribe(_ handler: LSEllipseDelegate) {
        let ellipse = LSEllipseManager.shared.ellips(for: handler)
        subscribe(ellipse)
    }
    
    func unsubscribe(_ handler: LSEllipseDelegate) {
        let ellipse = LSEllipseManager.shared.ellips(for: handler)
        unsubscribe(ellipse)
    }
    
    var connectionState: LSEllipseConnection {
        return .init(connection)
    }
    
    var securityState: LSEllipseSecurity {
        return .init(security)
    }
    
    var objcIsCapTouchEnabled: Bool {
        get {
            return isCapTouchEnabled ?? false
        }
        set {
            isCapTouchEnabled = newValue
        }
    }
    
    var objcIsMagnetAutoLockEnabled: Bool {
        get {
            return isMagnetAutoLockEnabled ?? false
        }
        set {
            isMagnetAutoLockEnabled = newValue
        }
    }
    
    var objcExtraOpenTime: Bool {
        get {
            return extendedOpenTime ?? false
        }
        set {
            extendedOpenTime = newValue
        }
    }
    
    var objcSecurity: LSEllipseSecurity {
        return .init(security)
    }
}

public extension EllipseManager {
    func scan(with handler: LSEllipseManagerDelegate) {
        scan(with: LSEllipseManager.shared.handler(for: handler))
    }
    
    func startScan() {
        scan()
    }
    
    func subscribe(delegate: LSEllipseManagerDelegate) {
        subscribe(handler: LSEllipseManager.shared.handler(for: delegate))
    }
}
