//
//  CDFleet+CoreDataClass.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/13/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import CoreData

@objc(CDFleet)
public class CDFleet: NSManagedObject {

}

extension CDFleet: CoreDataObject {
    static var entityName: String { return "CDFleet" }
}
