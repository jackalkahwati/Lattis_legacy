//
//  CDEllipse+CoreDataClass.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/19/17.
//  Copyright © 2017 Lattis. All rights reserved.
//
//

import Foundation
import CoreData

public class CDEllipse: NSManagedObject {

}

extension CDEllipse: CoreDataObject {
    static var entityName: String { return "CDEllipse" }
}
