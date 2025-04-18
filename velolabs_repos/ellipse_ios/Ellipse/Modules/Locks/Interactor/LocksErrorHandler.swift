//
//  LocksErrorHandler.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/3/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import Oval
import LattisSDK
import Firebase

class ErrorHandler {
    typealias ViewType = ErrorPresentable & AnyObject
    open weak var view: ViewType!
    init(_ view: ViewType) {
        self.view = view
    }
    
    func handle(error: Error) {
        report(error: error)
        if let error = error as? EllipseError {
            handleBLE(error: error)
        } else if let error = error as? SessionError {
            handleOval(error: error)
        } else {
            view?.show(error: error)
        }
    }
    
    func handleBLE(error: EllipseError) {
        view?.show(error: error)
    }
    
    func handleOval(error: SessionError) {
        view?.show(error: error)
    }
}

public func report(error: Error, file: String = #file, line: Int = #line) {
    let info: [String: Any] = [
        "description": error.localizedDescription,
        "file": file,
        "line": line
    ]
    Crashlytics.sharedInstance().recordError(error, withAdditionalUserInfo: info)
}

