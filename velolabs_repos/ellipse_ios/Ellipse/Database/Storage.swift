//
//  Storage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/19/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

class StorageHandler {
    let check: (String) -> Bool
    let callback: () -> ()
    init(check: @escaping (String) -> Bool, callback: @escaping () -> ()) {
        self.check = check
        self.callback = callback
    }
}

protocol EllipseStorage {
    var isEmpty: Bool {get}
    func save(_ ellipse: Ellipse)
    func update(_ ellipses: [Ellipse])
    func current(completion: @escaping (Ellipse?) -> ()) -> StorageHandler
    func ellipses(completion: @escaping ([Ellipse]) -> ()) -> StorageHandler
    func ellipse(lockId: Int, completion: @escaping (Ellipse) -> ()) -> StorageHandler
    func ellipse(macId: String, completion: @escaping (Ellipse) -> ()) -> StorageHandler
    func delete(ellipse: Ellipse)
    func getEllipse(_ macId: String) -> Ellipse?
}

protocol UserStorage {
    func save(_ user: User)
    func save(_ user: FacebookHelper.User)
    func current(completion: @escaping (User?) -> ()) -> StorageHandler
    func isUserExists(with userId: Int) -> Bool
    var getCurrent: User? {get}
    var getLockName: String? {get}
}

protocol ContactStorage {
    func update(_ contacts: [Contact])
    func save(_ contact: Contact)
    func delete(_ contact: Contact)
    func emergency(completion: @escaping ([Contact]) -> ()) -> StorageHandler
    func getEmergency() -> [Contact]
}
