//
//  AccelerometerHandler.swift
//  Ellipse
//
//  Created by Andre Green on 2/7/17.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import RestService


class Accelerometer {
    enum Value {
        case xValue
        case yValue
        case zValue
        case xStdDev
        case yStdDev
        case zStdDev
    }
    
    var maxPoints:Int {
        return 0
    }
    
    var points: [AccelerometerValue] = []
    
    func add(point: AccelerometerValue) {
        if maxPoints == 0 {
            return
        }
        
        if self.points.count < maxPoints || self.points.count == 0 {
            self.points.append(point)
            return
        }
        
        self.points.remove(at: 0)
        self.points.append(point)
    }
    
    func allStdDevs() -> [Value:Float] {
        if (self.points.count == 0 || self.points.count == 1) {
            return [.xValue: 0.0, .yValue: 0.0, .zValue: 0.0, .xStdDev: 0.0, .yStdDev: 0.0, .zStdDev: 0.0]
        }
        
        let averages = self.average()
        guard let xAve = averages[.xValue],
            let yAve = averages[.yValue],
            let zAve = averages[.zValue],
            let xStdAve = averages[.xStdDev],
            let yStdAve = averages[.yStdDev],
            let zStdAve = averages[.zStdDev] else
        {
            return [.xValue: 0.0, .yValue: 0.0, .zValue: 0.0, .xStdDev: 0.0, .yStdDev: 0.0, .zStdDev: 0.0]
        }
        
        var xDev:Float = 0.0
        var yDev:Float = 0.0
        var zDev:Float = 0.0
        var xStdDev:Float = 0.0
        var yStdDev:Float = 0.0
        var zStdDev:Float = 0.0
        
        for point in self.points {
            xDev += powf(point.x - xAve, 2.0)
            yDev += powf(point.y - yAve, 2.0)
            zDev += powf(point.z - zAve, 2.0)
            xStdDev += powf(point.xDev - xStdAve, 2.0)
            yStdDev += powf(point.yDev - yStdAve, 2.0)
            zStdDev += powf(point.zDev - zStdAve, 2.0)
        }
        
        let txValue =  stdDev(summedValue: xDev)
        let tyValue = stdDev(summedValue: yDev)
        let tzValue = stdDev(summedValue: zDev)
        let txStdDev = stdDev(summedValue: xStdDev)
        let tyStdDev = stdDev(summedValue: yStdDev)
        let tzStdDev = stdDev(summedValue: zStdDev)
        print(txValue, tyValue, tzValue, txStdDev, tyStdDev, tzStdDev)
        return [
            .xValue: stdDev(summedValue: xDev),
            .yValue: stdDev(summedValue: yDev),
            .zValue: stdDev(summedValue: zDev),
            .xStdDev: stdDev(summedValue: xStdDev),
            .yStdDev: stdDev(summedValue: yStdDev),
            .zStdDev: stdDev(summedValue: zStdDev)
        ]
    }
    
    func average() -> [Value:Float] {
        if (self.points.count == 0) {
            return [.xValue: 0.0, .yValue: 0.0, .zValue: 0.0, .xStdDev: 0.0, .yStdDev: 0.0, .zStdDev: 0.0]
        }
        
        var xAve:Float = 0.0
        var yAve:Float = 0.0
        var zAve:Float = 0.0
        var xStdAve:Float = 0.0
        var yStdAve:Float = 0.0
        var zStdAve:Float = 0.0
        
        for point in self.points {
            xAve += point.x
            yAve += point.y
            zAve += point.z
            xStdAve += point.xDev
            yStdAve += point.yDev
            zStdAve += point.zDev
        }
        
        
        return [
            .xValue: xAve/Float(self.points.count),
            .yValue: yAve/Float(self.points.count),
            .zValue: zAve/Float(self.points.count),
            .xStdDev: xStdAve/Float(self.points.count),
            .yStdDev: yStdAve/Float(self.points.count),
            .zStdDev: zStdAve/Float(self.points.count)
        ]
    }
    
    func stdDev(summedValue: Float) -> Float {
        if self.points.count == 0 || self.points.count == 1 {
            return 0.0
        }
        
        return sqrtf(summedValue/Float(self.points.count - 1))
    }
    
    func getLastAccValue() -> AccelerometerValue {
        guard let lastValue = self.points.last else {
            return AccelerometerValue(x: 0, y: 0, z: 0, xDev: 0, yDev: 0, zDev: 0)
        }
        
        return lastValue
    }
    
    // This method should be overridden in this class' children
    func shouldAlert() -> Bool {
        return false
    }
}
