//
//  SLLock+CoreDataClass.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 23/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//  This file was automatically generated and should not be edited.
//

import Foundation
import CoreData
import RestService

public class SLLock: NSManagedObject {
    var displayName: String {
        var name = givenName
        if name == nil || name!.isEmpty {
            name = self.name
        }
        if (name == nil || name!.isEmpty)  && owner?.userId != user?.userId {
            name = String(format: NSLocalizedString("Shared by %@", comment: ""), owner?.fullName ?? owner!.usersId!)
        }
        return name ?? ""
    }
    
    var isInFactoryMode: Bool {
        return name?.contains("-") ?? false
    }
    
    var location: CLLocationCoordinate2D {
        set {
            if CLLocationCoordinate2DIsValid(newValue) == false {
                latitude = nil
                longitude = nil
                return
            }
            latitude = NSNumber(value: newValue.latitude)
            longitude = NSNumber(value: newValue.longitude)
        }
        get {
            guard let lat = latitude?.doubleValue, let lng = longitude?.doubleValue else { return kCLLocationCoordinate2DInvalid }
            return CLLocationCoordinate2D(latitude: lat, longitude: lng)
        }
    }
    
    func switchNameToProvisioned() {
        guard let parts = name?.components(separatedBy: "-"),
            isInFactoryMode && parts.count > 1  else { return }
        
        name = "\(parts[0]) \(parts[1])"
    }
    
    func range(forParameter type: SLLockParameterType) -> SLLockParameterRange {
        var range: SLLockParameterRange!
        if type == .battery {
            guard let batteryVoltage = batteryVoltage else { return .zero }
            if batteryVoltage > 3175 {
                range = .four
            } else if batteryVoltage > 3050 {
                range = .three
            } else if batteryVoltage > 2925 {
                range = .two
            } else if batteryVoltage > 2800 {
                range = .one
            } else {
                range = .zero
            }
        } else {
            guard let rssiStrength = rssiStrength else { return .zero }
            if rssiStrength > -62.5 {
                range = .four
            } else if rssiStrength > -75.0 {
                range = .three
            } else if rssiStrength > -82.5 {
                range = .two
            } else if rssiStrength > -100.0 {
                range = .one
            } else {
                range = .zero
            }
        }
        
        return range
    }

    var temperature: Int8?
    var batteryVoltage: Int16?
    var wifiStrength: Int16?
    var cellStrength: Int16?
    var distanceAway: Int = 0
    var rssiStrength: Float?
    var isShallowConnection: Bool = false
}

enum SLLockPosition: UInt8 {
    case unlocked, locked, middle, invalid
}

enum SLLockParameterRange: Int {
    case zero, one, two, three, four
}

enum SLLockParameterType {
    case battery, rssi
}

extension SLLock {
    func fill(with serverLock: Oval.Locks.Responce) {
        lockId = serverLock.lockId
        macId = serverLock.macId
        if let shareId = serverLock.shareId {
            self.shareId = shareId
        }
        if let givenName = serverLock.name, givenName.isEmpty == false {
            self.givenName = givenName
        }
    }
}
