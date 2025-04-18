//
//  AlertView+Ellipse.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/31/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

extension AlertView {
    class func deleteLock(unshare: Bool, action: @escaping () -> ()) -> AlertView {
        return alert(title: unshare ? "confirm_action".localized() : "warning".localized(), text: unshare ? "unshare_alert_text".localized() : "delete_lock_alert".localized(), actions: [
            AlertView.Action(title: unshare ? "unshare".localized().lowercased().capitalized : "delete".localized().lowercased().capitalized, style: .default, handler: { (_) in
                action()
            }),
            AlertView.Action(title: "cancel".localized().lowercased().capitalized, style: .cancel)
            ])
    }
}
