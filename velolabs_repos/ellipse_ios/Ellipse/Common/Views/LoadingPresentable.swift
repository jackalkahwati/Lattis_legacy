//
//  LoadingPresentable.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/24/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

protocol LoadingPresentable {
    func startLoading(text: String?)
    func stopLoading(completion: (() -> ())?)
}

extension LoadingPresentable {
    func startLoading(text: String?) {
        if let view = UIApplication.shared.keyWindow?.subviews.last as? LoadingView {
            view.textLabel.text = text
        } else {
            _ = LoadingView.show(text)
        }
    }
    
    func stopLoading(completion: (() -> ())?) {
        guard let view = UIApplication.shared.keyWindow?.subviews.last as? LoadingView else { completion?(); return }
        view.hide(completion: completion)
    }
}
