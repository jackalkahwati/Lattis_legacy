//
//  CDContact+CoreDataClass.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/14/17.
//  Copyright © 2017 Lattis. All rights reserved.
//
//

import Foundation
import CoreData

@objc(CDContact)
public class CDContact: NSManagedObject {

}
extension CDContact: CoreDataObject {
    static var entityName: String { return "CDContact" }
}
