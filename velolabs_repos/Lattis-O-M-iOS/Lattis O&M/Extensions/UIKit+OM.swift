//
//  UIKit+OM.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

extension UIStoryboard {
    static var main: UIStoryboard {
        return UIStoryboard(name: "Main", bundle: nil)
    }
    
    static var userOnboarding: UIStoryboard {
        return UIStoryboard(name: "UserOnboarding", bundle: nil)
    }
    
    static var dashboard: UIStoryboard {
        return UIStoryboard(name: "Dashboard", bundle: nil)
    }
    
    static var settings: UIStoryboard {
        return UIStoryboard(name: "Settings", bundle: nil)
    }
    
    static var ticketDetails: UIStoryboard {
        return UIStoryboard(name: "TicketDetails", bundle: nil)
    }
    
    static var bikeSearch: UIStoryboard {
        return UIStoryboard(name: "BikeSearch", bundle: nil)
    }
}
