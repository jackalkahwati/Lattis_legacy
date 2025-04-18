//
//  ErrorPresentable.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/24/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

protocol ErrorPresentable {
    func show(error: Error)
    func show(warning: String, title: String?)
    func didHideWarning()
}

extension ErrorPresentable {
    func show(error: Error) {
        let text = "general_warning_text".localized()
        show(warning: text, title: nil)
    }
    
    func show(warning: String, title: String?) {
        func show() {
            AlertView.alert(title: title ?? "warning".localized(), text: warning, actions: [.ok {
                self.didHideWarning()
                }]).show()
        }
        if let view = UIApplication.shared.keyWindow?.subviews.last as? LoadingView {
            view.hide(completion: show)
        } else if let view = UIApplication.shared.keyWindow?.subviews.last as? AlertView {
//            view.textLabel.text = warning
            view.titleLabel.text = title ?? "warning".localized()
        } else {
            show()
        }
    }
    
    func didHideWarning() {
        
    }
}
