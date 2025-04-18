//
//  CoreDataStack+UserStorage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/7/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

extension CoreDataStack: UserStorage {
    fileprivate var check: (String) -> Bool {
        return { string in
            return String(describing: CDUser.self) == string
        }
    }
    
    func save(_ user: User) {
        write(completion: { (context) in
            do {
                var usr = try CDUser.find(in: context, with: NSPredicate(format: "userId = %@", NSNumber(value: user.userId)))
                if usr == nil, let usersId = user.usersId {
                    usr = try CDUser.find(in: context, with: NSPredicate(format: "usersId = %@", usersId))
                }
                if usr == nil {
                    usr = CDUser.create(in: context)
                }
                usr?.fill(user)
            } catch {
                print(error)
            }
        }, fail: {
            print($0)
        })
    }
    
    func save(_ user: FacebookHelper.User) {
        write(completion: { (context) in
            do {
                var usr = try CDUser.find(in: context, with: NSPredicate(format: "usersId = %@", user.id))
                if usr == nil {
                    usr = CDUser.create(in: context)
                }
                usr?.usersId = user.id
                usr?.pictureUrl = user.picture
            } catch {
                print(error)
            }
        }, fail: {
            print($0)
        })
    }
    
    func current(completion: @escaping (User?) -> ()) -> StorageHandler {
        let handler = StorageHandler(check: check, callback: { [unowned self] in
            do {
                if let user = try CDUser.find(in: self.mainContext, with: NSPredicate(format: "userId = %@", NSNumber(value: self.userId))) {
                    completion(User(user))
                } else {
                    completion(nil)
                }
            } catch {
                completion(nil)
                print(error)
            }
        })
        subscribe(handler: handler)
        return handler
    }
    
    var getCurrent: User? {
        do {
            guard let usr = try CDUser.find(in: mainContext, with: NSPredicate(format: "userId = %@", NSNumber(value: self.userId))) else { return nil }
            return User(usr)
        } catch {
            print(error)
            return nil
        }
    }
    
    var getLockName: String? {
        guard let user = getCurrent else { return nil }
        return user.newLockName
    }
    
    func isUserExists(with userId: Int) -> Bool {
        do {
            return try CDUser.find(in: mainContext, with: NSPredicate(format: "userId = %@", NSNumber(value: userId))) != nil
        } catch {
            print(error)
            return false
        }
    }
}

extension User {
    init(_ user: CDUser) {
        self.userId = Int(user.userId)
        self.email = user.email
        self.firstName = user.firstName
        self.lastName = user.lastName
        self.phone = user.phone
        self.usersId = user.usersId
        self.pictureUrl = user.pictureUrl
        self.locksCount = user.locks?.count ?? 0
        if let tt = user.userType, let type = UserType(rawValue: tt) {
            self.userType = type
        }
    }
    
    var newLockName: String? {
        if let name = firstName ?? lastName {
            return "\(name)'s Ellipse \(locksCount)"
        } else {
            return nil
        }
    }
}

extension CDUser {
    func fill(_ user: User) {
        self.userId = Int32(user.userId)
        self.email = user.email
        self.firstName = user.firstName
        self.lastName = user.lastName
        self.phone = user.phone
        self.usersId = user.usersId
        self.pictureUrl = FacebookHelper.User.pictureUrl(for: user.usersId)
        self.userType = user.userType.rawValue
        do {
            let locks = try CDEllipse.all(in: self.managedObjectContext!, with: NSPredicate(format: "userId = %@", NSNumber(value: self.userId)))
            for lock in locks {
                lock.owner = self
            }
            let borrowed = try CDEllipse.all(in: self.managedObjectContext!, with: NSPredicate(format: "sharedToUserId = %@", NSNumber(value: self.userId)))
            for lock in borrowed {
                lock.borrower = self
            }
        } catch {
            
        }
    }
}
