//
//  CDUser+CoreDataClass.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//
//

import Foundation
import CoreData

@objc(CDUser)
public class CDUser: NSManagedObject {

}

extension CDUser: CoreDataObject {
    static var entityName: String {
        return "CDUser"
    }
}

extension User {
    init(_ cd: CDUser) {
        self.userId = Int(cd.userId)
        self.firstName = cd.firstName ?? ""
        self.lastName = cd.lastName ?? ""
        self.phoneNumber = cd.phoneNumber
        self.email = cd.email ?? ""
    }
}

extension CDUser {
    static func fill(_ user: User, context: NSManagedObjectContext) throws  {
        let u: CDUser
        if let old = try CDUser.find(in: context, with: NSPredicate(format: "userId = %@", NSNumber(value: user.userId))) {
            u = old
        } else {
            u = .create(in: context)
        }
        u.email = user.email
        u.firstName = user.firstName
        u.lastName = user.lastName
        u.userId = Int32(user.userId)
        u.phoneNumber = user.phoneNumber
    }
}


