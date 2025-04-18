//
//  LocksOnboardingViewModel.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/17/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import Oval
import LattisSDK

class LocksOnboardingViewModel {
    var reload: (String) -> () = {_ in}
    fileprivate let ble = EllipseManager.shared
    fileprivate let storage = CoreDataStack.shared
    fileprivate(set) var locks: [Ellipse.Device] = []
    fileprivate let sort: (Peripheral, Peripheral) -> Bool = { lhs, rhs in
        return lhs.name > rhs.name
    }
    
    init() {
        ble.scan(with: self)
    }
}

extension LocksOnboardingViewModel: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Peripheral], delete: [Peripheral]) {
        locks = lockManager.locks.filter({self.storage.getEllipse($0.macId) == nil}).sorted(by: sort).map(Ellipse.Device.init)
        reload(locks.count > 0 ? "we_have_found_the_following".localized() : "searching_for_ellipses".localized())
    }
}

class SharedLockOnboardingHelper {
    fileprivate let ble = EllipseManager.shared
    fileprivate var lock: Ellipse.Device?
    fileprivate let ellipse: Ellipse
    fileprivate var handler: (Error?) -> () = {_ in}
    fileprivate let timer: Timer
    
    init(_ ellipse: Ellipse, handle: @escaping (Error?) -> ()) {
        self.ellipse = ellipse
        self.handler = handle
        self.timer = Timer.scheduledTimer(withTimeInterval: 30, repeats: false, block: { _ in
            handle(EllipseError.timeout)
        })
        ble.scan(with: self)
        
        // Updating lock owner
        Session.shared.user(ellipse.userId) { [weak self] result in
            switch result {
            case .success(let user):
                CoreDataStack.shared.save(user)
            case .failure:
                break
            }
        }
    }
}

extension SharedLockOnboardingHelper: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Peripheral], delete: [Peripheral]) {
        if let per = lockManager.locks.filter({ $0.macId == ellipse.macId }).first {
            let lock = Ellipse.Device(per)
            lock.ellipse = ellipse
            lock.connect(self)
            self.lock = lock
            lockManager.stopScan()
            timer.invalidate()
        }
    }
}

extension SharedLockOnboardingHelper: EllipseDelegate {
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {
        
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
        switch connection {
        case .paired:
            handler(nil)
        case .failed(let error):
            handler(error)
        default:
            break
        }
    }
}
