//
//  UIResponder+Error.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit

public protocol AlertPresentable: Error {
    var title: String? { get }
    var message: String? { get }
}

struct ErrorAlert: AlertPresentable {
    let title: String?
    let message: String?
}

extension UIResponder {
    
    @objc
    open func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        guard let nextResponder = next else { return }
        nextResponder.handle(error, from: viewController, retryHandler: retryHandler)
    }
}

extension UIViewController {
    
    func handle(_ error: Error, retryHandler: @escaping () -> Void = {}, file: String = #file, function: String = #function) {
        Analytics.report(error, file: file, function: function)
        handle(error, from: self, retryHandler: retryHandler)
    }    
}
