//
//  SLEmergencyContact+CoreDataClass.swift
//  Ellipse
//
//  Created by Andre Green on 10/3/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import Foundation
import CoreData

@objc(SLEmergencyContact)
public class SLEmergencyContact: NSManagedObject {
    func fullName() -> String {
        var name = ""
        if let firstName = self.firstName {
            name += firstName
        }
        
        if let lastName = self.lastName {
            name += " " + lastName
        }
        
        return name
    }
}
