//
//  Accelerometer.swift
//  BLEService
//
//  Created by Ravil Khusainov on 5/25/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

public struct Coordinate {
    public let x: Float
    public let y: Float
    public let z: Float
    
    public static let zero = Coordinate(x: 0, y: 0, z: 0)
}
public struct AccelerometerValue {
    public struct Limit {
        public let min: Float
        public let max: Float
        public var sensetivity: Float
        public var signalCount: Int
        
        var threshold: Float {
            return min + (max - min)*(1 - sensetivity)
        }
    }
    public let mav: Coordinate
    public let deviation: Coordinate
    public let sensitivity: Float
    
    public static let zero = AccelerometerValue(mav: .zero, deviation: .zero, sensitivity: 0)
}

public extension AccelerometerValue {
    init?(_ data: Data) {
        let bytes = [UInt8](data)
        guard bytes.count > 12 else { return nil }
        self.sensitivity = Float(bytes[12])
        self.mav = Coordinate(bytes: bytes, x: 0, y: 2, z: 4, count: 2)
        self.deviation = Coordinate(bytes: bytes, x: 6, y: 8, z: 10, count: 2)
    }
    
    func isSignal(threshold: Float) -> Bool {
        return deviation.x > threshold || deviation.y > threshold || deviation.z > threshold
    }
}

extension Coordinate {
    init(bytes: [UInt8], x: Int, y: Int, z: Int, count: Int) {
        self.x = convert(bytes: Array(bytes[x..<x+count]))
        self.y = convert(bytes: Array(bytes[y..<y+count]))
        self.z = convert(bytes: Array(bytes[z..<z+count]))
    }
}

public protocol TheftPresentable: NSObjectProtocol {
    func handleTheft(value: AccelerometerValue)
}

public protocol CrashPresentable: NSObjectProtocol {
    func handleCrash(value: AccelerometerValue)
}

public final class AccelerometerHandler {
    public var theftLimit = AccelerometerValue.Limit(min: 200, max: 1000, sensetivity: 0.5, signalCount: 2)
    public var crashLimit = AccelerometerValue.Limit(min: 200, max: 1000, sensetivity: 0.5, signalCount: 2)

    internal var onChange:((AccelerometerValue) -> ())?
    
    fileprivate var theftHandlers: [TheftPresentable] = []
    fileprivate var crashHandlers: [CrashPresentable] = []
    fileprivate var theftCounter: Int = 0
    fileprivate var crashCounter: Int = 0
    
    public func subscrybeTheft(handler: TheftPresentable) {
        guard theftHandlers.contains(where: { $0 === handler }) == false else { return }
        theftHandlers.append(handler)
    }
    
    public func unsubscrybeTheft(handler: TheftPresentable) {
        guard let idx = theftHandlers.index(where: { $0 === handler }) else { return }
        theftHandlers.remove(at: idx)
    }
    
    public func subscrybeCrash(handler: CrashPresentable) {
        guard crashHandlers.contains(where: { $0 === handler }) == false else { return }
        crashHandlers.append(handler)
    }
    
    func unsubscrybeCrash(handler: CrashPresentable) {
        guard let idx = crashHandlers.index(where: { $0 === handler }) else { return }
        crashHandlers.remove(at: idx)
    }
    
    func handle(data: Data?) {
        guard let data = data,
            let value = AccelerometerValue(data) else { return }
        
        onChange?(value)
        if value.isSignal(threshold: theftLimit.threshold), theftHandlers.isEmpty == false {
            theftCounter += 1
            if theftLimit.signalCount == theftCounter {
                theftCounter = 0
                theftHandlers.forEach({ $0.handleTheft(value: value) })
            }
        } else {
            theftCounter = 0
        }
        
        if value.isSignal(threshold: crashLimit.threshold), crashHandlers.isEmpty == false {
            crashCounter += 1
            if crashLimit.signalCount == crashCounter {
                crashCounter = 0
                crashHandlers.forEach({ $0.handleCrash(value: value) })
            }
        } else {
            crashCounter = 0
        }
    }
}
