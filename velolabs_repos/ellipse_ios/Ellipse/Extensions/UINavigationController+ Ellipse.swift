//
//  UINavigationController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 13/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

extension UIBarButtonItem {
    class func close(target: Any?, action: Selector?) -> UIBarButtonItem {
        return UIBarButtonItem(image: UIImage(named: "button_close_big"), style: .plain, target: target, action: action)
    }

    class func back(target: Any?, action: Selector?) -> UIBarButtonItem {
        return UIBarButtonItem(image: #imageLiteral(resourceName: "icon_back"), style: .plain, target: target, action: action)
    }

    class func menu(target: Any?, action: Selector?) -> UIBarButtonItem {
        return UIBarButtonItem(image: UIImage(named: "hamburger_gray"), style: .plain, target: target, action: action)
    }

    static var empty: UIBarButtonItem {
        return UIBarButtonItem(title: "", style: .plain, target: nil, action: nil)
    }
}

