//
//  ErrorHandler.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 19/09/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit

public extension UIViewController {
    
    fileprivate(set) var activity: ActivityViewController? {
        set {
            AppRouter.shared.activity = newValue
        }
        get {
            return AppRouter.shared.activity
        }
    }
    
    fileprivate(set) var alert: AlertController? {
        set {
            AppRouter.shared.alert = newValue
        }
        get {
            return AppRouter.shared.alert
        }
    }
    
    @objc
    func startLoading(_ text: String) {
        if let activity = activity {
            activity.loading(text)
        } else {
            let activity = ActivityViewController(text)
            activity.modalPresentationStyle = .overCurrentContext
            activity.modalTransitionStyle = .crossDissolve
            present(activity, animated: true, completion: nil)
            self.activity = activity
        }
    }
    
    @objc
    func stopLoading(completion: (() -> ())? = nil) {
        if let activity = activity {
            activity.hide(completion: completion)
        } else {
            completion?()
        }
    }
    
    func warning(title: String? = nil, message: String? = nil, completion: (() -> ())? = nil) {
        let m = message ?? "general_error_message".localized()
        let t = title ?? "general_error_title".localized()
        func presentAlert() {
            let alert = AlertController(title: t, message: .plain(m))
            alert.actions = [
                .plain(title: "ok".localized(), handler: completion)
            ]
            present(alert, animated: true, completion: nil)
            self.alert = alert
        }
        if let activity = activity {
            activity.hide {
                presentAlert()
            }
        } else if let alert = alert {
            alert.update(title: t, message: .plain(m))
        } else {
            presentAlert()
        }
    }
}
