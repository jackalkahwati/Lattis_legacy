//
//  CDTicket+CoreDataClass.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import CoreData

@objc(CDTicket)
public class CDTicket: NSManagedObject {

}

extension CDTicket: CoreDataObject {
    static var entityName: String { return "CDTicket" }
}
