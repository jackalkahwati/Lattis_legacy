//
//  CrashHandler.swift
//  Ellipse
//
//  Created by Andre Green on 2/8/17.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import Foundation

class CrashHandler: Accelerometer {
    override var maxPoints: Int {
        return 3
    }
    
    private let stdDevCutOff:Float = 323.0
    private let valueCuttOff:Float = 782.3
    
    override func shouldAlert() -> Bool {
        if points.count < maxPoints {
            return false
        }
        
        let stdDevs = allStdDevs()
        guard let xDev = stdDevs[.xValue], let yDev = stdDevs[.yValue], let zDev = stdDevs[.zValue] else {return false}
        
        let aves = average()
        guard let xAve = aves[.xValue], let yAve = aves[.yValue], let zAve = aves[.zValue] else {return false}
        
        print(xAve, yAve, zAve, xDev, yDev, zDev)
        if (check(value: xAve) || check(value: yAve) || check(value: zAve))
            && (checkStdDev(value: xDev) || checkStdDev(value: yDev) || checkStdDev(value: zDev))
        {
            print("Throwing crash alert with values: xAve: \(xAve) yAve: \(yAve) zAve: \(zAve) ")
            print("xDev: \(xDev), yDev: \(yDev), zDev: \(zDev)")
            points.removeAll()
            return true
        }
        
        return false
    }
    
    func checkStdDev(value: Float) -> Bool {
        return  value > stdDevCutOff
    }
    
    func check(value: Float) -> Bool {
        return value > valueCuttOff
    }
}
