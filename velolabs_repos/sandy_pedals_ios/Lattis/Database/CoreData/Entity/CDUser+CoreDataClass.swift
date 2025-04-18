//
//  CDUser+CoreDataClass.swift
//  Lattis
//
//  Created by Ravil Khusainov on 21/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import CoreData

@objc(CDUser)
public class CDUser: NSManagedObject {
    var privateNetworksArray: [CDPrivateNetwork] {
        get {
            guard let array = privateNetworks?.allObjects as? [CDPrivateNetwork] else { return [] }
            return array
        }
        set {
            privateNetworks = NSSet(array: newValue)
        }
    }
}

extension CDUser: CoreDataObject {
    static var entityName: String {return "CDUser"}
}
