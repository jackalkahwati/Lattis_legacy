//
//  CDLock+CoreDataClass.swift
//  Lattis
//
//  Created by Ravil Khusainov on 18/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//  This file was automatically generated and should not be edited.
//

import Foundation
import CoreData

@objc(CDLock)
public class CDLock: NSManagedObject {
    func fill(with ellipse: Ellipse) {
        lockId = ellipse.lockId
        macId = ellipse.macId
        userId = ellipse.userId
        shareId = ellipse.shareId
        usersId = ellipse.usersId
        name = ellipse.name
        sharedToUserId = ellipse.sharedToUserId
    }
    
    var ellipse: Ellipse {
        return Ellipse(self)
    }
}

extension CDLock: CoreDataObject {
    static var entityName: String { return "CDLock" }
}

extension Ellipse {
    init(_ lock: CDLock) {
        lockId = lock.lockId
        macId = lock.macId!
        userId = lock.userId
        shareId = lock.shareId
        usersId = lock.usersId
        name = lock.name
        sharedToUserId = lock.sharedToUserId
    }
}
