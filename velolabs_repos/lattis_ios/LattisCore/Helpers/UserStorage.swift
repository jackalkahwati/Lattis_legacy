//
//  UserStorage.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation

class UserStorage {
    fileprivate let coreData = CoreDataStack.shared
    fileprivate var callback: ((User) -> ())?
    fileprivate var cached: User?
    fileprivate let network: UserAPI = AppRouter.shared.api()
    fileprivate var subscryber: CoreDataStack.Subscriber<CDUser>?
    
    init() {
        subscryber = coreData.subscribe(completion: { [weak self] (users) in
            self?.handle(user: users.first)
        })
    }
    
    func current(needRefresh: Bool = false, completion: @escaping (User) -> ()) {
        self.callback = completion
        if let user = cached {
            completion(user)
        }
        guard needRefresh else { return }
        refresh()
    }
    
    func update(user: User, failure: @escaping (Error) -> ()) {
        network.update(user: user) { [weak self] (result) in
            switch result {
            case .success(let u):
                self?.coreData.save(user: u) { c in
                    self?.handle(changes: c)
                }
            case .failure(let error):
                failure(error)
            }
        }
    }
    
    func refresh() {
        guard coreData.isReady else {
            AppRouter.shared.checIfLoggedIn { (isLoggedIn) in
                if isLoggedIn {
                    self.refresh()
                }
            }
            return
        }
        network.refresh { [weak self] (result) in
            switch result {
            case .success(let update):
                self?.coreData.save(user: update.user) { hasChanges in
                    self?.handle(changes: hasChanges)
                }
                self?.coreData.save(fleets: update.fleets)
            case .failure(let error):
                Analytics.report(error)
            }
        }
    }
    
    func update(email: String, code: String? = nil, completion: @escaping (Error?) -> ()) {
        network.update(email: email, code: code) { [weak self] (result) in
            switch result {
            case .success(let u):
                if let user = u {
                    self?.coreData.save(user: user, completion: {_ in})
                }
                completion(nil)
            case .failure(let error):
                completion(error)
            }
        }
    }
    
    func update(phone: String, code: String? = nil, completion: @escaping (Error?) -> ()) {
        network.update(phone: phone, code: code) { [weak self] (result) in
            switch result {
            case .success(let u):
                if let user = u {
                    self?.coreData.save(user: user, completion: {_ in})
                }
                completion(nil)
            case .failure(let e):
                completion(e)
            }
        }
    }
    
    func update(password: String, newPassword: String, completion: @escaping (Error?) -> ()) {
        network.update(password: password, newPassword: newPassword) { (result) in
            switch result {
            case .success:
                completion(nil)
            case .failure(let e):
                completion(e)
            }
        }
    }
    
    func deleteAccount(completion: @escaping (Error?) -> ()) {
        network.deleteAccount { result in
            switch result {
            case .success:
                completion(nil)
            case .failure(let e):
                completion(e)
            }
        }
    }
    
    fileprivate func handle(user: CDUser?) {
        guard let cd = user else { return }
        let result = User(cd)
        cached = result
        callback?(result)
    }
    
    fileprivate func handle(changes: Bool) {
        guard !changes, let c = callback, let u = cached else { return }
        c(u)
    }
}

extension CoreDataStack {
    func save(user: User, completion: @escaping (Bool) -> ()) {
        write(completion: { (context) in
            do {
                try CDUser.fill(user, context: context)
            } catch {
                Analytics.report(error)
            }
        }, fail: { (error) in
            Analytics.report(error)
        }, after: { hasChanges in
            completion(hasChanges)
        })
    }
}
