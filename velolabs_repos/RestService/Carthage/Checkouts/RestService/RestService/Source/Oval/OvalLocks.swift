//
//  OvalLocks.swift
//  OvalApi
//
//  Created by Ravil Khusainov on 20/01/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import SwiftyJSON
import CoreLocation

public extension Oval {
    public static let locks = Locks()
    public class Locks: Oval.Route {
        internal let basePath = "locks/"
        
        public func registration(with macId: String, success: @escaping (Responce) -> (), fail: @escaping fail)  {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let reg = Oval.post(path: basePath + "registration/", post: ["mac_id": macId], restToken: token)
            let retry = { self.registration(with: macId, success: success, fail: fail) }
            perform(resource: reg, parse: Responce.init, retry: retry, fail: fail, mapped: success)
        }
        
        public func update(lock: Request, success: @escaping (Responce) -> (), fail: @escaping fail)  {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let reg = Oval.post(path: basePath + "update-lock/", post: lock.params, restToken: token)
            let retry = { self.update(lock: lock, success: success, fail: fail) }
            perform(resource: reg, parse: Responce.init, retry: retry, fail: fail, mapped: success)
        }
        
        public func locks(success: @escaping (Groups) -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let parse: (JSON) -> Groups? = { json in
                guard let my = json["user_locks"].array,
                    let sharedActive = json["shared_locks"]["by_user"]["active"].array,
                    let sharedInactive = json["shared_locks"]["by_user"]["inactive"].array,
                    let borrowed = json["shared_locks"]["to_user"].array else { return nil }
                return Groups(my: my.flatMap({ Responce($0) }),
                              shared: (sharedActive.flatMap({ Responce($0) }), sharedInactive.flatMap({ Responce($0) })),
                              borrowed: borrowed.flatMap({ Responce($0) }))
            }
            let request = Oval.get(path: basePath + "users-locks/", restToken: token)
            let retry = { self.locks(success: success, fail: fail) }
            perform(resource: request, parse: parse, retry: retry, fail: fail, mapped: success)
        }
        
        public func signLock(with macId: String, success: @escaping (String, String) -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let parse: (JSON) -> (String, String)? = { payload in
                guard let signedMessage = payload["signed_message"].string,
                    let publicKey = payload["public_key"].string else { return nil }
                return (signedMessage, publicKey)
            }
            let request = Oval.post(path: basePath + "signed-message-and-public-key/", post: ["mac_id": macId], restToken: token)
            let retry = { self.signLock(with: macId, success: success, fail: fail) }
            perform(resource: request, parse: parse, retry: retry, fail: fail, mapped: success)
        }
        
        public func firmvare(version: String? = nil, success: @escaping ([String]) -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            var params: [String: Any] = [:]
            if let ver = version {
                params["version"] = ver
            }
            let request = Oval.post(path: basePath + "firmware/", post: params, restToken: token)
            let retry = { self.firmvare(version: version, success: success, fail: fail) }
            let parse: (JSON) -> [String]? = { $0.array?.flatMap({ $0.string }) }
            perform(resource: request, parse: parse, retry: retry, fail: fail, mapped: success)
        }
        
        public func firmvareVersions(success: @escaping ([String]) -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.get(path: basePath + "firmware-versions/", restToken: token)
            let parse: (JSON) -> [String]? = { $0.arrayValue.flatMap({ $0.string }) }
            let retry = { self.firmvareVersions(success: success, fail: fail) }
            perform(resource: request, parse: parse, retry: retry, fail: fail, mapped: success)
        }
        
        public func firmvareChangeLog(for version: String? = nil, success: @escaping ([String]) -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            var params: [String: Any] = [:]
            if let ver = version {
                params["version"] = ver
            }
            let request = Oval.post(path: basePath + "firmware-log/", post: params, restToken: token)
            let parse: (JSON) -> [String]? = { $0.arrayValue.flatMap({ $0.string }) }
            let retry = { self.firmvareChangeLog(for: version, success: success, fail: fail) }
            perform(resource: request, parse: parse, retry: retry, fail: fail, mapped: success)
        }
        
        public func save(pinCode: [Pin], forLock macId: String, success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "save-pin-code/", post: ["mac_id": macId, "pin_code": pinCode.map({ $0.rawValue })], restToken: token)
            let retry =  { self.save(pinCode: pinCode, forLock: macId, success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
        
        public func delete(lock macId: String, success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "delete-lock/", post: ["mac_id": macId], restToken: token)
            let retry = { self.delete(lock: macId, success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
        
        public func crashDetected(info: CrashInfo, success: @escaping (Crash) -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "crash-detected/", post: info.params, restToken: token)
            let retry = { self.crashDetected(info: info, success: success, fail: fail) }
            perform(resource: request, parse: Crash.init, retry: retry, fail: fail, mapped: success)
        }
        
        public func send(emergency message: EmergencyMessage, success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "send-emergency-message/", post: message.params, restToken: token)
            let retry = { self.send(emergency: message, success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
        
        public func theftDetected(info: CrashInfo, success: @escaping (Crash) -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "theft-detected/", post: info.params, restToken: token)
            let retry = { self.theftDetected(info: info, success: success, fail: fail) }
            perform(resource: request, parse: Crash.init, retry: retry, fail: fail, mapped: success)
        }
        
        public func confirm(theft theftId: Int, isConfirmed: Bool, success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "confirm-theft/", post: ["theft_id": theftId, "is_confirmed": isConfirmed], restToken: token)
            let retry = { self.confirm(theft: theftId, isConfirmed: isConfirmed, success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
        
        public func share(lock lockId: Int32, to contact: Contact, success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "share/", post: ["lock_id": lockId, "contact": contact.params], restToken: token)
            let retry = { self.share(lock: lockId, to: contact, success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
        
        public func acceptSharing(confirmationCode: String, success: @escaping (String) -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "share-confirmation/", post: ["confirmation_code": confirmationCode], restToken: token)
            let retry = { self.acceptSharing(confirmationCode: confirmationCode, success: success, fail: fail) }
            let parse: (JSON) -> String? = { $0["mac_id"].string }
            perform(resource: request, parse: parse, retry: retry, fail: fail, mapped: success)
        }
        
        public func revoke(sharing shareId: Int32, fromUser userId: Int32, success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "revoke-sharing/", post: ["share_id": shareId, "shared_to_user_id": userId], restToken: token)
            let retry = { self.revoke(sharing: shareId, fromUser: userId, success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
    }
}

public extension Oval.Locks {
    public struct Request {
        public let lockId: Int32
        public var name: String?
        public init(lockId: Int32, name: String?) {
            self.lockId = lockId
            self.name = name
        }
    }
    
    public struct Responce {
        public let macId: String
        public let userId: Int32
        public let usersId: String?
        public let lockId: Int32
        public let name: String?
        public let publicKey: String
        public let sharedToUserId: Int32?
        public let shareId: Int32?
    }
    
    public struct Contact {
        public let firstName: String?
        public let lastName: String?
        public let phoneNumber: String
        public let countryCode: String?
        public init(firstName: String? = nil, lastName: String? = nil, phoneNumber: String, countryCode: String?) {
            self.firstName = firstName
            self.lastName = lastName
            self.phoneNumber = phoneNumber
            self.countryCode = countryCode
        }
    }
    
    public struct EmergencyMessage {
        public var crashId: Int
        public let contacts: [Contact]
        public let macId: String
        public let location: CLLocationCoordinate2D
        public init(crashId: Int = 0, contacts: [Contact], macId: String, location: CLLocationCoordinate2D) {
            self.crashId = crashId
            self.contacts = contacts
            self.macId = macId
            self.location = location
        }
    }
    
    public struct Groups {
        public let my: [Responce]
        public let shared: (active: [Responce], inactive: [Responce])
        public let borrowed: [Responce]
        public var all: [Responce] {
            return my + shared.active + shared.inactive + borrowed
        }
    }
    
    public enum Pin: String {
        case up, right, down, left
    }
    
    public struct CrashInfo {
        public let macId: String
        public let accelerometerValue: AccelerometerValue
        public let location: CLLocationCoordinate2D
        
        public init(macId: String, accelerometerValue: AccelerometerValue, location: CLLocationCoordinate2D) {
            self.macId = macId
            self.accelerometerValue = accelerometerValue
            self.location = location
        }
    }
    
    public struct Crash {
        public let crashId: Int
        public let messageSent: Bool
        public let lockId: Int
        public let userId: Int
        public let accelerometerValue: AccelerometerValue
        public let date: Date
        
        public init(crashId: Int, messageSent: Bool, lockId: Int, userId: Int, accelerometerValue: AccelerometerValue, date: Date) {
            self.crashId = crashId
            self.messageSent = messageSent
            self.lockId = lockId
            self.userId = userId
            self.accelerometerValue = accelerometerValue
            self.date = date
        }
    }
}

public extension Oval.Locks.Pin {
    public var byteValue: UInt8 {
        switch self {
        case .up:
            return 0x01
        case .right:
            return 0x02
        case .down:
            return 0x04
        case .left:
            return 0x08
        }
    }
}

private extension Oval.Locks.Request {
    var params: [String: Any] {
        var dict: [String: Any] = ["lock_id": lockId]
        if let name = name {
            dict["name"] = name
        }
        return ["properties": dict]
    }
}

private extension Oval.Locks.Responce {
    init?(_ json: JSON) {
        guard let macId = json["mac_id"].string,
            let userId = json["user_id"].int32,
            let lockId = json["lock_id"].int32,
            let publicKey = json["public_key"].string else { return nil }
        self.macId = macId
        self.userId = userId
        self.lockId = lockId
        self.publicKey = publicKey
        self.name = json["name"].string
        self.sharedToUserId = json["shared_to_user_id"].int32
        self.usersId = json["users_id"].string
        self.shareId = json["share_id"].int32
    }
}

private extension Oval.Locks.Crash {
     init?(_ json: JSON) {
        guard let crashId = json["crash_id"].int,
            let messageSent = json["message_sent"].int,
            let lockId = json["lock_id"].int,
            let userId = json["user_id"].int,
            let xAve = json["x_ave"].float,
            let yAve = json["y_ave"].float,
            let zAve = json["z_ave"].float,
            let xDev = json["x_dev"].float,
            let yDev = json["y_dev"].float,
            let zDev = json["z_dev"].float,
            let timestamp = json["date"].double else { return nil }
        self.crashId = crashId
        self.userId = userId
        self.lockId = lockId
        self.messageSent = messageSent != 0
        self.accelerometerValue = AccelerometerValue(
            x:xAve,
            y: yAve,
            z: zAve,
            xDev: xDev,
            yDev: yDev,
            zDev: zDev
        )
        self.date = Date(timeIntervalSince1970: timestamp)
    }
}

public struct AccelerometerValue {
    public var x:Float
    public var y:Float
    public var z:Float
    public var xDev:Float
    public var yDev:Float
    public var zDev:Float
    
    public init(x:Float, y:Float, z:Float, xDev:Float, yDev:Float, zDev:Float) {
        self.x = x
        self.y = y
        self.z = z
        self.xDev = xDev
        self.yDev = yDev
        self.zDev = zDev
    }
    
    public func params() -> [String: Any] {
        return ["x_ave": x, "y_ave": y, "z_ave": z, "x_dev": xDev, "y_dev": yDev, "z_dev": zDev]
    }
}

private extension Oval.Locks.CrashInfo {
    var params: [String: Any] {
        return ["mac_id": macId,
                "accelerometer_data": accelerometerValue.params(),
                "location": location.params
        ]
    }
}

private extension Oval.Locks.Contact {
    var params: [String: Any] {
        var dict: [String: Any] = ["phone_number": phoneNumber]
        if let firstName = firstName {
            dict["first_name"] = firstName
        }
        if let lastName = lastName {
            dict["last_name"] = lastName
        }
        if let countryCode = countryCode {
            dict["country_code"] = countryCode
        }
        return dict
    }
}

private extension Oval.Locks.EmergencyMessage {
    var params: [String: Any] {
        return ["crash_id": crashId,
                "location": location.params,
                "mac_id": macId,
                "contacts": contacts.map({ $0.params })]
    }
}

private extension CLLocationCoordinate2D {
    var params: [String: Any] {
        return ["latitude": latitude, "longitude": longitude]
    }
}
