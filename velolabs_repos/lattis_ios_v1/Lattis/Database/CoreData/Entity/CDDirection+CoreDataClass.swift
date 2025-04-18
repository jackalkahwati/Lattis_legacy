//
//  CDDirection+CoreDataClass.swift
//  Lattis
//
//  Created by Ravil Khusainov on 22/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import CoreData

@objc(CDDirection)
public class CDDirection: NSManagedObject {

}


extension CDDirection: CoreDataObject {
    static var entityName: String {
        return "CDDirection"
    }
}
