//
//  ErrorPresentable.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/11/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LattisSDK
import FirebaseCrashlytics
import Oval

protocol ErrorPresentable {
    func show(error: Error)
    func showAlert(title: String?, subtitle: String?)
}

extension UIViewController: ErrorPresentable {
    func show(error: Error) {
        // FIXME:
        var subtitle: String? = nil
        #if DEBUG
            subtitle = "\(error)"
        #endif
        showAlert(title: nil, subtitle: subtitle)
        report(error: error)
    }
    
    func showAlert(title: String?, subtitle: String?) {
        let alert = ErrorAlertView.alert(title: title ?? "general_error_title".localized(), subtitle: subtitle ?? "general_error_text".localized())
        if let loading = self as? LoaderPresentable {
            loading.stopLoading {
                alert.show()
            }
        } else {
            alert.show()
        }
    }
}

fileprivate let crashlytics = Crashlytics.crashlytics()
public func report(error: Error, file: String = #file, line: Int = #line) {
    crashlytics.setCustomValue(error.localizedDescription, forKey: "description")
    crashlytics.setCustomValue(file, forKey: "file")
    crashlytics.setCustomValue(line, forKey: "line")
    crashlytics.record(error: error)
}
