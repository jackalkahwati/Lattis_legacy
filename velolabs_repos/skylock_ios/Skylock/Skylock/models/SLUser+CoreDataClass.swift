//
//  SLUser+CoreDataClass.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 22/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//  This file was automatically generated and should not be edited.
//

import Foundation
import CoreData
import KeychainSwift

public class SLUser: NSManagedObject {
    static let entityName = "SLUser"
    var fullName: String {
        var name = firstName ?? ""
        if let lastName = lastName {
            if name.isEmpty {
                name = lastName
            } else {
                name += " \(lastName)"
            }
        }
        return name
    }
    
    var location: CLLocationCoordinate2D = kCLLocationCoordinate2DInvalid
    
    var password: String? {
        guard let usersId = usersId else { return nil }
        return KeychainSwift(keyPrefix: usersId).get(.password)
    }
}
