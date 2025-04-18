//
//  ViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/6/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class ViewController: UIViewController {
    var loadingView: LoadingView?
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        if #available(iOS 13.0, *) {
            return .darkContent
        }
        return .default
    }
    
    override var title: String? {
        didSet {
            super.title = title?.lowercased().capitalized
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
    }
    
    func didHideWarning() {
        
    }
}

extension ViewController: InteractorOutput {
    
}

protocol InteractorOutput: class, LoadingPresentable, ErrorPresentable {
}

class NavigationController: UINavigationController {
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return topViewController?.preferredStatusBarStyle ?? .default
    }
}
