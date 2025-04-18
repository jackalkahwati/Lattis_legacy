//
//  Ellipse.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/17/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import CoreLocation

private let ellipseIsCurrentKey = "ellipseIsCurrentKey"

struct Ellipse: Codable {
    let lockId: Int
    let macId: String
    let userId: Int
    var name: String?
    var shareId: Int?
    var sharedToUserId: Int?
    var pinCode: String?
    
    // Local
    var connectedAt: Date? = nil
    var owner: User? = nil
    var borrower: User? = nil
    var lockState: LockState? = nil
    var stateChangedAt: Date? = nil
    var isAutoLockEnabled: Bool = false
    var isAutoUnlockEnabled: Bool = false
    var sensorSensitivity: Sensetivity = .medium
    var isCrashEnabled: Bool = false
    var isTheftEnabled: Bool = false
    var source: Source = .network
    private var latitude: CLLocationDegrees = -180
    private var longitude: CLLocationDegrees = -180
}

extension Ellipse {
    enum LockState: String, Codable {
        case locked, unlocked
    }
    enum Pin: String, Codable {
        case up, down, left, right
    }
    enum Source {
        case network, storage
    }
    
    enum Sensetivity: Int, Codable {
        case low, medium, high
    }
    
    var isCurrent: Bool {
        set {
            if newValue {
                UserDefaults.standard.set(lockId, forKey: ellipseIsCurrentKey)
            } else {
                UserDefaults.standard.removeObject(forKey: ellipseIsCurrentKey)
            }
            UserDefaults.standard.synchronize()
        }
        get {
            return UserDefaults.standard.integer(forKey: ellipseIsCurrentKey) == lockId
        }
    }
    
    var coordinate: CLLocationCoordinate2D {
        get {
            return CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
        }
        set {
            latitude = newValue.latitude
            longitude = newValue.longitude
        }
    }
    
    var pin: [Pin]? {
        get {
            return pinCode?.toPin
        }
        set {
            if let value = newValue {
                pinCode = String(value)
            } else {
                pinCode = nil
            }
        }
    }
    
    static func removeCurrent() {
        UserDefaults.standard.removeObject(forKey: ellipseIsCurrentKey)
        UserDefaults.standard.synchronize()
    }
}

extension Ellipse: Equatable {}

func == (lhs: Ellipse, rhs: Ellipse) -> Bool {
    return lhs.lockId == rhs.lockId
        && lhs.name == rhs.name
        && lhs.pin == rhs.pin
}

extension Ellipse {
    enum CodingKeys: String, CodingKey {
        case lockId
        case macId
        case userId
        case name
        case shareId
        case sharedToUserId
        case pinCode
    }
}

extension Ellipse.Sensetivity {
    init(_ value: Int32) {
        if let s = Ellipse.Sensetivity(rawValue: Int(value)) {
            self = s
        } else {
            self = .medium
        }
    }
    
    var stringValue: String {
        return "\(self)"
    }
    
    var note: String {
        switch self {
        case .low:
            return "lock_sensetivity_description_low".localized()
        case .medium:
            return "lock_sensetivity_description_medium".localized()
        case .high:
            return "lock_sensetivity_description_high".localized()
        }
    }
}

