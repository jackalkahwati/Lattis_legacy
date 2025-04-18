//
//  CDEllipse+CoreDataClass.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/11/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import CoreData

@objc(CDEllipse)
public class CDEllipse: NSManagedObject {

}

extension CDEllipse: CoreDataObject {
    static var entityName: String { return "CDEllipse" }
}
