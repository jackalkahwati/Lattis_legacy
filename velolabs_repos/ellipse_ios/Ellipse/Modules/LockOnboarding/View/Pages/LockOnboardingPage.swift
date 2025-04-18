//
//  LockOnboardingPage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

protocol LockOnboardingPageDelegate: class {
    func hideCloseButton()
}

protocol LockOnboardingPage: class {
    func set(delegate: Any?)
}
