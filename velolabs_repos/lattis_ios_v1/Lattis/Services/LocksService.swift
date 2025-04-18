//
//  LocksService.swift
//  Lattis
//
//  Created by Ravil Khusainov on 19/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import Oval
import LattisSDK

final class LocksService: NSObject {
    public static let shared = LocksService()
    fileprivate var storage: EllipseStorage!
    fileprivate let network = Session.shared
    fileprivate let ble = EllipseManager.shared
    fileprivate var locksSubsribers: [Request: ([Lock]) -> ()] = [:]
    fileprivate var locks: [Lock] = []
    class func setup(_ storage: EllipseStorage) {
        shared.storage = storage
    }
    
//    override init() {
//        super.init()
//
//    }
    
    public func startScan() {
        ble.scan(with: self)
    }
    
    public func stopScan() {
        ble.stopScan()
    }
    
    public func subscribe(request: Request, completion: @escaping ([Lock]) -> ()) {
        locksSubsribers[request] = completion
        notify()
    }
    
    fileprivate func notify() {
        DispatchQueue.main.async {
            for (_, value) in self.locksSubsribers {
                value(self.locks)
            }
        }
    }
    
    public func unsubscribe(target: AnyHashable) {
        guard let request = locksSubsribers.keys.filter({ $0.target == target }).first else { return }
        locksSubsribers[request] = nil
    }
}


extension LocksService: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [LattisSDK.Ellipse], delete: [LattisSDK.Ellipse]) {
        for per in lockManager.locks {
            if let idx = locks.firstIndex(where: { $0.peripheral == nil &&  $0.ellipse?.macId == per.macId }) {
                var lock = locks[idx]
                lock.peripheral = per
                locks.insert(lock, at: idx)
            } else {
                var lock = Lock(peripheral: per)
                lock.ellipse = storage.ellipse(with: per.macId)
                locks.append(lock)
            }
        }
        notify()
    }
}

extension LocksService {
    struct Request {
        let target: AnyHashable
        var lockType: LockType
        
        init(target: AnyHashable, predicate: NSPredicate? = nil, filter: ((Lock) -> Bool)? = nil, lockType: LockType = .ble) {
            self.target = target
            self.lockType = lockType
        }
    }
    
    enum LockType {
        case storage, ble
    }
}

extension LocksService.Request: Hashable {
    var hashValue: Int {
        return target.hashValue
    }
}

func ==(lhs: LocksService.Request, rhs: LocksService.Request) -> Bool {
    return lhs.target == rhs.target
}
