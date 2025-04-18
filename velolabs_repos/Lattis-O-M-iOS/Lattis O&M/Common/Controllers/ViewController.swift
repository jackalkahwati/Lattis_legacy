//
//  ViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class ViewController: UIViewController, LoaderPresentable {
    var loadingView: LoadingView?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        title = "general_title".localized()
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
    }
}
