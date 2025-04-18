//
//  CDOperator+CoreDataClass.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/17/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import CoreData

@objc(CDOperator)
public class CDOperator: NSManagedObject {

}

extension CDOperator: CoreDataObject {
    static var entityName: String { return "CDOperator" }
}
