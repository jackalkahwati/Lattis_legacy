//
//  Protocols.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 8/1/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import Foundation

public protocol EllipseManagerDelegate: class {
    func manager(_ lockManager: EllipseManager, didRestoreConnected locks: [Ellipse])
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Ellipse], delete: [Ellipse])
    func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool)
}

public extension EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didRestoreConnected locks: [Ellipse]) {}
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Ellipse], delete: [Ellipse]) {}
    func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool) {}
}

public extension Ellipse {
    enum Value {
        case firmwareVersion(String)
        case serialNumber(String)
        case metadata(Metadata)
        case magnet(Accelerometer.Coordinate)
        case accelerometer(Accelerometer.Value)
        case capTouchEnabled(Bool)
        case shackleInserted(Bool)
        case magnetAutoLockEnabled(Bool)
    }
}

public protocol EllipseDelegate: class {
    func ellipse(_ ellipse: Ellipse, didUpdate connection: Ellipse.Connection)
    func ellipse(_ ellipse: Ellipse, didUpdate security: Ellipse.Security)
    func ellipse(_ ellipse: Ellipse, didUpdate value: Ellipse.Value)
}

public extension EllipseDelegate {
    func ellipse(_ ellipse: Ellipse, didUpdate value: Ellipse.Value) {}
}


public protocol Network {
    func sign(lockWith macId: String, completion: @escaping (Result<(String, String), Error>) -> ())
    func firmvareVersions(completion: @escaping (Result<[String], Error>) -> ())
    func firmvare(version: String?, completion: @escaping (Result<[UInt8], Error>) -> ())
    func firmvareChangeLog(for version: String?, completion: @escaping (Result<[String], Error>) -> ())
    func save(pinCode: [String], forLock macId: String, completion: @escaping (Result<Void, Error>) -> ())
    func getPinCode(forLockWith macId: String, completion: @escaping (Result<[String], Error>) -> ())
}
