//
//  UDStorage.swift
//  BLEService
//
//  Created by Ravil Khusainov on 6/21/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol Storage {
    var isTheftEnabled: Bool {get set}
    var isCrashEnabled: Bool {get set}
    var onTheftEnable: (Bool) -> () {get set}
    var onCrashEnable: (Bool) -> () {get set}
    var theftSensetivity: Float {get set}
    var crashSensetivity: Float {get set}
    var onTheftSensetivityChange: (Float) -> () {get set}
    var onCrashSensetivityChange: (Float) -> () {get set}
}

final class UDStorage: Storage {
    
    var onTheftEnable: (Bool) -> () = {_ in}
    var onCrashEnable: (Bool) -> () = {_ in}
    var onTheftSensetivityChange: (Float) -> () = {_ in}
    var onCrashSensetivityChange: (Float) -> () = {_ in}
    
    var isTheftEnabled: Bool {
        set {
            let value: Float? = newValue ? 0.5 : nil
            userDefaults.setValue(value, forKey: theftValueKey)
            userDefaults.synchronize()
            onTheftEnable(newValue)
        }
        get {
            return userDefaults.bool(forKey: theftValueKey)
        }
    }
    
    var isCrashEnabled: Bool {
        set {
            let value: Float? = newValue ? 0.5 : nil
            userDefaults.setValue(value, forKey: crashValueKey)
            userDefaults.synchronize()
            onCrashEnable(newValue)
        }
        get {
            return userDefaults.bool(forKey: crashValueKey)
        }
    }
    
    var theftSensetivity: Float {
        set {
            userDefaults.setValue(newValue, forKey: theftValueKey)
            userDefaults.synchronize()
            onTheftSensetivityChange(newValue)
        }
        get {
            return userDefaults.float(forKey: theftValueKey)
        }
    }
    
    var crashSensetivity: Float {
        set {
            userDefaults.setValue(newValue, forKey: crashValueKey)
            userDefaults.synchronize()
            onCrashSensetivityChange(newValue)
        }
        get {
            return userDefaults.float(forKey: crashValueKey)
        }
    }
    
    
    fileprivate let userDefaults = UserDefaults.standard
    fileprivate let theftValueKey = "theftValue"
    fileprivate let crashValueKey = "crashValue"
}
