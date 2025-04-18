//
//  CoreDataStack+EllipseStorage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/19/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import CoreLocation

extension CoreDataStack: EllipseStorage {
    fileprivate var check: (String) -> Bool {
        return { string in
            return String(describing: CDEllipse.self) == string
        }
    }
    func save(_ ellipse: Ellipse) {
        write(completion: { (context) in
            do {
                var lock = try CDEllipse.find(in: context, with: NSPredicate(format: "lockId = %@", NSNumber(value: ellipse.lockId)))
                if lock == nil {
                    lock = CDEllipse.create(in: context)
                }
                lock?.fill(ellipse, userId: self.userId)
            } catch {
                
            }
        }, fail: { error in
            
        })
    }
    
    func update(_ ellipses: [Ellipse]) {
        write(completion: { (context) in
            do {
                let lockIds = ellipses.map({$0.lockId})
                let toRemove = try CDEllipse.all(in: context, with: NSPredicate(format: "NOT (lockId IN %@)", lockIds))
                toRemove.forEach{context.delete($0)}
                let toUpdate = try CDEllipse.all(in: context, with: NSPredicate(format: "lockId IN %@", lockIds))
                var buffer: [CDEllipse] = []
                for ellipse in ellipses {
                    let lock: CDEllipse
                    if let l = toUpdate.filter({$0.lockId == ellipse.lockId}).first {
                        lock = l
                    } else if let l = buffer.filter({$0.lockId == ellipse.lockId}).first {
                        lock = l
                    } else {
                        lock = CDEllipse.create(in: context)
                    }
                    if ellipses.count == 1 {
                        var copy = ellipse
                        copy.isCurrent = true
                    }
                    lock.fill(ellipse, userId: self.userId)
                    buffer.append(lock)
                }
            } catch {
                
            }
        }, fail: { error in
            
        })
    }
    
    func current(completion: @escaping (Ellipse?) -> ()) -> StorageHandler {
        let call = { [unowned self] in
            self.queue.addOperation {
                DispatchQueue.main.async {
                    do {
                        let result: [CDEllipse] = try self.read()
                        completion(result.compactMap(Ellipse.init).filter({$0.isCurrent}).first)
                    } catch {
                        print(error)
                        completion(nil)
                    }
                }
            }
        }
        return handler(for: call)
    }
    
    func ellipses(completion: @escaping ([Ellipse]) -> ()) -> StorageHandler {
        let call = { [unowned self] in
            self.queue.addOperation {
                DispatchQueue.main.async {
                    do {
                        let result: [CDEllipse] = try self.read()
                        completion(result.compactMap(Ellipse.init))
                    } catch {
                        print(error)
                        completion([])
                    }
                }
            }
        }
        return handler(for: call)
    }
    
    func ellipsesCount() -> Int {
        do {
            return try mainContext.count(for: CDEllipse.request)
        } catch {
            return 0
        }
    }
    
    var isEmpty: Bool {
        let request = CDEllipse.request
        request.includesSubentities = false
        do {
            let count = try mainContext.count(for: request)
            return count <= 0
        } catch {
            return true
        }
    }
    
    func ellipse(lockId: Int, completion: @escaping (Ellipse) -> ()) -> StorageHandler {
        let call = { [unowned self] in
            self.queue.addOperation {
                DispatchQueue.main.async {
                    do {
                        guard let lock = try CDEllipse.find(in: self.mainContext, with: NSPredicate(format: "lockId = %@", NSNumber(value: lockId))),
                            let ellipse = Ellipse(lock) else { return }
                        completion(ellipse)
                    } catch {
                        print(error)
                    }
                }
            }
        }
        return handler(for: call)
    }
    
    func ellipse(macId: String, completion: @escaping (Ellipse) -> ()) -> StorageHandler {
        let call = { [unowned self] in
            self.queue.addOperation {
                DispatchQueue.main.async {
                    do {
                        guard let lock = try CDEllipse.find(in: self.mainContext, with: NSPredicate(format: "macId = %@", macId)),
                            let ellipse = Ellipse(lock) else { return }
                        completion(ellipse)
                    } catch {
                        print(error)
                    }
                }
            }
        }
        return handler(for: call)
    }
    
    func getEllipse(_ macId: String) -> Ellipse? {
        do {
            guard let lock = try CDEllipse.find(in: self.mainContext, with: NSPredicate(format: "macId = %@", macId)) else { return nil }
            return Ellipse(lock)
        } catch {
            return nil
        }
    }
    
    func delete(ellipse: Ellipse) {
        write(completion: { (context) in
            do {
                guard let lock = try CDEllipse.find(in: context, with: NSPredicate(format: "lockId = %@", NSNumber(value: ellipse.lockId))) else { return }
                context.delete(lock)
            } catch {
                print(error)
            }
        }, fail: {print($0)})
    }
}

private extension CoreDataStack {
    func handler(for callback: @escaping () -> ()) -> StorageHandler {
        let handler = StorageHandler(check: check, callback: callback)
        subscribe(handler: handler)
        return handler
    }
}

extension Ellipse {
    init?(_ ellipse: CDEllipse) {
        guard let macId = ellipse.macId else { return nil }
        self.lockId = Int(ellipse.lockId)
        self.macId = macId
        self.name = ellipse.name
        self.connectedAt = ellipse.connectedAt
        self.userId = Int(ellipse.userId)
        self.stateChangedAt = ellipse.stateChangedAt
        self.isAutoLockEnabled = ellipse.isAutoLockEnabled
        self.isAutoUnlockEnabled = ellipse.isAutoUnlockEnabled
        self.sensorSensitivity = .init(ellipse.sensorSensitivity)
        self.isTheftEnabled = ellipse.isTheftEnabled
        self.isCrashEnabled = ellipse.isCrashEnabled
        self.source = .storage
        if let state = ellipse.lockState {
            self.lockState = Ellipse.LockState(rawValue: state)
        }
        self.pinCode = ellipse.pin
        self.shareId = ellipse.shareId == -1 ? nil : Int(ellipse.shareId)
        self.sharedToUserId = ellipse.sharedToUserId == -1 ? nil : Int(ellipse.sharedToUserId)
        if let user = ellipse.owner  {
            self.owner = User(user)
        } else {
            self.owner = nil
        }
        if let user = ellipse.borrower  {
            self.borrower = User(user)
        } else {
            self.borrower = nil
        }
        self.coordinate = CLLocationCoordinate2D(latitude: ellipse.latitude, longitude: ellipse.longitude)
    }
    
    
}

extension CDEllipse {
    func fill(_ ellipse: Ellipse, userId: Int) {
        self.lockId = Int32(ellipse.lockId)
        self.macId = ellipse.macId
        self.name = ellipse.name
        self.userId = Int32(ellipse.userId)
        self.latitude = ellipse.coordinate.latitude
        self.longitude = ellipse.coordinate.longitude
        self.lockState = ellipse.lockState?.rawValue
        self.stateChangedAt = ellipse.stateChangedAt
        if ellipse.source == .storage {
            self.isAutoLockEnabled = ellipse.isAutoLockEnabled
            self.isAutoUnlockEnabled = ellipse.isAutoUnlockEnabled
            self.sensorSensitivity = Int32(ellipse.sensorSensitivity.rawValue)
            self.isTheftEnabled = ellipse.isTheftEnabled
            self.isCrashEnabled = ellipse.isCrashEnabled
        }
        if ellipse.connectedAt != nil {
            self.connectedAt = ellipse.connectedAt
        }
        if let pin = ellipse.pinCode {
            self.pin = pin
        }
        self.shareId = Int32(ellipse.shareId ?? -1)
        self.sharedToUserId = Int32(ellipse.sharedToUserId ?? -1)
        if ellipse.sharedToUserId == nil {
            self.borrower = nil
        }
        do {
            let user = try CDUser.find(in: self.managedObjectContext!, with: NSPredicate(format: "userId = %@", NSNumber(value: userId)))
            if userId == ellipse.userId {
                self.owner = user
                if let bId = ellipse.sharedToUserId {
                    self.borrower = try CDUser.find(in: self.managedObjectContext!, with: NSPredicate(format: "userId = %@", NSNumber(value: bId)))
                }
            } else {
                self.borrower = user
                self.owner = try CDUser.find(in: self.managedObjectContext!, with: NSPredicate(format: "userId = %@", NSNumber(value: ellipse.userId)))
            }
        } catch {
            print(error)
        }
    }
}

extension Ellipse {
    static var count: Int {
        return CoreDataStack.shared.ellipsesCount()
    }
}

extension String {
    var toPin: [Ellipse.Pin] {
        let array = components(separatedBy: ",")
        return array.compactMap{ Ellipse.Pin(rawValue: $0) }
    }
    
    init(_ pin: [Ellipse.Pin]) {
        let array = pin.map({ $0.rawValue })
        self = array.joined(separator: ",")
    }
}

