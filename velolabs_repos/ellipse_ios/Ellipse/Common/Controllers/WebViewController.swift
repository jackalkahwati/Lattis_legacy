//
//  WebViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 07/05/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Cartography
import JTMaterialSpinner

class WebViewController: ViewController {
    class func navigation(with title: String, url: URL) -> UINavigationController {
        let controller = WebViewController()
        controller.title = title.uppercased()
        controller.url = url
        let navigation = NavigationController(rootViewController: controller)
        largeTitleWhiteStyle(navigation.navigationBar)
        return navigation
    }
    
    internal let webView = UIWebView()
    internal var webViewGroup: ConstraintGroup!
    fileprivate var url: URL!
    fileprivate let spinner: JTMaterialSpinner = {
        let spinner = JTMaterialSpinner(frame: CGRect(x: 0, y: 0, width: 20, height: 20))
        spinner.circleLayer.lineWidth = 2
        spinner.circleLayer.strokeColor = UIColor.white.cgColor
        spinner.animationDuration = 1.5
        spinner.isUserInteractionEnabled = false
        return spinner
    }()
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .default
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        
        webView.delegate = self
        view.addSubview(webView)
        webViewGroup = constrain(webView) { (view) in
            view.edges == view.superview!.edges
        }
        
        addCloseButton()
        navigationItem.rightBarButtonItem = UIBarButtonItem(customView: spinner)
        
        webView.loadRequest(URLRequest(url: url))
    }
}

extension WebViewController: UIWebViewDelegate {
    func webViewDidStartLoad(_ webView: UIWebView) {
        spinner.beginRefreshing()
    }
    
    func webViewDidFinishLoad(_ webView: UIWebView) {
        spinner.endRefreshing()
    }
}
