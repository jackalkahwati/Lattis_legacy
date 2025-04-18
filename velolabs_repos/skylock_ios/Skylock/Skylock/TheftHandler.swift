//
//  TheftHandler.swift
//  Ellipse
//
//  Created by Andre Green on 2/7/17.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import Foundation

class TheftHandler: Accelerometer {
    override var maxPoints:Int {
        return 20
    }
    
    private var sensitivity:Float
    
    private let cutOff:Float = 195.73
    
    init(sensitivity: Float) {
        self.sensitivity = sensitivity
    }
    
    func set(sensitivity: Float) {
        self.sensitivity = sensitivity
    }
    
    override func shouldAlert() -> Bool {
        if points.count < maxPoints {
            return false
        }
        
        let stdDevs = allStdDevs()
        guard let x = stdDevs[.xValue], let y = stdDevs[.yValue], let z = stdDevs[.zValue] else {
            return false
        }
        
        if checkAxis(value: x) || checkAxis(value: y) || checkAxis(value: z) {
            print("Throwing theft alert with values: x: \(x) y: \(y) z: \(z)")
            points.removeAll()
            return true
        }
        
        return false
    }
    
    func checkAxis(value: Float) -> Bool {
        return value - 10.0 * (sensitivity - 0.5) > cutOff
    }
}
