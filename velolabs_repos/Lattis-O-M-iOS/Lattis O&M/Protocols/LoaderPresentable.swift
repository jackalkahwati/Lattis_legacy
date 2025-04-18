//
//  LoaderRepresentable.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/11/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

protocol LoaderPresentable: class {
    var loadingView: LoadingView? {get set}
    func startLoading(title: String?)
    func stopLoading(completion:@escaping () -> ())
}

extension LoaderPresentable where Self: UIViewController {
    func startLoading(title: String? = nil) {
        if let view = loadingView {
            view.titleLabel.text = title
        } else {
            loadingView = LoadingView.show(title: title)
        }
    }
    
    func stopLoading(completion:@escaping () -> () = {}) {
        guard let view = loadingView else { return completion() }
        view.hide(completion: completion)
        loadingView = nil
    }
}
