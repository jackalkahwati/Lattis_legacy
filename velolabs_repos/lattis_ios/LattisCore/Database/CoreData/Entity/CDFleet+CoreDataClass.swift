//
//  CDFleet+CoreDataClass.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 31/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//
//

import Foundation
import CoreData

@objc(CDFleet)
public class CDFleet: NSManagedObject {

}

extension CDFleet: CoreDataObject {
    static var entityName: String {
        return "CDFleet"
    }
}

extension Fleet {
    init(_ cd: CDFleet) {
        fleetId = Int(cd.fleetId)
        name = cd.name
        customer = cd.customer ?? ""
        email = cd.email ?? ""
        logo = cd.logo == nil ? nil : URL(string: cd.logo!)
    }
}

extension CDFleet {
    func fill(_ fl: Fleet) {
        fleetId = Int32(fl.fleetId)
        name = fl.name
        email = fl.email
        customer = fl.customer
        logo = fl.logo?.absoluteString
    }
}
