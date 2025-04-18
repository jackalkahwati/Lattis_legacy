//
//  Accelerometer.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 8/27/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import Foundation

public final class Accelerometer {
    // Used for testing only
//    public var theftLimit = Accelerometer.Value.Limit(min: 200, max: 1000, sensetivity: 0.5, signalCount: 2)
//    public var crashLimit = Accelerometer.Value.Limit(min: 200, max: 1000, sensetivity: 0.5, signalCount: 2)
    public var theftLimit = Accelerometer.Sensetivity.medium
    public var crashLimit = Accelerometer.Sensetivity(threshold: 800, signalCount: 1)
    
    public fileprivate(set) var value: Value = .zero
    
    internal var onUpdate:((Accelerometer.Value) -> ())?
    
    fileprivate var theftHandlers = WeakCollection<TheftPresentable>()
    fileprivate var crashHandlers = WeakCollection<CrashPresentable>()
    fileprivate var theftCounter: Int = 0
    fileprivate var crashCounter: Int = 0
    
    public func subscribeTheft(handler: TheftPresentable) {
        guard theftHandlers.contains(where: { $0 === handler }) == false else { return }
        theftHandlers.insert(handler)
    }
    
    public func unsubscribeTheft(handler: TheftPresentable) {
        theftHandlers.delete(handler)
    }
    
    public func subscribeCrash(handler: CrashPresentable) {
        guard crashHandlers.contains(where: { $0 === handler }) == false else { return }
        crashHandlers.insert(handler)
    }
    
    public func unsubscribeCrash(handler: CrashPresentable) {
        crashHandlers.delete(handler)
    }
    
    func handle(data: Data?, ellipse: Ellipse) {
        guard let data = data,
            let value = Accelerometer.Value(data) else { return }
        self.value = value
        onUpdate?(value)
        if value.isSignal(threshold: theftLimit.threshold), theftHandlers.isEmpty == false {
            theftCounter += 1
            if theftLimit.signalCount == theftCounter {
                theftCounter = 0
                safe(self.theftHandlers) {$0.handleTheft(value: value, for: ellipse)}
            }
        } else {
            theftCounter = 0
        }
        
        if value.isSignal(threshold: crashLimit.threshold), crashHandlers.isEmpty == false {
            crashCounter += 1
            if crashLimit.signalCount == crashCounter {
                crashCounter = 0
                safe(self.crashHandlers) {$0.handleCrash(value: value, for: ellipse)}
            }
        } else {
            crashCounter = 0
        }
    }
}

public extension Accelerometer {
    struct Coordinate {
        public let x: Float
        public let y: Float
        public let z: Float
        
        public static let zero = Coordinate(x: 0, y: 0, z: 0)
    }
    
    struct Value {
        public struct Limit {
            public let min: Float
            public let max: Float
            public var sensetivity: Float
            public var signalCount: Int
            
            public var threshold: Float {
                return min + (max - min)*(1 - sensetivity)
            }
        }
        public let mav: Coordinate
        public let deviation: Coordinate
        public let sensitivity: Float
        
        public static let zero = Value(mav: .zero, deviation: .zero, sensitivity: 0)
    }
    
    struct Sensetivity {
        internal let threshold: Float
        internal let signalCount: Int
        
        public static let low = Sensetivity(threshold: 400, signalCount: 5)
        public static let medium = Sensetivity(threshold: 400, signalCount: 1)
        public static let high = Sensetivity(threshold: 200, signalCount: 1)
    }
}

public extension Accelerometer.Value {
    init?(_ data: Data) {
        let bytes = [UInt8](data)
        guard bytes.count > 12 else { return nil }
        self.sensitivity = Float(bytes[12])
        self.mav = Accelerometer.Coordinate(bytes: bytes, x: 0, y: 2, z: 4, count: 2)
        self.deviation = Accelerometer.Coordinate(bytes: bytes, x: 6, y: 8, z: 10, count: 2)
    }
    
    func isSignal(threshold: Float) -> Bool {
        return deviation.x > threshold || deviation.y > threshold || deviation.z > threshold
    }
}

extension Accelerometer.Coordinate {
    init(bytes: [UInt8], x: Int, y: Int, z: Int, count: Int) {
        self.x = convert(bytes: Array(bytes[x..<x+count]))
        self.y = convert(bytes: Array(bytes[y..<y+count]))
        self.z = convert(bytes: Array(bytes[z..<z+count]))
    }
}

public protocol TheftPresentable: NSObjectProtocol {
    func handleTheft(value: Accelerometer.Value, for ellipse: Ellipse)
}

public protocol CrashPresentable: NSObjectProtocol {
    func handleCrash(value: Accelerometer.Value, for ellipse: Ellipse)
}

extension Accelerometer.Sensetivity: Equatable {
}

public func ==(lhs: Accelerometer.Sensetivity, rhs: Accelerometer.Sensetivity) -> Bool {
    return lhs.signalCount == rhs.signalCount && rhs.threshold == lhs.threshold
}

public extension Accelerometer.Sensetivity {
    init(_ value: Int) {
        let stat: Accelerometer.Sensetivity
        switch value {
        case 0:
            stat = .low
        case 1:
            stat = .medium
        default:
            stat = .high
        }
        self.init(threshold: stat.threshold, signalCount: stat.signalCount)
    }
}
