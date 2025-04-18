//
//  CDUser+CoreDataClass.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/7/17.
//  Copyright © 2017 Lattis. All rights reserved.
//
//

import Foundation
import CoreData

@objc(CDUser)
public class CDUser: NSManagedObject {

}

extension CDUser: CoreDataObject {
    static var entityName: String { return "CDUser" }
}
