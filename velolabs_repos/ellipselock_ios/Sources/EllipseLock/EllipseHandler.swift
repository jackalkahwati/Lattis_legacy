//
//  EllipseHandler.swift
//  EllipseLock
//
//  Created by Ravil Khusainov on 28.02.2020.
//

import Foundation

public class EllipseHandler<Lock: EllipseProtocol>: NSObject {
    public var discovered: (Lock) -> () = {_ in}
    public var connectionUpdated: (Lock) -> () = {_ in}
    public var securityUpdated: (Lock) -> () = {_ in}
    public var metadataUpdated: (Lock) -> () = {_ in}
    public var magnetDataUpdated: (Lock) -> () = {_ in}
    public var failed: (Lock, Error) -> () = {_, _ in}
    
    var locks: [Lock] = []

    public override init() {
        super.init()
        Lock.add(handler: self)
    }
    
    public func add(_ lock: Lock) {
        guard !locks.contains(lock) else { return }
        locks.append(lock)
    }
    
    public func remove(_ lock: Lock) {
        guard let idx = locks.firstIndex(of: lock) else { return }
        locks.remove(at: idx)
    }
}
