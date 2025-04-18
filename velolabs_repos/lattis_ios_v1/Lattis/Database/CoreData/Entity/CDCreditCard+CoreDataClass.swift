//
//  CDCreditCard+CoreDataClass.swift
//  Lattis
//
//  Created by Ravil Khusainov on 7/4/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import CoreData

@objc(CDCreditCard)
public class CDCreditCard: NSManagedObject {

}

extension CDCreditCard: CoreDataObject {
    static var entityName: String {return "CDCreditCard"}
}

