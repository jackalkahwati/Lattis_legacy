//
//  BaseRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 05/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class BaseRouter {
    internal weak var controller: UIViewController!
    init(_ controller: UIViewController) {
        self.controller = controller
    }
}
