    //
//  LocksService.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 23/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import Foundation
import KeychainSwift
import RestService

// Fasade for cache and server
final class LocksService {
    func locks(updateCache: Bool = false, completion: (([SLLock], Bool, Error?) -> ())? = nil) {
        let dbMgr = SLDatabaseManager.shared()
        let locks: [SLLock] = dbMgr.locksForCurrentUser() as? [SLLock] ?? []
        
        completion?(locks, false, nil)
        
        guard updateCache else { return }
        Oval.locks.locks(success: { (group) in
            let serverLocks = group.all
            let allLockMacs = Set(serverLocks.map({ $0.macId }))
            let myLockMacs = Set(group.my.map({ $0.macId }))
            let borrowedLockMacs = Set(group.borrowed.map({ $0.macId }))
            let activeLockMacs = Set(group.shared.active.map({ $0.macId }))
            let inactiveLockMacs = Set(group.shared.inactive.map({ $0.macId }))
            let currentUser = dbMgr.getCurrentUser()
            
            LocksService.cleanUp(my: myLockMacs, shared: activeLockMacs.union(inactiveLockMacs), borrowed: borrowedLockMacs)
            
            for serverLock in serverLocks {
                var lock: SLLock!
                if let oldLock = locks.filter({ $0.lockId == serverLock.lockId }).first {
                    lock = oldLock
                } else {
                    lock = dbMgr.newLockWith(givenName: serverLock.name, andMacAddress: serverLock.macId)
                }
                lock.fill(with: serverLock)
                lock.owner = dbMgr.user(withId: serverLock.userId, usersId: serverLock.usersId)
                lock.isShared = borrowedLockMacs.contains(serverLock.macId)
                if let userId = serverLock.sharedToUserId, activeLockMacs.contains(serverLock.macId) || inactiveLockMacs.contains(serverLock.macId) {
                    lock.borrower = dbMgr.user(withId: userId, usersId: nil)
                    if lock.borrower?.usersId == nil {
                        LocksService.update(userWithId: userId)
                    }
                } else {
                    lock.borrower = nil
                }
                lock.isSharingAccepted = activeLockMacs.contains(serverLock.macId)
                lock.user = currentUser ?? lock.owner
                if lock.user?.userId != lock.owner?.userId {
                    Oval.users.user(withId: lock.owner?.userId, success: { (ovalUser) in
                        SLDatabaseManager.shared().save(ovalUser: ovalUser, setAsCurrent: false)
                    }, fail: {_ in})
                }
                lock.onboard = true
                dbMgr.save(lock)
            }
            
            NotificationCenter.default.post(
                name: NSNotification.Name(rawValue: kSLNotificationLockManagerUpdatedLocksFromServer),
                object: Array(allLockMacs)
            )
            
            guard let locks = dbMgr.locksForCurrentUser() as? [SLLock] else {
                completion?([], true, nil)
                return
            }
            
            completion?(locks, true, nil)
        }, fail: { error in
            completion?([],true, error)
        })
    }
    
    private class func update(userWithId userId: Int32) {
        Oval.users.user(withId: userId, success: { (result) in
            SLDatabaseManager.shared().save(ovalUser: result, setAsCurrent: false)
        }, fail: { _ in })
    }
    
    private class func cleanUp(my: Set<String>, shared: Set<String>, borrowed: Set<String>) {
        let lockMgr = SLLockManager.sharedManager
        guard let locks = SLDatabaseManager.shared().locksForCurrentUser() as? [SLLock] else { return }
        lockMgr.clear(allLocks: locks.filter({ $0.borrower == nil && $0.isShared == false }), existed: Array(my))
        lockMgr.clear(allLocks: locks.filter({ $0.borrower != nil }), existed: Array(shared))
        lockMgr.clear(allLocks: locks.filter({ $0.isShared }), existed: Array(borrowed))
    }
    
    func cleanKeys(forLockWith macId: String) {
        let keychain = KeychainSwift(keyPrefix: macId)
        keychain.delete(.signedMessage)
        keychain.delete(.publicKey)
    }
    
    func signLock(with macId: String, completion: @escaping (String, String) -> (), fail: @escaping (Error) -> ()) {
        func sign() {
            let keychain = KeychainSwift(keyPrefix: macId)
            if let signedMessege = keychain.get(.signedMessage), let publicKey = keychain.get(.publicKey) {
                return completion(signedMessege, publicKey)
            }
            Oval.locks.signLock(with: macId, success: { (signedMessege, publicKey) in
                keychain.setWithAccess(signedMessege, forKey: .signedMessage, access: .accessibleAfterFirstUnlock)
                keychain.set(publicKey, forKey: .publicKey)
                completion(signedMessege, publicKey)
            }, fail: fail)
        }
        
        func register() {
            Oval.locks.registration(with: macId, success: { (serverLock) in
                let dbManager = SLDatabaseManager.shared()
                let lock = dbManager.getLockWithMacId(serverLock.macId)
                lock?.lockId = serverLock.lockId
                dbManager.save(lock)
                sign()
            }, fail: { error in
                if let err = error as? Oval.Error, err == .internalServer || err == .resourceNotFound { //|| err == .resourceNotFound
                    sign()
                } else {
                    fail(error)
                }
            })
        }
        let lock = SLDatabaseManager.shared().getLockWithMacId(macId)
        if lock?.owner == SLDatabaseManager.shared().getCurrentUser() || lock!.isShared { //|| lock!.isShared
            sign()
        } else {
            if let lock = lock, lock.isInFactoryMode {
                register()
            } else {
                fail(SLLockManagerConnectionError.NotAuthorized)
            }
        }
    }
    
    func changeName(forLockWith lockId: Int32, name: String, success: (() -> ())? = nil, fail:  ((Error) -> ())? = nil) {
        let request = Oval.Locks.Request(lockId: lockId, name: name)
        Oval.locks.update(lock: request, success: { (result) in
            let dbMgr = SLDatabaseManager.shared()
            if let lock = dbMgr.getLockWithLockId(lockId) {
                lock.fill(with: result)
                dbMgr.save(lock)
                
                NotificationCenter.default.post(
                    name: NSNotification.Name(rawValue: kSLNotificationLockNameChanged),
                    object: lock
                )
            }
            success?()
        }, fail: { fail?($0) })
    }
    
    func save(pinCode: [Oval.Locks.Pin], forLockWithMacId macId: String, success: (() -> ())? = nil, fail:  ((Error) -> ())? = nil) {
        Oval.locks.save(pinCode: pinCode, forLock: macId, success: {
            SLLockManager.sharedManager.writeTouchPadButtonPushes(touches: pinCode.map({ $0.byteValue }))
        }, fail: { fail?($0) })
    }
    
    func acceptSharing(confirmationCode: String, success: @escaping (SLLock) -> (), fail: (() -> ())? = nil) {
        Oval.locks.acceptSharing(confirmationCode: confirmationCode, success: { [weak self] macId in
            self?.locks(updateCache: true, completion: { (locks, isServer, error) in
                guard error == nil else {
                    fail?()
                    return
                }
                guard let lock = locks.filter({ $0.macId == macId }).first, isServer else {
                    return
                }
                success(lock)
            })
        }, fail: { _ in fail?() })
    }
}

