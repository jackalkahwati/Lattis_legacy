//
//  Handler.swift
//  AXALock
//
//  Created by Ravil Khusainov on 24.02.2020.
//

import Foundation


public extension AxaBLE {
    class Handler: NSObject {
        public var discovered: (Lock) -> () = {_ in}
        public var connectionChanged: (Lock) -> () = {_ in}
        public var statusChanged: (Lock) -> () = {_ in}
        public var cableStatusChanged: (Lock, Lock.Cable) -> () = {_,_ in}
        public var failed: (Lock, Error) -> () = {_, _ in}
        public var lockInfoUpdated: (Lock) -> () = {_ in}
        public var bleStateUpdated: (Bool) -> () = {_ in}
        
        var locks: [Lock] = []
        
        public func add(_ lock: Lock) {
            guard !locks.contains(lock) else { return }
            AxaBLE.Lock.add(handler: self)
            locks.append(lock)
        }
        
        public func remove(_ lock: Lock) {
            guard let idx = locks.firstIndex(of: lock) else { return }
            locks.remove(at: idx)
        }
    }
}

