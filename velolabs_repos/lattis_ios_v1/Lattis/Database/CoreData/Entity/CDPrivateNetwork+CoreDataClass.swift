//
//  CDPrivateNetwork+CoreDataClass.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/05/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import CoreData

@objc(CDPrivateNetwork)
public class CDPrivateNetwork: NSManagedObject {
    
}

extension CDPrivateNetwork: CoreDataObject {
    static var entityName: String {return "CDPrivateNetwork"}
}
