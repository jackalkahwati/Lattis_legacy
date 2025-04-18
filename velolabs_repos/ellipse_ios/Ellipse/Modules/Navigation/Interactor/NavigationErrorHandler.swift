//
//  NavigationErrorHandler.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/17/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

final class NavigationErrorHandler: ErrorHandler {
    override func handle(error: Error) {
        report(error: error)
        view.show(warning: "unable_to_build_direction".localized(), title: nil)
    }
}
