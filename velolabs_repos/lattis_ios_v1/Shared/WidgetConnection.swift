//
//  WidgetConnection.swift
//  Lattis
//
//  Created by Ravil Khusainov on 16.01.2020.
//  Copyright © 2020 Velo Labs. All rights reserved.
//

import Foundation

#if RELEASE
fileprivate let groupIdentifier = "group.io.lattis.www.Lattis.Today"
#elseif BETA
fileprivate let groupIdentifier = "group.io.lattis.www.Lattis.Beta.Today"
#else
fileprivate let groupIdentifier = "group.io.lattis.www.Lattis.Dev.Today"
#endif

class WidgetConnection {
    var receive: (Message) -> () = {_ in}
    
    let appIsExtension = Bundle.main.bundlePath.hasSuffix(".appex")
    
    fileprivate let userDefaults: UserDefaults
    fileprivate let encoder = JSONEncoder()
    fileprivate let decoder = JSONDecoder()
    fileprivate var sendingSuffix: String { return appIsExtension ? ".Widget" : ".MainApp"}
    fileprivate var subscribingSuffix: String { return !appIsExtension ? ".Widget" : ".MainApp"}
    
    init() {
        userDefaults = UserDefaults(suiteName: groupIdentifier)!
        DarwinNotificationCenter.shared.addObserver(self, for: .init("\(groupIdentifier).Open\(subscribingSuffix)")) { [weak self] (notification) in
            self?.receive(.lock(false))
        }
        DarwinNotificationCenter.shared.addObserver(self, for: .init("\(groupIdentifier).Close\(subscribingSuffix)")) { [weak self] (notification) in
            self?.receive(.lock(true))
        }
    }
    
    func send(message: Message) {
        var name = groupIdentifier
        switch message {
        case .lock(let shouldLock):
            name += shouldLock ? ".Close" : ".Open"
        }
        name += sendingSuffix
        DarwinNotificationCenter.shared.postNotification(.init(name))
    }
    
    func swithcLockState(to locked: Bool) {
        if let trip = currentTrip {
            currentTrip = trip.updated(isLocked: locked)
        }
        send(message: .lock(locked))
    }
    
    func clean() {
        currentTrip = nil
    }
    
    var loggedIn: Bool {
        set {
            clean()
            userDefaults.set(newValue, forKey: "loggedIn")
            userDefaults.synchronize()
        }
        get {
            userDefaults.bool(forKey: "loggedIn")
        }
    }
    
    var currentTrip: SharedTrip? {
        set {
            if let trip = newValue {
                guard let data = try? encoder.encode(trip) else { return }
                    let string = String(data: data, encoding: .utf8)
                userDefaults.set(string, forKey: "trip")
            } else {
                userDefaults.removeObject(forKey: "trip")
            }
            userDefaults.synchronize()
        }
        get {
            guard let string = userDefaults.string(forKey: "trip"),
                let data = string.data(using: .utf8),
                let trip = try? decoder.decode(SharedTrip.self, from: data) else { return nil }
            return trip
        }
    }
    
    enum Message {
        case lock(Bool)
    }
}
