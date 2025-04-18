//
//  LockOnboardingViewModel.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 06.01.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import UIKit
import LattisSDK
import Oval

class LockOnboardingViewModel {
    let networkLocks: [Ellipse]
    init(_ locks: [Ellipse], onboard: @escaping (Peripheral) -> ()) {
        networkLocks = locks
        self.onboard = onboard
//        insert(peripherals: ble.locks)
    }
    
    var insert: ([IndexPath]) -> () = {_ in}
    var delete: ([IndexPath]) -> () = {_ in}
    var locksCount: Int { locks.count }
    
    fileprivate let onboard: (Peripheral) -> ()
    fileprivate var removedIds: [String] = []
    fileprivate var locks: [Peripheral] = []
    fileprivate let ble = EllipseManager.shared
    fileprivate let network: EllipseNetwork = Session.shared
    
    func scan() {
        EllipseManager.shared.scan(with: self)
    }
    
    func finish(with index: Int) {
        onboard(locks[index])
    }

    func title(for index: Int) -> String {
        return locks[index].name
    }
    
    func blinkLock(at index: Int, completion: @escaping () -> ()) {
        let lock = locks[index]
        lock.flashLED { (error) in
            if let e = error {
                report(error: e)
            }
            completion()
        }
    }
    
    func onboardLock(at index: Int, completion: @escaping (Bool) -> ()) {
        let lock = locks[index]
        network.getFleetId(by: lock.macId) { (result) in
            switch result {
            case .success(let fleet):
                completion(fleet == nil)
            case .failure(let error):
                report(error: error)
                completion(false)
            }
        }
    }
    
    func deleteLock(at index: Int) {
        let lock = locks.remove(at: index)
        removedIds.append(lock.macId)
        delete([IndexPath(row: index, section: 0)])
    }
    
    func insert(peripherals: [Peripheral]) {
        let filtered = peripherals.filter{ lock in
            print(lock.macId)
            print(networkLocks.map({$0.macId}))
            return !self.networkLocks.map({$0.macId}).contains(lock.macId) &&
                !self.removedIds.contains(lock.macId)
        }
        guard filtered.count > 0 else { return }
        var paths: [IndexPath] = []
        for lock in filtered {
            paths.append(.init(row: locks.count, section: 0))
            locks.append(lock)
        }
        self.insert(paths)
    }
}

extension LockOnboardingViewModel: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Peripheral], delete: [Peripheral]) {
        self.insert(peripherals: insert)
    }
}
