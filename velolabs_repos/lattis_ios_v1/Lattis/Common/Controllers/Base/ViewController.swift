//
//  ViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 24/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit
import LGSideMenuController
import Oval

class ViewController: UIViewController {

    @IBOutlet weak var topContainer: UIView!
    @IBOutlet weak var contentContainer: UIView!
    @IBOutlet weak var topHeightLayout: NSLayoutConstraint!
    
    var isStatusBarHidden = false {
        didSet {
            setNeedsStatusBarAppearanceUpdate()
        }
    }
    weak var loadingView: LoadingView?
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .default
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        navigationController?.navigationBar.barStyle = preferredStatusBarStyle == .default ? .default : .black
    }
    
    override var prefersStatusBarHidden: Bool {
        return isStatusBarHidden
    }
    
    @objc func menu() {
        sideMenuController?.showLeftViewAnimated()
    }
}

extension ViewController: BaseInteractorOutput {
    func startLoading(with title: String? = nil) {
        if let view = loadingView {
            view.titleLabel.text = title
        } else {
            loadingView = LoadingView.show(title: title)
        }
    }
    
    func startLoading(with title: String? = nil, animated: Bool) {
        if let view = loadingView {
            view.titleLabel.text = title
        } else {
            loadingView = LoadingView.show(title: title, animated: animated)
        }
    }
    
    func stopLoading(completion:(() -> ())? = nil) {
        guard let view = loadingView else { completion?(); return }
        view.hide(completion: completion)
    }
    
    @objc func show(error: Error, file: String, line: Int) {
        var title = "general_error_title".localized()
        var subtitle = "general_error_text".localized()
        
        #if DEBUG
            subtitle = error.localizedDescription
        #endif
        let err = error as NSError
        if err.code == NSURLErrorNotConnectedToInternet, err.domain == NSURLErrorDomain {
            title = "general_no_internet_title".localized()
            subtitle = "general_no_internet_text".localized()
        }
        
        warning(with: title, subtitle: subtitle)
        
        Analytics.report(error, file: file, line: line)
    }
    
    func show(error: Error, file: String, line: Int, action: @escaping () -> ()) {
        var title = "general_error_title".localized()
        var subtitle = "general_error_text".localized()
        
        #if DEBUG
        subtitle = error.localizedDescription
        #endif
        let err = error as NSError
        if err.code == NSURLErrorNotConnectedToInternet, err.domain == NSURLErrorDomain {
            title = "general_no_internet_title".localized()
            subtitle = "general_no_internet_text".localized()
        }
        
        warning(with: title, subtitle: subtitle, action: action)
        
        Analytics.report(error, file: file, line: line)
    }
    
    func warning(with title: String, subtitle: String?) {
        stopLoading {
            let alert = ErrorAlertView.alert(title: title, subtitle: subtitle)
            alert.show()
        }
    }
    
    func warning(with title: String, subtitle: String?, action: @escaping () -> ()) {
        stopLoading {
            let alert = ErrorAlertView.alert(title: title, subtitle: subtitle)
            alert.show()
            alert.action  = action
        }
    }
}


extension ViewController: UIGestureRecognizerDelegate {}

